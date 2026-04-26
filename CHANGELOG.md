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

## [0.4.0] — 2026-04-26 — Ders Kurallarına Hizalama + Oracle Cloud MySQL Bağlantısı

### 🟢 Eklenenler
- **`docs/guidelines/`** — Tüm proje yönerge yapısı:
  - `README.md`, `INTEGRATION_GUIDELINES.md`, `PROJECT_ARCHITECTURE.md`, `CODING_STANDARDS.md`, `DEPLOYMENT_GUIDE.md`
  - `courses/` altında `ders7-8-9.md` (1923 satır) ve `ders_kod_referansi.md` (1006 satır) BM470 ders dökümanları entegre edildi
- **`CLAUDE.md`** — Claude Code için proje çalışma talimatları (her yeni konuşmada otomatik yüklenir)
- **`hibernate.properties`** + **`hibernate.properties.example`** — ders §7.7 kalıbına uygun config (gitignored, şifre içerir)
- **`OracleMySQLConnectionTest`** (`src/test/java/com/stemsep/integration/`) — Spring context + Hibernate SessionFactory + Oracle Cloud MySQL canlı bağlantı testi (3/3 PASS)
- **Oracle Cloud MySQL HeatWave 9.6.1** — `stemsep_db` şeması (utf8mb4), `admin@%` user, bastion SSH tunnel ile lokal erişim
- **SSH key altyapısı** — `~/.ssh/oracle_key` ed25519, `~/.ssh/config`, `~/.my.cnf` (mysql-client)
- **Lombok 1.18.42** dependency (provided scope, ders §7.10)

### 🟡 Değiştirilenler
- **pom.xml**:
  - JDK 21 → **17** (LTS, ders 18'in EOL alternatifi)
  - Hibernate 6.1.7 → **5.6.15.Final-jakarta** (Spring 6 jakarta uyumlu, ders 5.x kuralı)
  - `hibernate-core` → `hibernate-core-jakarta` artifactId
  - Servlet API 6.0.0 → 6.1.0 (ders versiyonu)
  - Spring 6.0.4 (değişmedi — bağlayıcı)
- **`HibernateConfig.java`** — Ders §7.7 kalıbına yeniden yazıldı:
  - `AvailableSettings.*` static import ile property anahtarları
  - `LocalSessionFactoryBean` + `HibernateTransactionManager`
  - Tüm c3p0 parametreleri ders şablonuna uygun
- **`AppConfig.java`** — `@PropertySource(value="classpath:hibernate.properties", encoding="UTF-8")`
- **`WebConfig.java`** — Locale `Locale("tr","TR")` (i18n format)
- **Messages dosyaları**: `messages_tr.properties` → `messages_tr_TR.properties` (locale formatı)
- **`.gitignore`** — `hibernate.properties`, `*.pem`, `*.key`, `.my.cnf` eklendi (kimlik bilgileri korunsun)

### 🔴 Kaldırılanlar
- **H2 database dependency** — ders MySQL zorunlu kılıyor
- **`application.properties`** — Spring Boot stili, ders kuralına aykırı (`hibernate.properties` ile değiştirildi)
- **`hibernate-c3p0-jakarta`** girişimi (artifact yok, `hibernate-c3p0` yeterli)

### 📝 Notlar
- **Ders versiyon çelişkisi çözüldü**: Hoca "Hibernate 5.3.20" diyor ama Spring 6 jakarta zorunluluğuyla compile etmiyor → "Spirit" yorumla 5.6.15-jakarta seçildi (ders kuralı korundu, gerçeklikle uyumlu)
- **Paket migrasyonu yapılmadı**: Kullanıcı `com.stemsep` paketini koruma kararı aldı; ders öneri olan `tr.edu.duzce.mf.bm.bm470` projeye özel olduğu için bağlayıcı sayılmadı
- **MySQL bağlantı altyapısı**: Bastion 3 saatte expire — yeni session her seferinde Console'dan açılır
- **Build komutu**: `JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home mvn clean compile`

---

## [0.4.1] — 2026-04-26 — Hibernate Geri Dönüş + Spring MVC Düzeltme + Tomcat E2E ✅

### 🔵 Düzeltilenler
- **Hibernate 5.6.15-jakarta → 6.1.7.Final geri alındı**: Spring 6 `SpringSessionContext` `JtaPlatform.retrieveTransactionManager()` çağrısı `jakarta.transaction.TransactionManager` bekliyor ama Hibernate 5.6-jakarta JtaPlatform SPI'sında hâlâ `javax.transaction` kalıntısı var → `NoSuchMethodError` ve servlet context fail. Hibernate 6'da bu sorun yok (jakarta-native).
- **Spring MVC controller scan**: Controllers root context'e düşüyordu, DispatcherServlet servlet context'inde göremiyordu → tüm sayfalarda 404. Düzeltme:
  - `AppConfig`: `excludeFilters` ile `@Controller` + `@ControllerAdvice` hariç tutuldu
  - `WebConfig`: `@ComponentScan(basePackages = "com.stemsep.controller")` eklendi
- **Cargo Tomcat port 8080 → 8090**: Lokal'de başka projeden node app 8080'i tutuyordu

### 🟢 Eklenenler
- **Tomcat End-to-End test başarılı**: `mvn cargo:run` → `http://localhost:8090/stemsep/` → tüm sayfalar HTTP 200
  - `/stemsep/` (home): 200, 15239 byte
  - `/stemsep/upload`: 200, 15066 byte
  - `/stemsep/history`: 200, 11051 byte
- **OracleMySQLConnectionTest** Hibernate 6.1.7 ile yeniden doğrulandı: 3/3 PASS

### 📝 Notlar — Ders Kuralı Sapması
- **Ders "Hibernate 5.x" zorunluluğu** gerçek dünyada Spring 6.0.4 ile uyumsuz
- Hocaya savunma: ders dökümanı `ders7-8-9.md` §11 zaten "Versiyon Çelişkileri" başlığı altında bu sorunu işaret ediyor; bizim çözüm de işaret edilen gerçeklik üzerine pragmatik karar
- **Yine de korunan ders kuralları**: log4j 1.2.14, JUnit 4.13.1, MySQL Driver 8.0.28, Lombok, `hibernate.properties` formatı, `AvailableSettings.*` sabitleri, c3p0 tam config, locale tr_TR, paket yapısı

---

## [Sonraki] — Planlanıyor

### 📋 Faz 2.1: Tomcat End-to-End Test
- [ ] `mvn cargo:run` ile lokal Tomcat deploy
- [ ] JSP view'ların uçtan uca testi (Oracle MySQL ile)
- [ ] Stem indirme & ZIP export testi

### 📋 Faz 3: Sunucu & GPU Bağlantısı
- [ ] Oracle Cloud Compute VM (Java backend)
- [ ] Oracle Cloud GPU instance (Demucs Flask)
- [ ] systemd service tanımı (`docs/guidelines/DEPLOYMENT_GUIDE.md` §3)
- [ ] WAR'ı production'a taşıma + `hibernate.properties` private IP'ye çevrim
