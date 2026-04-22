# 🧪 SplitNOrder — Lokal Testing Guide

## Hızlı Başlangıç (M3 8GB Mac)

```bash
# 1️⃣ Setup
./test-local.sh setup

# 2️⃣ Run (her terminal'de otomatik başlatır)
./test-local.sh run

# 3️⃣ Cleanup (Ctrl+C veya manual)
./test-local.sh cleanup
```

## Manual Start (İleri Kullanıcılar İçin)

### Terminal 1 — Flask API
```bash
cd demucs-server
python3 app.py

# Output:
# ============================================================
#   SplitNOrder — Lokal Demucs API Sunucusu
# ============================================================
# → http://localhost:5000/api/health
```

### Terminal 2 — Tomcat
```bash
mvn cargo:run

# Bekle ~10 saniye, sonra açık:
# http://localhost:8080/stemsep/
```

---

## Test Senaryosu

### 1️⃣ Home Sayfası
```
URL: http://localhost:8080/stemsep/
✅ Hero section görüntüleniyor
✅ Feature cards (4-Stem, AI Powered, Fast)
✅ "Get Started" buttonu /upload'a yönlendiriyor
```

### 2️⃣ Upload Dosya
```
URL: http://localhost:8080/stemsep/upload

1. Test dosya seç (test.mp3, test.wav, test.flac)
   - Min: 100KB
   - Max: 50MB
   
2. Model seç: htdemucs (default) veya htdemucs_ft

3. Submit butonu aktif mi? (dosya seçilince)

4. POST /upload
   ✅ Dosya uploads/ dizinine kaydedildi
   ✅ Job record DB'de PENDING status
   ✅ Otomatik /job/{id}'ye redirect
```

### 3️⃣ Processing Sayfası
```
URL: http://localhost:8080/stemsep/job/{id}
Status: PENDING → PROCESSING

JS polling 3 saniye aralıkla:
GET /job/{id}/status

✅ Spinner animasyonu gösteriliyor
✅ Dosya adı ve model gösteriliyor
✅ Status badge (processing)

Bekleme: Flask'ta Demucs çalışıyor...
(Test dosya: ~30-60 saniye)
```

### 4️⃣ Result Sayfası (Success Path)
```
URL: http://localhost:8080/stemsep/job/{id}
Status: COMPLETED

✅ 4 Stem Card görüntüleniyor
   - Vocals
   - Drums
   - Bass
   - Other

✅ Her stem'in altında:
   - <audio> player (INLINE PLAYBACK)
   - Individual download button
   
✅ "Download All (ZIP)" button
   - Tüm stem'leri ZIP'e pakla
   - İndirilen dosya: stems_{originalFilename}.zip
   
✅ Audio player tarayıcıda çalıyor (download dialog YOK)
   - [Content-Disposition: inline]
```

### 5️⃣ History Sayfası
```
URL: http://localhost:8080/stemsep/history

✅ Tüm job'lar tabloda listelenir
✅ Tarih formatı: YYYY-MM-DDTHH:mm (okunabilir)
   Örnek: 2026-04-22T15:30
   
✅ Status badges: COMPLETED ✅ PROCESSING ⏳ FAILED ❌

✅ "View" button → /job/{id}
```

### 6️⃣ Error Path (Mock Mode)
Flask API yanıt vermezse:
```
ColabInferenceService.mockProcessing()
→ 3 saniye uyuyor, return true (simülasyon)

Result: Job COMPLETED ama stems/{jobId}/*.wav YOKTUR
Download endpoint: 404 (expected in mock mode)
```

---

## Sistem Kaynakları (M3 8GB)

Monitor et: `Activity Monitor` → Memory tab

```
Flask + Demucs:   ~1.5-2.0 GB
Tomcat + Spring:  ~1.5-2.0 GB
System:           ~2-3 GB
─────────────────────────────
Total:            ~5-7 GB (safe ✅)
```

**RED FLAG:** 7.5GB+ mem usage → bir servisi restart et

---

## Troubleshooting

### Flask API başlamıyor
```bash
# Check:
python3 --version          # 3.12+?
pip3 list | grep demucs    # Yüklü mü?

# Fix:
pip3 install -r demucs-server/requirements.txt
python3 demucs-server/app.py
```

### Tomcat 10080'de başlamıyor
```bash
# Check:
lsof -i :8080              # Meşgul mi?

# Fix:
kill -9 $(lsof -t -i:8080) # Önceki process'i öldür
mvn cargo:run
```

### Audio player çalmıyor
```
❌ İndirme dialogu açılıyorsa:
   → JobController.downloadStem() Content-Disposition kontrol et
   → İnline ayarlanmış mı?
   
✅ Tamam: Content-Disposition: inline; filename="vocals.wav"
```

### Tarih düzgün görünmüyor
```
history.jsp'de:
${fn:substring(job.createdAt.toString(), 0, 16)}

Test: Saat 15:30:42.123456 →  2026-04-22T15:30 ✅
```

### ZIP indirme başarısız
```
/job/{id}/download-all

Check:
1. Job COMPLETED mi?
2. stems/{jobId}/*.wav dosyaları mevcut mi?
   (Mock mode'da dosyalar oluşturulmuyor!)
```

---

## Environment Variables

```bash
# Flask API URL (default: http://localhost:5000)
export COLAB_API_URL=http://localhost:5000

# Tomcat port (default: 8080)
export CARGO_SERVLET_PORT=8080

# Log level (optional)
export LOG_LEVEL=INFO
```

---

## İleri: Real Audio Test

Test MP3 dosyası oluştur:
```bash
# ffmpeg kullanan örnek (bilgisayarda yoksa skip et)
ffmpeg -f lavfi -i "aevalsrc=sin(440*2*3.14159*t):s=44100" -duration 5 test.wav
```

---

## Next: Server/GPU Integration

✅ Lokal test tamamlandıktan sonra:
1. Oracle Cloud GPU setup
2. Ngrok tunnel configuration
3. Remote Demucs server entegrasyonu

PDF özetlerini paylaş → Integration planinı adjust edeceğim! 🚀
