# AI StemSep (splitnorder)

Müzik dosyalarını (mp3, wav, flac) yapay zeka ile **vokal, davul, bas ve diğer** enstrüman stem'lerine ayıran Spring MVC tabanlı web platformu. BM470 dersi kapsamında geliştirilmiştir.

## Teknoloji Yığını

- **Backend:** Java 18, Spring MVC 6.0.4, Spring ORM
- **ORM:** Hibernate 6.1.7 + c3p0 connection pool
- **Veritabanı:** H2 (geliştirme) / MySQL 8 (production — Oracle Cloud HeatWave)
- **View:** JSP + JSTL (Jakarta), Bootstrap 5
- **Loglama:** log4j 1.2.14 + slf4j 1.7.25
- **Test:** JUnit 4.13.1 + Mockito
- **Servlet Container:** Tomcat 10.1.8 (Jakarta Servlet 6)
- **GPU Inference:** Google Colab + Ngrok (harici servis)
- **Build:** Maven (WAR packaging)

## Proje Yapısı

```
src/main/java/com/stemsep/
├── config/        — Spring, Hibernate, Web init (annotation-based)
├── controller/    — Home, Upload, Job, History
├── service/       — JobService, ColabInferenceService, StemService, UserService
├── dao/           — Hibernate SessionFactory tabanlı repository'ler
├── model/         — JPA entities (User, Job, Stem, JobStatus enum)
└── interceptor/   — RequestLoggingInterceptor
src/main/resources/
├── application.properties
├── log4j.properties
└── messages_{tr,en}.properties    — i18n
src/main/webapp/WEB-INF/views/    — home, upload, processing, result, history
```

## Ön Gereksinimler

- JDK 18
- Maven 3.9+
- Tomcat 10.1.8
- (Opsiyonel) MySQL 8 veya Oracle Cloud HeatWave
- (Opsiyonel) Google Colab notebook + Ngrok (GPU inference için)

## Kurulum

```bash
git clone https://github.com/importanttobecalm/splitnorder.git
cd splitnorder
mvn clean package
```

`target/stemsep.war` dosyasını Tomcat `webapps/` dizinine kopyalayın veya IDE'nizden deploy edin.

Uygulama: `http://localhost:8080/stemsep`

## Konfigürasyon

`src/main/resources/application.properties` dosyasını düzenleyin:

```properties
# Veritabanı (varsayılan: H2 in-memory)
db.driver=org.h2.Driver
db.url=jdbc:h2:mem:stemsepdb;DB_CLOSE_DELAY=-1
db.username=sa
db.password=

# Production: MySQL için yukarıyı yorum satırı yapıp aşağıyı açın
# db.driver=com.mysql.cj.jdbc.Driver
# db.url=jdbc:mysql://HOST:3306/stemsep?useSSL=true&serverTimezone=UTC

# Colab/Kaggle GPU API (Ngrok URL)
colab.api.url=http://localhost:5000
```

> ⚠️ **Güvenlik notu:** Veritabanı kimlik bilgilerini production'da environment variable'a taşıyın; plaintext olarak repo'ya commit etmeyin.

## Çalıştırma (Geliştirme)

Tomcat Maven plugin veya IDE entegrasyonu ile deploy edin. Akış:

1. `/` → Ana sayfa
2. `/upload` → Dosya yükleme (max 50 MB; mp3/wav/flac)
3. `/job/{id}` → İşlem durumu (AJAX polling ile otomatik güncellenir)
4. `/job/{id}/download/{stemType}` → Tek stem indirme
5. `/job/{id}/download-all` → Tüm stem'leri ZIP olarak indir
6. `/history` → Geçmiş işlemler (session tabanlı)

## Test

```bash
mvn test
```

Mevcut testler: `HomeControllerTest`, `JobServiceTest`, `ColabInferenceServiceTest`, `UserDaoTest`, `JobDaoTest`.

## Dil Desteği (i18n)

TR (varsayılan) ve EN. Değiştirmek için: `?lang=en` veya `?lang=tr` query parametresi (cookie'de saklanır).

## Bilinen Sorunlar ve Yol Haritası

Detaylı teknik inceleme için bkz. [PROJECT_REVIEW.md](./PROJECT_REVIEW.md). Öncelikli maddeler:

- [ ] **P0:** `HibernateConfig` Spring 6 + Hibernate 6 uyumluluğu (JPA'ya geçiş)
- [ ] **P1:** Colab entegrasyonunda gerçek dosya upload (şu an path gönderiliyor)
- [ ] **P1:** Session bazlı job erişim kontrolü
- [ ] **P2:** CSRF koruması, path traversal sıkılaştırması, credentials env'e taşıma
- [ ] **P3:** `colab_stemsep_server.py` notebook'unun repo'ya eklenmesi

## Mimari Planı

Detaylı faz faz uygulama planı için bkz. [implementation_plan.md](./implementation_plan.md).

## Lisans

MIT — bkz. [LICENSE](./LICENSE).
