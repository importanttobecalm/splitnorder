"""
Splitnorder · Kaggle Demucs API (dual format — WAV master + MP3 derived)
=========================================================================

İki format üretir:
  - WAV: Demucs'ın doğal çıktısı (kayıpsız, profesyonel, ~40 MB/stem)
  - MP3: ffmpeg ile 192 kbps türev (~4 MB/stem)

Java tarayıcıya stream ederken MP3 kullanır (hızlı), kullanıcı "WAV indir"
seçerse ZIP'e WAV girer.

Kaggle session yenilenince bağımlılıklar gider — aşağıdaki !pip satırı
Jupyter `!` magic'i ile çalışır, pip zaten yüklüyse sessizce skip eder.
(ffmpeg Kaggle ortamında zaten yüklü, ek pip gerekmiyor)
"""

# Bağımlılıklar — Jupyter hücresinde `!` magic, zarar vermez
!pip install -q pyngrok fastapi "uvicorn[standard]" nest_asyncio python-multipart demucs

from pyngrok import ngrok, conf
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import FileResponse, JSONResponse
import uvicorn, nest_asyncio, subprocess, os, shutil
from concurrent.futures import ThreadPoolExecutor
from threading import Thread

# ngrok auth
conf.get_default().auth_token = "3DP4QTvoSpRXS8xbbf49vwds6c2_2SRfb7tpw969ErUkbrsE"

app = FastAPI()
os.makedirs("/kaggle/working/uploads", exist_ok=True)
os.makedirs("/kaggle/working/output", exist_ok=True)

# Job durumlarını tutan basit dict
JOBS = {}

# Demucs'ın çıkardığı 4 stem ismi
STEMS = ["vocals", "drums", "bass", "other"]


@app.get("/")
@app.get("/api/health")
def health():
    return {"status": "alive"}


def convert_to_mp3(wav_path: str, mp3_path: str) -> None:
    """ffmpeg ile WAV → MP3 (192 kbps) dönüşümü, hızlı + yüksek kalite."""
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

    # Dosyayı kaydet
    input_path = f"/kaggle/working/uploads/{file.filename}"
    with open(input_path, "wb") as f:
        shutil.copyfileobj(file.file, f)

    JOBS[jid] = {"status": "processing", "progress": 30, "message": "running demucs (wav)"}

    # 1) Demucs çalıştır — WAV (default)
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

    # 2) ffmpeg ile her stem için MP3 türev üret — 4 dönüşüm paralel
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


@app.get("/api/stem/{job_id}/{stem_type}")
def get_stem(job_id: str, stem_type: str, fmt: str = "mp3"):
    """
    Stem dosyasını döndürür.
      fmt=mp3 (default) → küçük, hızlı, tarayıcı streaming için
      fmt=wav           → kayıpsız master, indirme için
    """
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


# Static domain
public_url = ngrok.connect(8000, domain="approval-licking-thread.ngrok-free.dev")
print(f"🌍 PUBLIC URL: {public_url}")

nest_asyncio.apply()
Thread(target=lambda: uvicorn.run(app, host="0.0.0.0", port=8000)).start()
