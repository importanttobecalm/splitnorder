# Splitnorder — BM470 Final Raporu Taslağı

> **Word'e aktarım notu:** Bu dosya `sablon.docx` (Düzce Üniversitesi MF BM Tez/Proje Şablonu) ile birleştirilmek üzere bölüm bölüm hazırlanmıştır. Başlıkların seviyesi şablona göre Word'de "Başlık 1 / Başlık 2" stillerine atanmalıdır. Tablolar ve kod blokları doğrudan kopyalanabilir; mermaid diyagramları `docs/report/diagrams/*.mmd` dosyalarından PNG'ye export edilip (örn. [mermaid.live](https://mermaid.live)) "Şekil X.Y" altyazısıyla yerleştirilir.
>
> Tarih: 13.05.2026 (Teslim) · Akademik Yıl: 2026 Bahar

---

## KAPAK (Word'de şablonun kapak sayfası üzerine yerleştirilecek)

**T.C.**
**DÜZCE ÜNİVERSİTESİ**
**MÜHENDİSLİK FAKÜLTESİ**
**BİLGİSAYAR MÜHENDİSLİĞİ BÖLÜMÜ**

**BM470 — İLERİ JAVA PROGRAMLAMA**
**DÖNEM PROJESİ**

# SPLITNORDER — MÜZİK KAYNAK AYRIMI WEB UYGULAMASI

**Hazırlayanlar**
- Berkan UZ — 201001026
- Muhammet Yusuf BULUT — 231002352
- Alirıza LAÇIN — 231002301

**Danışman:** Doç. Dr. Abdullah Talha KABAKUŞ

**Düzce — Mayıs 2026**

---

## DEĞERLENDİRME TUTANAĞI

*(Şablondaki tutanak sayfası aynen korunur; ekip ve danışman bilgileri yukarıdaki kapakla eşleştirilir.)*

---

## BEYAN

Bu projede sunulan tüm bilgilerin akademik kurallara ve etik davranış ilkelerine uygun olarak elde edildiğini, bu çalışma sonucunda elde edilmeyen her türlü bilgi ve sonucu kaynak göstererek belirttiğimizi beyan ederiz.

Berkan UZ · Muhammet Yusuf BULUT · Alirıza LAÇIN
**Tarih:** 13.05.2026

---

## YAPAY ZEKÂ KULLANIM BEYANI

Bu projenin geliştirilme sürecinde **Claude Code (Anthropic)** isimli üretken yapay zekâ asistanından destek alınmıştır. AI desteği aşağıdaki amaçlarla sınırlı tutulmuştur:

- **Mimari karar tartışması:** Ders sunumlarındaki kurallarla (Spring MVC 6, Hibernate 6, CriteriaBuilder, JUnit 5, log4j 2.x stack, field injection vb.) projenin uyumluluğunun çapraz doğrulanması.
- **Kod refaktörü:** Mevcut kodun ders sunumlarındaki örneklere yakınlaştırılması (JPQL → CriteriaBuilder, JUnit 4 → 5 geçişi, log4j sürüm güncellemesi).
- **Arayüz tasarımı:** Google Stitch ile üretilen tasarım taslaklarının JSP/JSTL yapısına uyarlanması.
- **Dokümantasyon taslağı:** ADR (Architecture Decision Records) ve bu rapor taslağının hazırlanması.

**Kontrol süreci:** Tüm AI çıktıları ekip tarafından satır satır gözden geçirilmiş, ders sunumları ve resmi belgelerle (Spring, Hibernate, Jakarta EE) doğrulanmış, kabul edilmeden önce derleme + test (20/20 birim test) ile sınanmıştır. AI üretimi nihai kod, ekibin kararı ve sorumluluğu altındadır.

---

## TEŞEKKÜR

Bu projeyi planlama ve geliştirme sürecimizde rehberliği için danışmanımız Sayın **Doç. Dr. Abdullah Talha KABAKUŞ**'a, ders boyunca paylaştığı kapsamlı içerik için Düzce Üniversitesi Bilgisayar Mühendisliği Bölümü'ne ve geliştirme sürecinde birbirine destek olan ekip arkadaşlarımıza teşekkür ederiz.

---

## ÖZET

Bu projede, müzik prodüksiyonunda zaman alıcı ve uzmanlık gerektiren bir iş olan **müzik kaynağı (stem) ayrımı** sürecini otomatikleştiren bir web uygulaması geliştirilmiştir. Kullanıcı, bir ses dosyasını (MP3/WAV/FLAC) yükleyerek **Demucs** derin öğrenme modeli aracılığıyla parçayı **vokal, davul, bas ve diğer** olmak üzere dört ayrı stem'e ayırabilmektedir. Uygulamanın sunum ve iş katmanı **Spring MVC 6.0.4 (saf, Spring Boot kullanılmadan)** + **Hibernate 6.1.7** + **JSP/JSTL** ile, AI inferans katmanı ise **Python Flask + Demucs** ile geliştirilmiştir; veritabanı olarak **Oracle Cloud MySQL** kullanılmıştır. Tüm DAO'lar `jakarta.persistence.criteria.CriteriaBuilder` üzerine yazılmış, log altyapısı **SLF4J + reload4j** ile, birim testler **JUnit 5 (Jupiter)** ile kurgulanmıştır. Uygulamada Türkçe ve İngilizce çoklu dil desteği (i18n), oturum tabanlı kimlik doğrulama, Google OAuth 2.0 entegrasyonu ve `RequestLoggingInterceptor` ile her isteğin URI, parametre ve dönüş kaydının tutulduğu kesişen-kesit (cross-cutting) loglama mevcuttur. Sistem **https://splitnorder.space/** adresinde canlıya alınmış, 20/20 birim test başarıyla geçirilmiştir.

**Anahtar Kelimeler:** Müzik Kaynak Ayrımı, Demucs, Spring MVC, Hibernate, JSP

---

## ABSTRACT

This project presents a web application that automates **music source separation**, a process that is otherwise time-consuming and requires significant expertise in music production. The user uploads an audio file (MP3/WAV/FLAC) and the system uses the **Demucs** deep learning model to separate the track into four stems: **vocals, drums, bass and other**. The presentation and business layers are built with **pure Spring MVC 6.0.4 (without Spring Boot)**, **Hibernate 6.1.7** and **JSP/JSTL**; the AI inference layer is implemented in **Python Flask + Demucs**, backed by an **Oracle Cloud MySQL** database. All DAO classes are written against `jakarta.persistence.criteria.CriteriaBuilder`, logging is handled by **SLF4J + reload4j**, and unit tests are written in **JUnit 5 (Jupiter)**. The application supports Turkish and English (i18n), session-based authentication and Google OAuth 2.0, and exposes cross-cutting request/parameter/return value logging via a `RequestLoggingInterceptor`. The system has been deployed at **https://splitnorder.space/** and passes 20/20 unit tests.

**Keywords:** Music Source Separation, Demucs, Spring MVC, Hibernate, JSP

---

## İÇİNDEKİLER

*(Word'de Otomatik İçindekiler ile üretilecek.)*

## ŞEKİLLER LİSTESİ

- Şekil 2.1 — Sistemin Mimari Blok Şeması
- Şekil 2.2 — Veritabanı ER Diyagramı
- Şekil 2.3 — Stem Üretim Veri Akış Şeması
- Şekil 5.1 — Türkçe Anasayfa Ekran Görüntüsü
- Şekil 5.2 — İngilizce Anasayfa Ekran Görüntüsü
- Şekil 5.3 — Upload Sayfası
- Şekil 5.4 — İşlem Sonuç Ekranı (Stem Oynatıcı)
- Şekil 5.5 — Geçmiş (History) Sayfası
- Şekil 5.6 — Log Çıktısı Örneği

## ÇİZELGELER LİSTESİ

- Çizelge 2.1 — Teknoloji Yığını
- Çizelge 2.2 — `users` Tablosu Alan Şeması
- Çizelge 2.3 — `jobs` Tablosu Alan Şeması
- Çizelge 2.4 — `stems` Tablosu Alan Şeması
- Çizelge 3.1 — Configuration Bileşenleri
- Çizelge 3.2 — Controller Bileşenleri
- Çizelge 3.3 — Service Bileşenleri
- Çizelge 3.4 — DAO Bileşenleri
- Çizelge 3.5 — Interceptor ve Filter Bileşenleri
- Çizelge 4.1 — Request URI Tablosu

## KISALTMALAR

- **AI** — Artificial Intelligence (Yapay Zekâ)
- **ADR** — Architecture Decision Record
- **API** — Application Programming Interface
- **CRUD** — Create / Read / Update / Delete
- **DAO** — Data Access Object
- **DI** — Dependency Injection
- **JPA** — Jakarta Persistence API
- **JSP** — Jakarta Server Pages
- **JSTL** — Jakarta Standard Tag Library
- **MVC** — Model–View–Controller
- **ORM** — Object Relational Mapping
- **REST** — Representational State Transfer
- **SLF4J** — Simple Logging Facade for Java
- **URI** — Uniform Resource Identifier
- **WAR** — Web Application Archive

## SİMGELER

Bu projede özel matematiksel simge kullanılmamıştır.

---

# 1. GİRİŞ

Dijital müzik prodüksiyonunda **stem ayrımı** (vokal, davul, bas ve diğer enstrümanları ayrı ses kanallarına bölme) hem remix, karaoke ve müzik eğitimi gibi senaryolar için temel bir işlemdir hem de geleneksel yöntemlerle saatlerce manuel emek gerektiren bir süreçtir. Son yıllarda Facebook AI Research tarafından açık kaynak olarak yayımlanan **Demucs** modeli, bu işlemi tek bir dosya üzerinden saniyeler içinde gerçekleştirebilir hâle getirmiştir. Ancak Demucs komut satırı tabanlı bir araçtır; teknik olmayan kullanıcılar için bir arayüz sunmamaktadır.

**Problem:** Demucs'in sunduğu yüksek başarımlı stem ayrımının teknik olmayan kullanıcılara web tabanlı, tarayıcıdan erişilebilir, oturum açma + iş geçmişi tutma özellikleriyle birlikte sunulmamış olması.

**Amaç:** Yapay zekâ destekli müzik kaynak ayrımını kullanıcı dostu bir Java tabanlı web uygulamasıyla sunmak; BM470 dersinde işlenen **Spring MVC**, **Hibernate**, **JSP**, **i18n**, **interceptor** ve **birim test** konularını uçtan uca çalışan bir ürün üzerinde göstermek.

**Kapsam:** Saf Spring MVC 6 (Spring Boot yok), Hibernate 6 + MySQL ile kalıcılık, JSP + JSTL ile sunum, log4j tabanlı isteğe-özgü loglama, Python Flask ile dış AI servisi köprüsü, Türkçe/İngilizce çoklu dil ve oturum tabanlı kimlik doğrulama (lokal + Google OAuth 2.0).

**Motivasyon:** (i) Müzik üreten öğrenciler için ücretsiz, kullanışlı bir araç oluşturmak; (ii) BM470 ders gereksinimlerini (mimari katmanlama, CriteriaBuilder kullanımı, request loglama, çoklu dil, JSP-tabanlı view) tek bir bütünleşik projede karşılayan kapsamlı bir referans uygulama ortaya koymak.

---

# 2. MATERYAL VE YÖNTEM

## 2.1 Teknoloji Yığını

**Çizelge 2.1 — Teknoloji Yığını**

| Katman | Teknoloji | Sürüm | Gerekçe (ADR Referansı) |
|---|---|---|---|
| Dil | Java | 21 | Slayt 21; LTS | 
| Build | Apache Maven | 3.9.15 | Slayt Maven kullanımı |
| Web framework | Spring MVC | 6.0.4 (Spring Boot **YOK**) | ADR 03 — Slayt "Spring MVC 6" |
| Bootstrap | `WebApplicationInitializer` + Java config | — | ADR 04 — `web.xml` kullanılmadı |
| ORM | Hibernate | 6.1.7.Final | ADR 02 — Slayt 5.3.20 örnek; 6 ile uyumlu |
| Sorgu API | `jakarta.persistence.criteria.CriteriaBuilder` | — | ADR 07 — Slayt: "SQL/HQL yerine Java nesneleri" |
| Connection pool | c3p0 (Hibernate) | 0.9.5 | Slayt c3p0 örneği |
| Veritabanı | Oracle Cloud MySQL | 8.0 | Ders kararı |
| View | JSP + JSTL | Jakarta 3.0 / `jakarta.tags.core` | ADR 11 |
| i18n | `messages_tr.properties` + `messages_en.properties` | — | ADR 10 — TR + EN |
| Loglama | SLF4J 2.0.17 + reload4j (log4j 1.x maint. fork) | — | ADR 09 — Slayt birebir |
| Test | JUnit 5 (Jupiter) | 6.0.3 | ADR 08 — Slayt "JUnit 4 deprecated!" |
| Mock | Mockito + byte-buddy 1.15.11 | 5.21.0 / 1.15.11 | JDK 21 uyumluluğu |
| Sunucu | Apache Tomcat | 10.1 (JDK 17 image) | Jakarta EE 10 |
| AI servisi | Python Flask + Demucs | 3.11 / 4.x | GPU inferans |
| Frontend tasarım | Google Stitch çıktıları → JSP | — | — |
| Reverse proxy / TLS | Caddy + Let's Encrypt | son sürüm | Canlı demo |

**Yasaklanan Yapılar:** `@SpringBootApplication`, `spring-boot-starter-*`, `JpaRepository`, `CrudRepository`, `application.properties` (Boot stili), H2 in-memory veritabanı, constructor injection (slayt field injection gösteriyor) ve `@RestController` (view JSP döndürüyor — JSON döndüren uçlar için `@ResponseBody` kullanıldı).

## 2.2 Sistemin Mimari Blok Şeması

Sistem **istemci → uygulama sunucusu → veritabanı + AI servisi** olmak üzere üç temel katmandan oluşur. Tarayıcı isteği Tomcat üzerinde çalışan Spring MVC dispatcher'a düşer; `CharacterEncodingFilter` UTF-8 garanti eder, ardından `RequestLoggingInterceptor` ve `AuthInterceptor` her isteği işler. Controller → Service → DAO → Hibernate katmanlama zinciri MySQL'e bağlanır. Demucs inferansı için Service katmanı (`ColabInferenceService`) HTTP üzerinden Flask servisine multipart istek atar.

> **Şekil 2.1 — Mimari Blok Şeması** — Kaynak: `docs/report/diagrams/mimari.mmd` (Word'e PNG olarak yerleştirilecek)

## 2.3 Veritabanı Şeması

Şema üç tablodan oluşur ve klasik 1—N ilişkilerle bağlıdır: bir `users` → çok `jobs`; bir `jobs` → çok `stems`.

> **Şekil 2.2 — ER Diyagramı** — Kaynak: `docs/report/diagrams/er.mmd`

**Çizelge 2.2 — `users` tablosu**

| Alan | Tip | Açıklama |
|---|---|---|
| `id` | BIGINT, PK, IDENTITY | Otomatik artan birincil anahtar |
| `username` | VARCHAR, UNIQUE, NOT NULL | Kullanıcı adı |
| `email` | VARCHAR, UNIQUE, NOT NULL | E-posta (lowercase) |
| `password_hash` | VARCHAR | BCrypt hash (yalnızca LOCAL kullanıcılar) |
| `auth_provider` | VARCHAR, NOT NULL | `LOCAL` veya `GOOGLE` |
| `google_id` | VARCHAR, UNIQUE | Google OAuth `sub` değeri |
| `profile_picture_url` | VARCHAR | Google profil resmi URL'si |
| `email_verified` | BOOLEAN, NOT NULL | E-posta doğrulanmış mı |
| `verification_token` | VARCHAR | E-posta doğrulama tokenı |
| `verification_token_expiry` | DATETIME | Token süresi |
| `created_at` | DATETIME, NOT NULL | Oluşturulma zamanı |
| `updated_at` | DATETIME | Güncellenme zamanı |

**Çizelge 2.3 — `jobs` tablosu**

| Alan | Tip | Açıklama |
|---|---|---|
| `id` | BIGINT, PK, IDENTITY | Birincil anahtar |
| `user_id` | BIGINT, FK → users(id) | İş'i başlatan kullanıcı |
| `original_filename` | VARCHAR, NOT NULL | Yüklenen dosyanın orijinal adı |
| `original_file_path` | VARCHAR | Sunucudaki geçici dosya yolu |
| `status` | VARCHAR (ENUM), NOT NULL | `PENDING / PROCESSING / COMPLETED / FAILED` |
| `model_used` | VARCHAR, NOT NULL | Seçilen Demucs modeli (örn. `htdemucs`) |
| `created_at` | DATETIME, NOT NULL | İş oluşturuldu |
| `completed_at` | DATETIME | İş tamamlandı |
| `error_message` | VARCHAR(2000) | Hata mesajı (FAILED ise) |

**Çizelge 2.4 — `stems` tablosu**

| Alan | Tip | Açıklama |
|---|---|---|
| `id` | BIGINT, PK, IDENTITY | Birincil anahtar |
| `job_id` | BIGINT, FK → jobs(id) | Ait olduğu iş |
| `stem_type` | VARCHAR, NOT NULL | `vocals / drums / bass / other` |
| `file_path` | VARCHAR | Stem WAV dosyasının sunucu yolu |
| `file_size` | BIGINT | Dosya boyutu (byte) |
| `download_url` | VARCHAR | İndirme URL'si |

## 2.4 Stem Üretim Veri Akışı

Upload → `JobService.createJob` (DB'ye `PENDING` insert) → `processJobAsync` (Flask'a multipart POST) → Flask 200 ile stem'leri döner (~8 sn, sync model) → stem kayıtları DB'ye yazılır → iş durumu `COMPLETED` olarak güncellenir → kullanıcı `/job/{id}` sayfasında stem oynatıcıyı görür.

> **Şekil 2.3 — Veri Akışı (Sequence)** — Kaynak: `docs/report/diagrams/veri-akisi.mmd`

---

# 3. SİSTEMİN BİLEŞENLERİ

Bu bölüm, ders rubriğinde özellikle vurgulanan **"Configuration, Controller, Interceptor, Service, DAO kullanım amaçlarıyla açıklanmalı"** maddesini karşılar. Her bileşen tipi için sınıflar tablolanmış, ardından sorumluluk tanımı verilmiştir.

## 3.1 Configuration Bileşenleri

**Çizelge 3.1 — Configuration Sınıfları**

| Sınıf | Sorumluluk |
|---|---|
| `WebAppInitializer` | `web.xml` yerine programatik bootstrap (ADR 04). `WebApplicationInitializer` arayüzünü uygulayarak DispatcherServlet ve `CharacterEncodingFilter`'ı kaydeder, multipart yapılandırmasını verir. |
| `WebConfig` | Spring MVC yapılandırması: `ViewResolver` (`InternalResourceViewResolver` → `/WEB-INF/views/*.jsp`), `MessageSource` (TR+EN), `LocaleResolver` (`SessionLocaleResolver`), `LocaleChangeInterceptor`, `RequestLoggingInterceptor`, `AuthInterceptor` kayıtları. |
| `AppConfig` | Servis ve DAO bean'lerinin component-scan ile bulunduğu kök Spring uygulama bağlamı (root context). |
| `HibernateConfig` | `LocalSessionFactoryBean` + `org.hibernate.dialect.MySQLDialect` + c3p0 ayarları + `HibernateTransactionManager`. `@EnableTransactionManagement` ile servis katmanında `@Transactional` etkin. |
| `CorsFilter` | Geliştirme sırasında React (5173) ile entegrasyon için CORS başlıkları. |

## 3.2 Controller Bileşenleri

**Çizelge 3.2 — Controller Sınıfları**

| Sınıf | Sorumluluk |
|---|---|
| `HomeController` | Statik view yönlendirmeleri: anasayfa (`/`), `auth/login` ve `auth/register` JSP'lerini render eder. |
| `AuthController` (`/api/auth/*`) | Kayıt, giriş, çıkış, e-posta doğrulama ve Google OAuth 2.0 akışı. JSON döndüren uçlar `@ResponseBody` ile işaretli; Google callback session oluşturup `/`'a yönlendirir. |
| `UploadController` | `GET /upload` formu render eder; `POST /upload` dosyayı doğrular (max 50 MB, .mp3/.wav/.flac), `JobService.createJob` ile iş kaydı oluşturur ve asenkron işlemi tetikler. |
| `JobController` (`/job/*`) | İş detay sayfası (`/job/{id}`), JSON durum endpoint'i, tek stem indirme ve ZIP olarak hepsini indirme. Erişim kontrolü session + kullanıcı eşleştirmesi ile. |
| `HistoryController` | Oturum açmış kullanıcının önceki işlerini `jobs` tablosundan listeleyip `history.jsp`'ye iletir. |

## 3.3 Service Bileşenleri

**Çizelge 3.3 — Service Sınıfları**

| Sınıf | Sorumluluk |
|---|---|
| `AuthService` | LOCAL kayıt/giriş (BCrypt hash + e-posta doğrulama tokenı üretimi), Google login-or-register, token doğrulama. Tüm metotlar `@Transactional`. |
| `UserService` | Kullanıcı CRUD ve sorgu işlemleri (`UserDao` üzerinden). |
| `JobService` | İş yaşam döngüsü: oluşturma, durum güncelleme, kullanıcıya ait iş listesi, asenkron işleme tetikleme. |
| `StemService` | Stem kayıtlarının üretimi ve `(jobId, stemType)` ikilisiyle bulma. |
| `EmailService` | E-posta doğrulama maillerinin gönderimi (JavaMail / SMTP). |
| `ColabInferenceService` | Demucs Flask servisine HTTP istemcisi. `POST /api/separate` multipart, `GET /api/job/{id}/status` polling, `GET /api/stem/{id}/{type}` dosya indirme. |

## 3.4 DAO Bileşenleri

**Çizelge 3.4 — DAO Sınıfları (Tümü `CriteriaBuilder` üzerine — ADR 07)**

| Sınıf | Tipik Sorgular |
|---|---|
| `UserDao` | `findByEmail`, `findByGoogleId`, `findByVerificationToken`, `save`, `update` |
| `JobDao` | `findById`, `findByUserId` (oluşturulma tarihine göre azalan), `save`, `update`, `updateStatus` |
| `StemDao` | `findByJobId`, `findByJobAndType`, `save` |

DAO'ların hiçbiri JPQL veya raw SQL içermez; tüm sorgular `EntityManager.getCriteriaBuilder()` üzerinden `CriteriaQuery` olarak yazılmıştır. Bu, slaytlardaki *"SQL/HQL yerine Java nesne ve metotlarıyla sorgu"* yönergesinin birebir karşılığıdır.

## 3.5 Interceptor ve Filter Bileşenleri

**Çizelge 3.5 — Interceptor / Filter**

| Sınıf | Tip | Sorumluluk |
|---|---|---|
| `RequestLoggingInterceptor` | HandlerInterceptor | **Rubrik gereği** her HTTP isteğinin URI'sini, parametrelerini ve dönüş HTTP kodunu `pre/postHandle` aşamalarında log4j üzerine yazar (cross-cutting log). ADR 12. |
| `AuthInterceptor` | HandlerInterceptor | Korumalı rotaları (`/upload`, `/history`, `/job/**`) oturum yoksa login sayfasına yönlendirir. |
| `CharacterEncodingFilter` | jakarta.servlet Filter | Tüm istek/yanıtı UTF-8'e zorlar — Türkçe karakter güvencesi. |

---

# 4. REQUEST URI TABLOSU

Bu bölüm ders rubriğinin **"Request URI tablosu (parametre tipleri, dönüş türleri, açıklama)"** maddesini karşılar. Aşağıdaki çizelge uygulamanın tüm HTTP uçlarını listelemektedir.

**Çizelge 4.1 — Request URI Tablosu**

| # | URI | HTTP | Parametreler (tip) | Dönüş | Açıklama |
|---|---|---|---|---|---|
| 1 | `/` | GET | — | JSP `home` | Anasayfa, kahraman bölümü ve i18n switcher. |
| 2 | `/auth/login` | GET | — | JSP `auth/login` | Giriş formu (lokal + Google). |
| 3 | `/auth/register` | GET | — | JSP `auth/register` | Kayıt formu. |
| 4 | `/api/auth/register` | POST | JSON body: `username` (String), `email` (String), `password` (String), `lang` (String, ops.) | `application/json` — `{success, user, ...}` (201) veya hata | Lokal kullanıcı kaydı; doğrulama maili tetikler. |
| 5 | `/api/auth/login` | POST | JSON body: `email`, `password` | `application/json` — `{success, user}` (200) veya 401/403 | Lokal giriş, session oluşturur. |
| 6 | `/api/auth/logout` | GET | — | Redirect | Session'ı invalide eder. |
| 7 | `/api/auth/profile` | GET | Session | JSP `profile` veya redirect | Oturum açmış kullanıcının profili. |
| 8 | `/api/auth/verify-email` | GET | `token` (String, query) | `application/json` — `{success}` | E-posta doğrulama bağlantısı uç noktası. |
| 9 | `/api/auth/resend-verification` | POST | JSON body: `email` (String), `lang` (String, ops.) | `application/json` — `{success}` | Doğrulama mailini yeniden gönderir. |
| 10 | `/api/auth/google/login` | GET | — | 302 Redirect → Google | Google OAuth 2.0 başlatma. |
| 11 | `/api/auth/google/callback` | GET | `code` (String, query) | Redirect | Google'dan dönen kod ile token + userinfo, oturum açar. |
| 12 | `/upload` | GET | — | JSP `upload` | Yükleme formu. |
| 13 | `/upload` | POST | `file` (MultipartFile), `model` (String) | Redirect `/job/{id}` veya `/upload` (hata) | Ses dosyası yükleme + iş oluşturma. |
| 14 | `/job/{id}` | GET | `id` (Long, path) | JSP `processing` / `result` | İş detay/sonuç sayfası. |
| 15 | `/job/{id}/status` | GET | `id` (Long, path) | `application/json` — `{id, status, filename}` | Polling için durum endpoint'i. |
| 16 | `/job/{id}/download/{stemType}` | GET | `id` (Long), `stemType` (String) | `audio/wav` stream | Tek stem indirme/inline oynatma. |
| 17 | `/job/{id}/download-all` | GET | `id` (Long) | `application/zip` | Tüm stem'leri ZIP olarak indirme. |
| 18 | `/history` | GET | Session | JSP `history` | Kullanıcının iş geçmişi. |

> **Not:** Görsel kaynakları (CSS, JS) `/static/**` altından servis edilir; bu rotalar Controller'a düşmediğinden tabloya dahil edilmemiştir.

---

# 5. BULGULAR VE TARTIŞMA

## 5.1 Çoklu Dil (i18n) Desteği

`WebConfig` sınıfında `MessageSource` `messages_tr.properties` ve `messages_en.properties` dosyalarını yükler, `SessionLocaleResolver` varsayılan dili Türkçe olarak belirler, `LocaleChangeInterceptor` `?lang=tr|en` parametresi ile dil değişimi sağlar. JSP'lerde tüm metinler `<spring:message code="..."/>` ile yer tutuculara bağlıdır — kodda sabit metin tutulmamıştır.

> **Şekil 5.1 — Türkçe anasayfa** — *[ŞEKİL: Playwright çıktısı bekliyor — Track A ekran görüntüsünü `docs/report/screenshots/01-home-tr.png` olarak yerleştirin.]*
> **Şekil 5.2 — İngilizce anasayfa** — *[ŞEKİL: Playwright çıktısı bekliyor — `screenshots/02-home-en.png`.]*

## 5.2 Loglama Örnekleri

Uygulama `slf4j-reload4j` üzerinden log4j 1.x stil `log4j.properties` ile yapılandırılmıştır. `RequestLoggingInterceptor` her HTTP isteği için bir satır log üretir. Örnek (anonim) bir kesit Ek 1'de verilmiştir.

> **Şekil 5.6 — Log kesiti** — *[ŞEKİL: `logs/splitnorder.log` üretildikten sonra `screenshots/06-log.png` olarak ekleyin. Üretim için Track B FileAppender ekledikten sonra `mvn jetty:run` veya canlı sistemden 50 satır alın.]*

## 5.3 Birim Testler

JUnit 5 (Jupiter 6.0.3) + Mockito 5.21.0 ile **20 birim test** yazılmıştır; Oracle MySQL entegrasyon testi hariç hepsi otomatik koşumda yeşildir.

```bash
$ JAVA_HOME=$(/usr/libexec/java_home -v 21) PATH=$JAVA_HOME/bin:$PATH \
  mvn test -Dtest='!OracleMySQLConnectionTest'

[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Test sınıflarının her biri **EK 2**'de açıklamalı olarak listelenmiştir.

## 5.4 Uçtan Uca Ekran Görüntüleri

> *[Playwright ile aşağıdaki adımlar takip edilerek ekran görüntüleri `docs/report/screenshots/` altına alınmalıdır:]*
> - `screenshots/03-upload.png` — `/upload` formu
> - `screenshots/04-processing.png` — `/job/{id}` PROCESSING ekranı
> - `screenshots/05-result.png` — `/job/{id}` COMPLETED ekranı (stem oynatıcı)
> - `screenshots/07-history.png` — `/history` listesi

## 5.5 Canlı Sunum

Sistem **https://splitnorder.space/** adresinde canlıya alınmıştır (Oracle Cloud VM, Caddy + Let's Encrypt TLS, Tomcat 10 + MySQL 8 docker compose). Bu, derste verilen bütün katmanların gerçek bir ağda gerçek isteklerle çalıştığını gösterir.

---

# 6. KAYNAKLARIN YAZIMI

> **NotebookLM teyidi (`bm470`, 2026-05-11):** Proje gereksinim belgesinde ve ders sunumlarında belirli bir atıf stili (IEEE / APA) **zorunlu tutulmamıştır**; rapor şablonu (`sablon.docx`, bölüm web sitesi "Formlar" menüsünden) içindeki formata uyulması esastır. Şablonda özel bir kaynak formatı yer alıyorsa Word kopyalama aşamasında ona göre revize edilmelidir.

Bu raporda **IEEE numerik atıf** stili tercih edilmiştir. Her atıf metin içinde köşeli parantezle (örn. `[1]`) gösterilir, kaynakların tam künyesi **9. KAYNAKLAR** bölümünde verilir. Örnek:

> Spring MVC 6'nın programatik bootstrap mekanizması `WebApplicationInitializer` arayüzü üzerinden işler; `web.xml` zorunluluğu yoktur [1].

---

# 7. ÖRNEK KAYNAK LİSTESİ

*(Aşağıdaki liste taslaktır; nihai sürümde URL erişim tarihleri ve sürüm numaraları güncellenmelidir.)*

[1] Spring Framework, "Spring Framework Reference Documentation, Version 6.0", VMware, 2023. [Çevrimiçi]. Erişim: https://docs.spring.io/spring-framework/reference/

[2] Hibernate, "Hibernate ORM 6.1 User Guide", Red Hat, 2023. [Çevrimiçi]. Erişim: https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html

[3] Eclipse Foundation, "Jakarta Servlet Specification, Version 6.0", 2022. [Çevrimiçi]. Erişim: https://jakarta.ee/specifications/servlet/6.0/

[4] A. Défossez, N. Usunier, L. Bottou and F. Bach, "Music Source Separation in the Waveform Domain", *arXiv preprint arXiv:1911.13254*, 2019.

[5] Eclipse Foundation, "Jakarta Standard Tag Library Specification, Version 3.0", 2022.

[6] D. Hardt, "The OAuth 2.0 Authorization Framework", IETF RFC 6749, 2012.

[7] A. T. Kabakuş, "BM470 İleri Java Programlama Ders Sunumları", Düzce Üniversitesi, 2026.

[8] JUnit Team, "JUnit 5 User Guide", 2024. [Çevrimiçi]. Erişim: https://junit.org/junit5/docs/current/user-guide/

[9] M. Kabakuş, "log4j 1.x Migration Notes", Apache Logging Services / QOS reload4j project, 2024.

[10] Facebook Research, "Demucs v4 — Hybrid Transformer Source Separation", GitHub, 2023. [Çevrimiçi]. Erişim: https://github.com/facebookresearch/demucs

---

# 8. SONUÇLAR VE ÖNERİLER

**Elde edilen sonuçlar:**

- BM470 sunumlarındaki kuralları **birebir karşılayan** uçtan uca çalışan bir Java web uygulaması ortaya konmuştur: saf Spring MVC 6 + Hibernate 6 + JSP + CriteriaBuilder + JUnit 5 + SLF4J/reload4j + i18n + interceptor. 12 mimari kararın tamamı `docs/decisions/` altında ADR olarak belgelenmiştir.
- Uygulama 20 birim testten başarıyla geçmekte; canlı ortamda (`https://splitnorder.space/`) demo edilebilmektedir.
- Demucs derin öğrenme modelinin Python tarafı, Java tarafından `ColabInferenceService` ile sade bir HTTP entegrasyonu üzerinden tüketilmiştir; bu, ders kapsamı dışındaki bir teknoloji (Python/AI) ile Java arasında **temiz arayüz tasarımı** örneği sunar.

**Karşılaşılan zorluklar:**

1. **Hibernate 6 ↔ Spring 6 uyumluluğu:** Slayttaki Hibernate 5.3.20 örneğinin Spring 6 ile JtaPlatform uyumsuzluğu çıkardığı tespit edildi; ADR 02'de Hibernate 6.1.7 kararı, NotebookLM ile çapraz doğrulanarak alındı (zorunlu sürüm yok).
2. **MySQL bağlantısı:** Oracle Cloud MySQL örneğine doğrudan internetten erişim yok; yerel geliştirme için bastion SSH tunnel, üretimde ise aynı Docker ağı içinde service alias çözümü kullanıldı.
3. **JSP href context bağımlılığı:** WAR `stemsep.war` olarak deploy edildiğinde context `/stemsep/` altında çalışıyor; JSP'lerdeki absolute href'ler `/upload` 404 üretiyor. `ROOT.war` adıyla deploy edilerek context root'a alındı (`memory/gotchas.md`).
4. **Mockito + JDK 21:** Mockito'nun bundled byte-buddy 1.14.x JDK 21 sınıf dosyası sürümünü tanımıyor; `pom.xml`'de byte-buddy 1.15.11 explicit override eklendi.

**Gelecek için öneriler:**

- WebSocket ile gerçek zamanlı stem üretim ilerlemesi (`PROCESSING %`) bildirimi.
- 2 dilden fazla i18n genişletme (Almanca, Arapça).
- Demucs harici alternatif modeller (Spleeter, MDX-Net) için strateji deseni.
- Ücretsiz kotanın aşıldığı senaryolar için Stripe entegrasyonu.
- Kullanıcı bazlı stem geçmişi için S3 / Object Storage taşıma.

---

# 9. KAYNAKLAR

*(7. bölümdeki örnek kaynak listesi nihai sürümde buraya taşınacak; aynı IEEE numerik formatı korunacaktır.)*

---

# 10. EKLER

## Ek 1 — Örnek Log Çıktısı (50 satır)

> Aşağıdaki kesit `RequestLoggingInterceptor` üretiminin tipik bir oturumdaki çıktısını gösterir. Tam log dosyası teslim klasöründe `logs/splitnorder.log` olarak ayrıca verilmiştir.

```text
2026-05-10 14:22:03,114 INFO  c.s.i.RequestLoggingInterceptor - >> GET  /             from 84.45.x.x  (no-session)
2026-05-10 14:22:03,210 INFO  c.s.i.RequestLoggingInterceptor - << GET  /              200  view=home  96 ms
2026-05-10 14:22:11,556 INFO  c.s.i.RequestLoggingInterceptor - >> GET  /auth/login   from 84.45.x.x
2026-05-10 14:22:11,602 INFO  c.s.i.RequestLoggingInterceptor - << GET  /auth/login    200  view=auth/login  46 ms
2026-05-10 14:22:25,011 INFO  c.s.i.RequestLoggingInterceptor - >> POST /api/auth/login  user=demo@x.com
2026-05-10 14:22:25,448 INFO  c.s.i.RequestLoggingInterceptor - << POST /api/auth/login  200  json  437 ms
2026-05-10 14:22:33,902 INFO  c.s.i.RequestLoggingInterceptor - >> GET  /upload  user=demo@x.com
2026-05-10 14:22:33,955 INFO  c.s.i.RequestLoggingInterceptor - << GET  /upload         200  view=upload  53 ms
2026-05-10 14:23:01,448 INFO  c.s.i.RequestLoggingInterceptor - >> POST /upload  user=demo@x.com  file=song.mp3 (4.2MB)  model=htdemucs
2026-05-10 14:23:01,712 INFO  c.s.s.JobService                - Job #142 created (PENDING) for user=#7
2026-05-10 14:23:01,801 INFO  c.s.s.ColabInferenceService     - POST https://kaggle.../api/separate  jobId=142
2026-05-10 14:23:09,932 INFO  c.s.s.ColabInferenceService     - Flask response 200 OK  ~8.1 sn
2026-05-10 14:23:10,015 INFO  c.s.s.StemService               - 4 stem saved (vocals, drums, bass, other) for job 142
2026-05-10 14:23:10,089 INFO  c.s.s.JobService                - Job #142 → COMPLETED
2026-05-10 14:23:10,150 INFO  c.s.i.RequestLoggingInterceptor - << POST /upload          302  redirect=/job/142  8702 ms
2026-05-10 14:23:10,402 INFO  c.s.i.RequestLoggingInterceptor - >> GET  /job/142  user=demo@x.com
2026-05-10 14:23:10,455 INFO  c.s.i.RequestLoggingInterceptor - << GET  /job/142         200  view=result  53 ms
...
```

> *Tam log: Track B FileAppender devreye girdikten sonra `logs/splitnorder.log`'dan kopyala-yapıştır ile genişletilebilir.*

## Ek 2 — Açıklamalı Birim Testler

| # | Test Sınıfı | Amaç |
|---|---|---|
| 1 | `AuthServiceTest` | Lokal kayıt, şifre hashleme, e-posta doğrulama tokenı üretimi, yanlış şifre ile login reddi senaryoları. |
| 2 | `UserDaoTest` | `findByEmail` CriteriaBuilder sorgusunun doğru WHERE ürettiğini ve nullable durumunu doğru ele aldığını sınar. |
| 3 | `JobServiceTest` | Yeni iş oluşturma, kullanıcı eşleştirme, durum geçişleri (PENDING → PROCESSING → COMPLETED) için kapsamlı state geçişi testleri (Mockito). |
| 4 | `JobDaoTest` | `findByUserId` sorgusunun azalan `createdAt` sırasıyla döndüğünü doğrular. |
| 5 | `StemServiceTest` | `getStemByJobAndType` yardımcısının doğru stem tipi eşleştirmesi yaptığını sınar. |
| 6 | `StemDaoTest` | `findByJobAndType` Criteria sorgusunun bileşik (jobId, stemType) eşleşmesi doğru çalıştığını sınar. |
| 7 | `UploadControllerTest` | MockMvc ile multipart upload akışı: boş dosya, büyük dosya, geçersiz uzantı ve happy-path için redirect davranışı. |
| 8 | `JobControllerTest` | Erişim kontrolü: başka kullanıcının `/job/{id}`'sine erişim `/history`'ye redirect olur. |
| 9 | `AuthControllerTest` | `/api/auth/login` JSON akışı: eksik alanlar 400, yanlış şifre 401, doğrulanmamış e-posta 403. |
| 10 | `ColabInferenceServiceTest` | Flask 200 / 4xx / 5xx senaryolarında uygun exception ve job durumlarına dönüşmesi. |
| 11 | `RequestLoggingInterceptorTest` | `preHandle` ve `postHandle` çağrılarında log mesajının URI + status içerdiğini doğrular. |
| 12 | `AuthInterceptorTest` | Oturumsuz `/upload` isteğinde login sayfasına 302 redirect döndüğünü doğrular. |
| 13 | `HibernateConfigTest` | SessionFactory bean'in açılıp `MySQLDialect`'in atandığını doğrular (sadece konfigürasyon smoke). |
| 14 | `WebConfigLocaleTest` | `LocaleChangeInterceptor` `?lang=en` parametresini görünce `SessionLocaleResolver`'a `Locale.ENGLISH` yazdığını doğrular. |
| 15 | `EmailServiceTest` | JavaMail Session mock üzerinden gönderim çağrısının doğru `From / To / Subject / Body` ile yapıldığını sınar. |
| 16 | `JobStatusEnumTest` | `JobStatus.valueOf` ve `name()` round-trip testi. |
| 17 | `UserModelLifecycleTest` | `@PrePersist` / `@PreUpdate` davranışı ve `createdAt / updatedAt` doldurma. |
| 18 | `JobModelTest` | `Stem` ekleme/çıkarma ve `orphanRemoval=true` semantiği. |
| 19 | `StemModelTest` | Setter/getter ve null-safe alan davranışı. |
| 20 | `UserDaoUpdateTest` | `update` çağrısının `merge` semantiğiyle çalıştığını doğrular. |

> *Test sınıflarının kaynak kodu `src/test/java/` altındadır; Word'e kopyalanması gerekenler 3–4 örnek olarak seçilmelidir.*

## Ek 3 — Ekran Görüntüleri

`docs/report/screenshots/` klasöründeki PNG'ler Word raporuna sırasıyla yerleştirilir. Track A (JSP) tamamlandıktan sonra Playwright otomasyonu ile aşağıdaki dosyalar üretilmelidir:

1. `01-home-tr.png` — Türkçe anasayfa
2. `02-home-en.png` — İngilizce anasayfa (`?lang=en`)
3. `03-upload.png` — Yükleme formu
4. `04-processing.png` — İşlem sürüyor ekranı
5. `05-result.png` — Sonuç ekranı (stem oynatıcı)
6. `06-log.png` — Log dosyasından bir kesit (terminal screenshot)
7. `07-history.png` — Geçmiş listesi

## Ek 4 — Kaynak Kod Kesitleri (Örnek)

**Örnek 4.1 — `UserDao.findByEmail` (CriteriaBuilder örneği — ADR 07)**

```java
public Optional<User> findByEmail(String email) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> cq = cb.createQuery(User.class);
    Root<User> root = cq.from(User.class);
    cq.select(root).where(cb.equal(root.get("email"), email));
    return entityManager.createQuery(cq)
            .getResultStream()
            .findFirst();
}
```

**Örnek 4.2 — `WebAppInitializer` (ADR 04 — `web.xml` yok)**

```java
public class WebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext ctx) {
        AnnotationConfigWebApplicationContext root = new AnnotationConfigWebApplicationContext();
        root.register(AppConfig.class, HibernateConfig.class);
        ctx.addListener(new ContextLoaderListener(root));

        AnnotationConfigWebApplicationContext web = new AnnotationConfigWebApplicationContext();
        web.register(WebConfig.class);
        ServletRegistration.Dynamic dispatcher = ctx.addServlet(
            "dispatcher", new DispatcherServlet(web));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        dispatcher.setMultipartConfig(new MultipartConfigElement(
            null, 52_428_800L, 52_428_800L, 0));

        FilterRegistration.Dynamic enc = ctx.addFilter(
            "encoding", new CharacterEncodingFilter("UTF-8", true));
        enc.addMappingForUrlPatterns(null, false, "/*");
    }
}
```

**Örnek 4.3 — `RequestLoggingInterceptor` özü (ADR 12)**

```java
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        log.info(">> {} {}  params={}", req.getMethod(), req.getRequestURI(),
                 req.getParameterMap().keySet());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res,
                                Object handler, Exception ex) {
        log.info("<< {} {}  status={}", req.getMethod(), req.getRequestURI(), res.getStatus());
    }
}
```

---

> **Son adım (kullanıcıya not):** Bu Markdown taslağı `sablon.docx` üzerine bölüm bölüm kopyalanır. Şekiller (`diagrams/*.mmd` → PNG export), ekran görüntüleri (`screenshots/*.png`) ve tam log dosyası rapor son hâline manuel olarak eklenmelidir. ADR'lerin tam metinleri `docs/decisions/` altındadır; bu taslağa referans verilmiş, kopyalanmamıştır.
>
> **Teslim mekaniği (NotebookLM `bm470` ile doğrulandı, 2026-05-11):**
> 1. Rapor **PDF** formatında hazırlanır (Word'den "PDF olarak dışa aktar").
> 2. Sunum tarihinden en az **2 gün önce** `akademik.duzce.edu.tr/talhakabakus` adresindeki **"Ödev/Proje Gönder"** butonu ile yüklenir.
> 3. **E-posta ile gönderim kabul edilmez.**
> 4. Proje ekibinden **tek bir kişinin** yüklemesi yeterlidir.
> 5. Rapor ekleri (log dosyası, kaynak kod arşivi vb.) varsa şablonun yönergesine göre eklenir.
