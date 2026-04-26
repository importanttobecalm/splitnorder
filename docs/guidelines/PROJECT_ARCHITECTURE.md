# Proje Mimarisi

> **Ders:** BM470 İleri Java Programlama — Doç. Dr. Talha KABAKUŞ
> **Üniversite:** Düzce Üniversitesi MF BM
> **Proje Paket Kökü:** `tr.edu.duzce.mf.bm.bm470`

## Ders Gereksinimleri (Bağlayıcı)

Bu proje **Spring Boot KULLANMIYOR** — saf Spring Framework + Hibernate 5.x ile yapılandırılır.

### Teknoloji ve Versiyonlar (ders_kod_referansi.md §0)

| Bileşen | Versiyon |
|---------|----------|
| JDK | 18 |
| Spring Framework | 6.0.4 |
| Hibernate | 5.3.20.Final |
| c3p0 | 0.9.5.2 |
| MySQL Driver | 8.0.28 |
| SLF4J | 1.7.25 |
| log4j | 1.2.14 (log4j 1.x — log4j 2.x DEĞİL) |
| JUnit | 4.13.1 |
| AspectJ | 1.8.13 |
| Jakarta Servlet API | 6.1.0 |
| JSTL API | 3.0.0 |
| Lombok | 1.18.42 (provided scope) |
| Packaging | war |

> **NOT:** Mevcut proje (`com.stemsep`, Hibernate 6.1.7) ders gereksinimleriyle birebir uyumlu **değildir**. Ders teslimi için `tr.edu.duzce.mf.bm.bm470` paketine ve Hibernate 5.3.20'ye uyumlu hale getirilmesi gerekir. Bkz. `INTEGRATION_GUIDELINES.md`.

## Paket Yapısı (Zorunlu)

```
tr.edu.duzce.mf.bm.bm470
├── config/         WebConfig, AppConfig, WebAppInitializer
├── web/            Controller sınıfları (@Controller)
├── service/        Service sınıfları (@Service, @Transactional)
├── dao/            DAO sınıfları (@Repository)
├── interceptor/    HandlerInterceptor uygulamaları
├── exception/      Custom exception sınıfları
└── model/          Hibernate Entity sınıfları (POJO)
```

## 3 Katmanlı Mimari (ders7-8-9.md §7.1)

```
HTTP İsteği
    ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Controller  │ ──► │   Service    │ ──► │     DAO      │ ──► │  Veritabanı  │
│  (@Controller)│     │  (@Service)  │     │ (@Repository)│     │   (MySQL)    │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
   Sunum Katmanı       İş Katmanı         Veri Erişim Katmanı
```

- **Controller**: HTTP isteklerini karşılar (Sunum Katmanı)
- **Service**: İş süreçlerini yönetir, transaction yönetimi (@Transactional) burada (İş Katmanı)
- **DAO**: Hibernate Session/CriteriaBuilder ile DB erişimi (Veri Erişim Katmanı)
- **Model (Entity)**: POJO — `@Entity`, `@Table`, `@Column`, `@Id`, `@GeneratedValue`

## Splitnorder Domain'i (Müzik Ayrımı)

Mevcut domain bileşenleri ders mimarisine **şu eşlemeyle** taşınır:

| Mevcut (`com.stemsep`) | Ders Karşılığı (`tr.edu.duzce.mf.bm.bm470`) |
|------------------------|---------------------------------------------|
| `model.User`           | `model.Kullanici` (entity, POJO)            |
| `model.Job`            | `model.Is` (entity)                         |
| `model.Stem`           | `model.SesParcasi` (entity)                 |
| `dao.JobDao`           | `dao.IsDAO` (@Repository, CriteriaBuilder)  |
| `service.JobService`   | `service.IsService` (@Service, @Transactional) |
| `controller.UploadController` | `web.YuklemeController` (@Controller)|

## Veri Akışı (Müzik Ayrım Özelinde)

```
Frontend (JSP)
    ▼ multipart/form-data
YuklemeController (@Controller)
    ▼ MultipartFile
IsService (@Service, @Transactional)
    ├──► IsDAO (@Repository) → MySQL (Hibernate Session)
    └──► DemucsClient (Flask API: localhost:5000)
              ▼
        Audio Service (Python Flask + Demucs)
              ▼
        WAV stems döner
```

## i18n (ders gereksinimi)

`src/main/resources/` altında zorunlu:
- `messages_tr_TR.properties` (Türkçe)
- `messages_en_US.properties` (İngilizce)

`@PropertySource(encoding = "UTF-8")` her zaman.

## Loglama (ders7-8-9.md §9)

- **slf4j-api 1.7.25** + **log4j 1.2.14** (log4j 2.x DEĞİL)
- `log4j.properties` classpath'te (`src/main/resources/`)
- Her `@Service`/`@Repository`/`@Controller` sınıfında:
  ```java
  private static final Logger logger = LoggerFactory.getLogger(SinifAdi.class);
  ```
- Her HTTP isteği `RequestLoggingInterceptor` ile loglanır

## Güncellemeler

- **2026-04-25**: Ders dökümanları (`ders7-8-9.md`, `ders_kod_referansi.md`) entegre edildi. Mimari ders gereksinimlerine bağlı tutulacak.
- **2026-04-22**: M3 8GB Mac lokal test altyapısı eklendi.
