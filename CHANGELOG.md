# 📋 SplitNOrder — Değişiklik Günlüğü (CHANGELOG)

> Bu dosya projedeki **tüm değişiklikleri** kronolojik sırayla takip eder.
> Her yeni özellik, düzeltme veya yapısal değişiklik burada belgelenir.
> **Ekip üyeleri**: Bu dosyayı `git pull` sonrası kontrol ederek nelerin değiştiğini takip edebilirsiniz.

---

## Versiyon Formatı

```
[MAJOR.MINOR.PATCH] — YYYY-MM-DD — Kısa Başlık

### Eklenenler (Added)       🟢
### Değiştirilenler (Changed) 🟡
### Düzeltilenler (Fixed)     🔵
### Kaldırılanlar (Removed)   🔴
### Notlar                    📝
```

---

## [0.1.0] — 2026-04-22 — Proje İskeleti & Temel Altyapı

### 🟢 Eklenenler
- **Maven Proje Yapısı**: `pom.xml` ile tüm bağımlılıklar tanımlandı
  - Spring MVC 6.0.4, Hibernate 6.1.7, c3p0, Jackson, H2, JUnit 4.13.1
- **Konfigürasyon Katmanı**:
  - `AppConfig.java` — Component scan, async, transaction management
  - `WebConfig.java` — ViewResolver, i18n, multipart, interceptor
  - `HibernateConfig.java` — SessionFactory, c3p0 connection pool
  - `WebAppInitializer.java` — Servlet 6.0 başlatıcı (web.xml yok)
- **Model Katmanı**: `User.java`, `Job.java`, `Stem.java`, `JobStatus.java`
- **DAO Katmanı**: `UserDao.java`, `JobDao.java`, `StemDao.java`
- **Service Katmanı**:
  - `JobService.java` — Dosya yükleme, job yönetimi
  - `ColabInferenceService.java` — GPU inference (mock mode destekli)
  - `UserService.java`, `StemService.java`
- **Controller Katmanı**:
  - `HomeController.java` — Ana sayfa
  - `UploadController.java` — Dosya yükleme (max 50MB, mp3/wav/flac)
  - `JobController.java` — İş durumu, stem indirme, ZIP export
  - `HistoryController.java` — Geçmiş işlemler
- **RequestLoggingInterceptor** — HTTP istek/yanıt loglama
- **JSP Views**: home, upload, processing, result, history
  - Dark mode tasarım, Bootstrap 5, Inter font
  - Gradient efektler, glassmorphism, micro-animasyonlar
- **i18n Desteği**: `messages_tr.properties`, `messages_en.properties`
- **Loglama**: log4j 1.2.14 + slf4j 1.7.25 (`logs/bm470.log`)
- **application.properties**: H2 (dev), MySQL (prod) konfigürasyonu

### 📝 Notlar
- Veritabanı: Geliştirmede H2 in-memory, üretimde Oracle Cloud MySQL
- GPU API: `colab.api.url=http://localhost:5000` — şu an mock mode

---

## [0.2.0] — 2026-04-22 — Lokal Demucs Flask API & Entegrasyon

### 🟢 Eklenenler
- **`demucs-server/` — Python Flask API Sunucusu**:
  - `app.py` — Flask REST API (port 5000)
  - `requirements.txt` — Flask 3.0+, Demucs 4.0+
  - **API Endpoints**:
    - `POST /api/separate` — Müzik dosyasını stem'lere ayır (202 Accepted)
    - `GET /api/job/{id}/status` — İş durumunu sorgula (polling)
    - `GET /api/stem/{id}/{stem_type}` — Stem dosyasını indir
    - `GET /api/health` — Sunucu sağlık kontrolü
  - **Özellikler**:
    - Demucs CLI ile 4 stem ayırma (vocals, drums, bass, other)
    - Arka planda işleme (threading)
    - İş durumu takibi (in-memory jobs dict)
    - HTDemucs & HTDemucs FT model desteği

### 🟡 Değiştirilenler
- **`ColabInferenceService.java`** — Flask API bağlantısı eklendi:
  - `sendSeparateRequest()` — POST /api/separate
  - `pollUntilComplete()` — GET /api/job/{id}/status yoklama
  - `createStemRecords()` — Stem kayıtlarını DB'ye yaz
  - Mock mode fallback (API çalışmazsa simüle et)
- **`application.properties`** — `colab.api.url=http://localhost:5000` (default)

### 📝 Notlar
- Flask API test edildi: Health check başarılı ✅
- Java ↔ Flask bağlantısı: Polling-based asynchronous processing
- Demucs modelleri otomatik indirilir (first run)
- İleride: Job status persistent storage (şu an in-memory)

---

## [Sonraki] — Planlanıyor

### 📋 Faz 2: Arayüz İyileştirmeleri
- [ ] Tomcat ile lokal deploy & test
- [ ] JSP view'ların uçtan uca testi (uploaders → processing → result)
- [ ] Stem indirme & ZIP export testi

### 📋 Faz 3: Sunucu & GPU Bağlantısı
- [ ] Oracle Cloud GPU sunucu entegrasyonu
- [ ] Ngrok tunnel yapılandırması
