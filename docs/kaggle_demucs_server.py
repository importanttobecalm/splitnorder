# Bağımlılıklar — pyngrok artık YOK, cloudflared subprocess olarak çalışır

import os, re, shutil, subprocess, time, threading
from concurrent.futures import ThreadPoolExecutor
from threading import Thread

from fastapi import FastAPI, UploadFile, File, Form, Body
from fastapi.responses import FileResponse, JSONResponse
import numpy as np
import soundfile as sf
import uvicorn, nest_asyncio

app = FastAPI()
os.makedirs("/kaggle/working/uploads", exist_ok=True)
os.makedirs("/kaggle/working/output", exist_ok=True)

JOBS = {}  # job_id -> {status, progress, message, base}
STEMS = ["vocals", "drums", "bass", "other"]


@app.get("/")
@app.get("/api/health")
def health():
    return {"status": "alive"}


def convert_to_mp3(wav_path: str, mp3_path: str) -> None:
    subprocess.run(
        [
            "ffmpeg", "-y", "-i", wav_path,
            "-codec:a", "libmp3lame",
            "-b:a", "192k",
            mp3_path,
        ],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )


@app.post("/separate")
@app.post("/api/separate")
async def separate(
    file: UploadFile = File(...),
    job_id: str = Form(None),
    model: str = Form("htdemucs"),
):
    jid = job_id or os.path.splitext(file.filename)[0]
    JOBS[jid] = {"status": "processing", "progress": 10, "message": "started"}

    input_path = f"/kaggle/working/uploads/{file.filename}"
    with open(input_path, "wb") as f:
        shutil.copyfileobj(file.file, f)

    JOBS[jid] = {"status": "processing", "progress": 30, "message": "running demucs (wav)"}

    # 1) Demucs çalıştır — WAV
    try:
        subprocess.run(
            ["demucs", "-o", "/kaggle/working/output", input_path],
            check=True,
        )
    except subprocess.CalledProcessError as e:
        JOBS[jid] = {"status": "failed", "progress": 0, "message": f"demucs: {e}"}
        return JSONResponse({"status": "failed", "error": str(e)}, status_code=500)

    base = os.path.splitext(file.filename)[0]
    stems_dir = f"/kaggle/working/output/htdemucs/{base}"

    JOBS[jid] = {"status": "processing", "progress": 75, "message": "converting to mp3"}

    # 2) ffmpeg ile 4 stem için MP3 üret (paralel)
    try:
        with ThreadPoolExecutor(max_workers=4) as pool:
            futures = []
            for stem in STEMS:
                wav = f"{stems_dir}/{stem}.wav"
                mp3 = f"{stems_dir}/{stem}.mp3"
                futures.append(pool.submit(convert_to_mp3, wav, mp3))
            for fut in futures:
                fut.result()
    except subprocess.CalledProcessError as e:
        JOBS[jid] = {"status": "failed", "progress": 0, "message": f"ffmpeg: {e}"}
        return JSONResponse({"status": "failed", "error": str(e)}, status_code=500)

    JOBS[jid] = {
        "status": "completed",
        "progress": 100,
        "message": "done",
        "base": base,
    }
    return {"status": "completed", "job_id": jid, "base": base}


@app.get("/api/job/{job_id}/status")
def job_status(job_id: str):
    job = JOBS.get(job_id)
    if not job:
        return {
            "status": "completed",
            "progress": 100,
            "message": "unknown job assumed done",
        }
    return job


@app.post("/api/mix")
def mix_stems(payload: dict = Body(...)):
    """
    Karma mix üretir: seçilen stem WAV'larını sample-level toplar, normalize
    eder ve istenen formatta (mp3/wav) tek bir dosya olarak döner.

    Body: {"job_id": "job-<publicId>", "stems": ["vocals","drums"], "fmt": "mp3"|"wav"}

    Java tarafı (MixService) çağırır, dönen dosyayı diske kaydeder.
    """
    job_id = payload.get("job_id")
    stems = payload.get("stems") or []
    fmt = (payload.get("fmt") or "mp3").lower()

    if fmt not in ("mp3", "wav"):
        return JSONResponse({"error": "fmt must be mp3 or wav"}, status_code=400)
    if not stems or len(stems) < 2:
        return JSONResponse({"error": "at least 2 stems required"}, status_code=400)
    if any(s not in STEMS for s in stems):
        return JSONResponse({"error": "invalid stem name"}, status_code=400)

    job = JOBS.get(job_id, {})
    base = job.get("base", job_id)
    stems_dir = f"/kaggle/working/output/htdemucs/{base}"

    # Stem WAV'larını yükle (hepsi aynı sample rate + uzunluk — Demucs garantisi).
    arrays = []
    sr = None
    for stem in stems:
        path = f"{stems_dir}/{stem}.wav"
        if not os.path.exists(path):
            return JSONResponse({"error": f"stem not found: {stem}"}, status_code=404)
        data, this_sr = sf.read(path, dtype="float32")
        sr = this_sr if sr is None else sr
        arrays.append(data)

    # Sample-level topla, peak'e göre normalize et (clipping engelle).
    mixed = np.sum(arrays, axis=0)
    peak = float(np.max(np.abs(mixed))) if mixed.size else 0.0
    if peak > 1.0:
        mixed = mixed / peak

    # Çıktıyı tmp WAV'a yaz, gerekirse ffmpeg ile mp3'e çevir.
    out_dir = f"/kaggle/working/output/htdemucs/{base}/mixes"
    os.makedirs(out_dir, exist_ok=True)
    mix_id = f"{'+'.join(stems)}-{int(time.time())}"
    wav_out = f"{out_dir}/{mix_id}.wav"
    sf.write(wav_out, mixed, sr, subtype="PCM_16")

    if fmt == "wav":
        return FileResponse(wav_out, media_type="audio/wav", filename=f"{mix_id}.wav")

    mp3_out = f"{out_dir}/{mix_id}.mp3"
    convert_to_mp3(wav_out, mp3_out)
    return FileResponse(mp3_out, media_type="audio/mpeg", filename=f"{mix_id}.mp3")


@app.get("/api/stem/{job_id}/{stem_type}")
def get_stem(job_id: str, stem_type: str, fmt: str = "mp3"):
    stem_clean = stem_type.replace(".wav", "").replace(".mp3", "")
    if fmt not in ("mp3", "wav"):
        fmt = "mp3"

    job = JOBS.get(job_id, {})
    base = job.get("base", job_id)

    path = f"/kaggle/working/output/htdemucs/{base}/{stem_clean}.{fmt}"
    if not os.path.exists(path):
        return JSONResponse(
            {"error": f"stem not found: {path}"}, status_code=404
        )

    media_type = "audio/mpeg" if fmt == "mp3" else "audio/wav"
    return FileResponse(
        path,
        filename=f"{stem_clean}.{fmt}",
        media_type=media_type,
    )


# ───── Cloudflare Tunnel (ephemeral, kayıtsız) ─────
# 1) cloudflared binary'sini indir (Kaggle x86_64)
CLOUDFLARED_BIN = "/kaggle/working/cloudflared"
if not os.path.exists(CLOUDFLARED_BIN):
    print("⬇️  cloudflared indiriliyor...")
    subprocess.run(
        ["wget", "-q",
         "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64",
         "-O", CLOUDFLARED_BIN],
        check=True,
    )
    print("✅ cloudflared indirildi")

# chmod'u her run'da garantile (yarım kalan indirme / exec biti yoksa düzeltir)
os.chmod(CLOUDFLARED_BIN, 0o755)

# 2) Uvicorn'u background'da başlat — sadece bir kez (hücre tekrar çalışırsa port çakışmasın)
nest_asyncio.apply()
UVICORN_THREAD_NAME = "uvicorn-thread"
if not any(t.name == UVICORN_THREAD_NAME for t in threading.enumerate()):
    Thread(
        target=lambda: uvicorn.run(app, host="0.0.0.0", port=8000),
        name=UVICORN_THREAD_NAME,
        daemon=True,
    ).start()
    time.sleep(2)  # uvicorn'un port'a bağlanmasına süre ver
else:
    print("ℹ️  uvicorn zaten çalışıyor, yeni thread başlatılmadı")

# 3) cloudflared ephemeral tunnel başlat — stdout'tan trycloudflare URL'sini yakala
print("☁️  Cloudflare Tunnel başlatılıyor...")
tunnel = subprocess.Popen(
    [CLOUDFLARED_BIN, "tunnel", "--url", "http://localhost:8000",
     "--no-autoupdate", "--metrics", "localhost:0"],
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    text=True,
    bufsize=1,
)

public_url = None
deadline = time.time() + 45
url_pattern = re.compile(r"https://[a-z0-9-]+\.trycloudflare\.com")
while time.time() < deadline:
    line = tunnel.stdout.readline()
    if not line:
        time.sleep(0.2)
        continue
    print(line, end="")
    m = url_pattern.search(line)
    if m:
        public_url = m.group(0)
        break

if public_url:
    print("\n" + "=" * 60)
    print(f"🌍 PUBLIC URL: {public_url}")
    print("=" * 60)
    print("\nSunucudaki Java'ya inject etmek için:")
    print(f"\n  ssh -i ~/Desktop/ssh-key-2026-02-24.key ubuntu@130.61.66.0 \\")
    print(f"    '/home/ubuntu/splitnorder-demo/set-gpu-url.sh {public_url}'\n")
else:
    print("\n⚠️  Cloudflared URL 45 saniyede yakalanamadı.")
    print("    Log çıktısına bakıp manuel URL'yi al + set-gpu-url.sh'a ver.")

# Tunnel log'larını yutmaya devam et (notebook kapanmasın)
def drain_logs():
    for line in tunnel.stdout:
        pass
Thread(target=drain_logs, daemon=True).start()
