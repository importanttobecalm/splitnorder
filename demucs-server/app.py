"""
SplitNOrder — Lokal Demucs Flask API Sunucusu
=============================================
Bu sunucu, Java Spring MVC backend'inden gelen istekleri alıp
Demucs ile müzik stem ayırma işlemi yapar.

Kullanım:
    python3 app.py

API Endpoints:
    POST /api/separate  — Müzik dosyasını stem'lere ayır
    GET  /api/health    — Sunucu sağlık kontrolü
    GET  /api/job/<id>/status — İş durumu sorgula
"""

import os
import sys
import json
import uuid
import shutil
import subprocess
import threading
import time
from pathlib import Path
from flask import Flask, request, jsonify, send_file

app = Flask(__name__)

# ─── Konfigürasyon ───────────────────────────────────────────────────────────
# Proje kök dizini (splitnorder/)
PROJECT_ROOT = Path(__file__).parent.parent.resolve()
UPLOAD_DIR = PROJECT_ROOT / "uploads"
STEMS_DIR = PROJECT_ROOT / "stems"
DEMUCS_OUTPUT_DIR = PROJECT_ROOT / "demucs-output"  # Demucs'un kendi çıktı dizini

# Desteklenen modeller
SUPPORTED_MODELS = {
    "htdemucs": "HTDemucs (Varsayılan — Hızlı)",
    "htdemucs_ft": "HTDemucs Fine-Tuned (Yavaş — Kaliteli)",
}

# İş durumlarını takip et (in-memory)
jobs = {}

# ─── Yardımcı Fonksiyonlar ───────────────────────────────────────────────────

def ensure_dirs():
    """Gerekli dizinleri oluştur."""
    UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
    STEMS_DIR.mkdir(parents=True, exist_ok=True)
    DEMUCS_OUTPUT_DIR.mkdir(parents=True, exist_ok=True)


def run_demucs(file_path: str, model: str, job_id: int):
    """
    Demucs CLI'yi subprocess ile çalıştırıp stem ayırma yapar.
    Bu fonksiyon ayrı bir thread'de çağrılır.
    """
    jobs[job_id] = {
        "status": "processing",
        "progress": 0,
        "message": f"Demucs ({model}) ile işleniyor...",
        "stems": {},
        "error": None
    }

    try:
        file_path = Path(file_path).resolve()
        if not file_path.exists():
            raise FileNotFoundError(f"Dosya bulunamadı: {file_path}")

        # Demucs komutunu oluştur
        cmd = [
            sys.executable, "-m", "demucs",
            "--name", model,
            "--out", str(DEMUCS_OUTPUT_DIR),
            "--float32",        # WAV float32 formatı
            "--two-stems", "vocals",  # Önce sadece vocals test için kaldırılabilir
            str(file_path)
        ]

        # İlk etapta 4 stem ayıralım (two-stems'i kaldır)
        cmd = [
            sys.executable, "-m", "demucs",
            "--name", model,
            "--out", str(DEMUCS_OUTPUT_DIR),
            "--float32",
            str(file_path)
        ]

        print(f"[JOB {job_id}] Demucs komutu: {' '.join(cmd)}")
        jobs[job_id]["progress"] = 10
        jobs[job_id]["message"] = "Demucs başlatıldı, model yükleniyor..."

        # Demucs'u çalıştır
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=1800  # 30 dakika timeout
        )

        if result.returncode != 0:
            error_msg = result.stderr or "Bilinmeyen hata"
            print(f"[JOB {job_id}] Demucs HATA: {error_msg}")
            raise RuntimeError(f"Demucs hatası: {error_msg}")

        jobs[job_id]["progress"] = 80
        jobs[job_id]["message"] = "Stem dosyaları taşınıyor..."

        # Demucs çıktı dosyalarını stems/{job_id}/ dizinine taşı
        stem_source_dir = DEMUCS_OUTPUT_DIR / model / file_path.stem
        job_stems_dir = STEMS_DIR / str(job_id)
        job_stems_dir.mkdir(parents=True, exist_ok=True)

        stem_types = ["vocals", "drums", "bass", "other"]
        found_stems = {}

        for stem_type in stem_types:
            source_file = stem_source_dir / f"{stem_type}.wav"
            if source_file.exists():
                dest_file = job_stems_dir / f"{stem_type}.wav"
                shutil.copy2(str(source_file), str(dest_file))
                found_stems[stem_type] = str(dest_file)
                print(f"[JOB {job_id}] Stem kopyalandı: {stem_type}.wav ({dest_file.stat().st_size} bytes)")
            else:
                print(f"[JOB {job_id}] Stem bulunamadı: {stem_type}.wav ({source_file})")

        if not found_stems:
            raise RuntimeError("Hiçbir stem dosyası oluşturulmadı!")

        # Demucs geçici çıktısını temizle
        if stem_source_dir.exists():
            shutil.rmtree(stem_source_dir, ignore_errors=True)

        jobs[job_id]["status"] = "completed"
        jobs[job_id]["progress"] = 100
        jobs[job_id]["message"] = "Tamamlandı!"
        jobs[job_id]["stems"] = found_stems
        print(f"[JOB {job_id}] ✅ Başarıyla tamamlandı: {len(found_stems)} stem")

    except Exception as e:
        print(f"[JOB {job_id}] ❌ HATA: {e}")
        jobs[job_id]["status"] = "failed"
        jobs[job_id]["progress"] = 0
        jobs[job_id]["message"] = str(e)
        jobs[job_id]["error"] = str(e)


# ─── API Endpoints ───────────────────────────────────────────────────────────

@app.route("/api/health", methods=["GET"])
def health_check():
    """Sunucu sağlık kontrolü."""
    return jsonify({
        "status": "ok",
        "service": "SplitNOrder Demucs API",
        "supported_models": SUPPORTED_MODELS,
        "project_root": str(PROJECT_ROOT),
        "upload_dir": str(UPLOAD_DIR),
        "stems_dir": str(STEMS_DIR)
    })


@app.route("/api/separate", methods=["POST"])
def separate():
    """
    Müzik dosyasını Demucs ile stem'lere ayır.

    JSON Body:
        file_path (str): Müzik dosyasının yolu (uploads/ dizinine göre veya mutlak)
        model (str): Demucs model adı (htdemucs, htdemucs_ft)
        job_id (int): Java backend'den gelen iş kimliği
    """
    data = request.get_json()

    if not data:
        return jsonify({"error": "JSON body gerekli"}), 400

    file_path = data.get("file_path")
    model = data.get("model", "htdemucs")
    job_id = data.get("job_id")

    if not file_path:
        return jsonify({"error": "file_path gerekli"}), 400
    if not job_id:
        return jsonify({"error": "job_id gerekli"}), 400

    # Model doğrulama
    if model not in SUPPORTED_MODELS:
        return jsonify({
            "error": f"Desteklenmeyen model: {model}",
            "supported": list(SUPPORTED_MODELS.keys())
        }), 400

    # Dosya yolunu çözümle
    resolved_path = Path(file_path)
    if not resolved_path.is_absolute():
        resolved_path = PROJECT_ROOT / file_path

    if not resolved_path.exists():
        return jsonify({"error": f"Dosya bulunamadı: {resolved_path}"}), 404

    # Zaten işleniyorsa
    if job_id in jobs and jobs[job_id]["status"] == "processing":
        return jsonify({"error": "Bu iş zaten işleniyor", "job_id": job_id}), 409

    # Arka planda Demucs'u çalıştır
    thread = threading.Thread(
        target=run_demucs,
        args=(str(resolved_path), model, job_id),
        daemon=True
    )
    thread.start()

    return jsonify({
        "status": "accepted",
        "job_id": job_id,
        "message": f"İşlem başlatıldı (model: {model})"
    }), 202


@app.route("/api/job/<int:job_id>/status", methods=["GET"])
def job_status(job_id):
    """İş durumunu sorgula."""
    if job_id not in jobs:
        return jsonify({"error": "İş bulunamadı", "job_id": job_id}), 404

    return jsonify({
        "job_id": job_id,
        **jobs[job_id]
    })


@app.route("/api/stem/<int:job_id>/<stem_type>", methods=["GET"])
def download_stem(job_id, stem_type):
    """Belirli bir stem dosyasını indir."""
    stem_path = STEMS_DIR / str(job_id) / f"{stem_type}.wav"
    if not stem_path.exists():
        return jsonify({"error": f"Stem bulunamadı: {stem_type}"}), 404

    return send_file(
        str(stem_path),
        mimetype="audio/wav",
        as_attachment=True,
        download_name=f"{stem_type}.wav"
    )


# ─── Ana Giriş ───────────────────────────────────────────────────────────────

if __name__ == "__main__":
    ensure_dirs()
    print("=" * 60)
    print("  SplitNOrder — Lokal Demucs API Sunucusu")
    print("=" * 60)
    print(f"  Proje Kökü  : {PROJECT_ROOT}")
    print(f"  Upload Dizini: {UPLOAD_DIR}")
    print(f"  Stems Dizini : {STEMS_DIR}")
    print(f"  Modeller     : {', '.join(SUPPORTED_MODELS.keys())}")
    print("=" * 60)
    print("  → http://localhost:5000/api/health")
    print("  → POST http://localhost:5000/api/separate")
    print("=" * 60)

    app.run(host="0.0.0.0", port=5000, debug=True)
