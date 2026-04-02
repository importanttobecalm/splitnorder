# AI StemSep – Müzik Stem Ayırma ve İşleme Web Platformu

Kullanıcıların yüklediği müzik dosyalarından (mp3, wav, flac) vokal, davul, bas ve diğer enstrüman stem'lerini AI ile ayıran, tamamen web tabanlı Spring MVC platformu.

---

## Mevcut Sistem Durumu

| Araç | Durum |
|------|-------|
| Python 3.12 | ✅ Mevcut |
| pip 25.0.1 | ✅ Mevcut |
| winget 1.28 | ✅ Mevcut |
| JDK 18 | ❌ Kurulması gerekli |
| Maven 3.9.x | ❌ Kurulması gerekli |
| Git | ❌ Kurulması gerekli |
| Tomcat 10.1.8 | ❌ Kurulması gerekli |

---

## User Review Required

> [!IMPORTANT]
> **Veritabanı Bağlantı Bilgileri Gerekli**
> Oracle Cloud HeatWave MySQL veritabanı bilgilerinizi sağlamanız gerekiyor:
> - Host/IP adresi
> - Port
> - Database adı
> - Kullanıcı adı ve şifre
> 
> **Eğer henüz veritabanı oluşturmadıysanız**, geliştirme aşamasında lokal H2 veritabanı kullanabiliriz, sonra Oracle Cloud'a geçiş yapılır.

> [!WARNING]
> **Google Colab Ngrok Entegrasyonu**
> Colab notebook'u ayrı bir adımda hazırlanacak. Colab tarafında bir Flask/FastAPI servisi çalıştırılıp Ngrok ile public URL alınacak. Bu URL'nin dinamik olması nedeniyle, uygulamada bir admin paneli ile URL güncellemesi yapılabilir veya `application.properties` dosyasından ayarlanabilir.

> [!IMPORTANT]
> **Ders Versiyonları – pom.xml Kontrol**
> PRD'de belirtilen zorunlu versiyonlar:
> - Spring MVC **6.0.4** (Spring 7 DEĞİL)
> - Hibernate **5.3.20.Final** (Hibernate 7 DEĞİL)
> - log4j **1.2.14** + slf4j **1.7.25** (log4j2 DEĞİL)
> - JUnit **4.13.1** (JUnit 5/6 DEĞİL)
> - c3p0 **0.9.5.2**
> - mysql-connector-j **8.0.28**
> - Jakarta Servlet API 6.0 (Tomcat 10.1.x uyumu)

---

## Proposed Changes

### Faz 0: Ortam Kurulumu

winget ile gerekli araçları kuracağız:

```powershell
# JDK 18 (Oracle OpenJDK)
winget install --id Oracle.JDK.18 --accept-source-agreements --accept-package-agreements

# Maven 3.9.x
winget install --id Apache.Maven --accept-source-agreements --accept-package-agreements

# Git
winget install --id Git.Git --accept-source-agreements --accept-package-agreements
```

Apache Tomcat 10.1.8 manuel indirilip workspace içine yerleştirilecek.

---

### Faz 1: Maven Proje İskeleti

#### Proje Dizin Yapısı
```
splicenorder/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/stemsep/
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java          — Spring ana config (annotation-based)
│   │   │   │   ├── WebConfig.java          — Spring MVC config (ViewResolver, Interceptor, i18n)
│   │   │   │   ├── WebAppInitializer.java  — Servlet 6.0 initializer (web.xml yerine)
│   │   │   │   └── HibernateConfig.java    — Hibernate + c3p0 config
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── UploadController.java
│   │   │   │   ├── JobController.java
│   │   │   │   └── HistoryController.java
│   │   │   ├── interceptor/
│   │   │   │   └── RequestLoggingInterceptor.java
│   │   │   ├── service/
│   │   │   │   ├── JobService.java
│   │   │   │   ├── StemService.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── ColabInferenceService.java
│   │   │   ├── dao/
│   │   │   │   ├── JobDao.java
│   │   │   │   ├── StemDao.java
│   │   │   │   └── UserDao.java
│   │   │   └── model/
│   │   │       ├── User.java
│   │   │       ├── Job.java
│   │   │       └── Stem.java
│   │   ├── resources/
│   │   │   ├── log4j.properties
│   │   │   ├── messages_tr.properties
│   │   │   ├── messages_en.properties
│   │   │   └── application.properties
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── views/
│   │               ├── home.jsp
│   │               ├── upload.jsp
│   │               ├── processing.jsp
│   │               ├── result.jsp
│   │               └── history.jsp
│   └── test/
│       └── java/com/stemsep/
│           ├── controller/
│           │   └── HomeControllerTest.java
│           ├── service/
│           │   ├── JobServiceTest.java
│           │   └── ColabInferenceServiceTest.java
│           └── dao/
│               ├── UserDaoTest.java
│               └── JobDaoTest.java
```

---

### Faz 2: pom.xml (Zorunlu Versiyonlar)

#### [NEW] [pom.xml](file:///c:/Users/Importanttobecalm/Desktop/splicenorder/pom.xml)

Zorunlu bağımlılıklar:

| Dependency | Version |
|------------|---------|
| spring-webmvc | 6.0.4 |
| spring-orm | 6.0.4 |
| spring-context | 6.0.4 |
| hibernate-core | 5.3.20.Final |
| hibernate-c3p0 | 5.3.20.Final |
| c3p0 | 0.9.5.2 |
| mysql-connector-j | 8.0.28 |
| log4j | 1.2.14 |
| slf4j-api | 1.7.25 |
| slf4j-log4j12 | 1.7.25 |
| junit | 4.13.1 |
| jakarta.servlet-api | 6.0.0 (provided) |
| jstl (jakarta) | 3.0.0 |
| jackson-databind | 2.15.x (JSON API) |
| commons-fileupload2 | 2.0.x (dosya yükleme) |

> [!WARNING]
> **Uyumluluk Notu**: Hibernate 5.3.x javax.persistence kullanır, Tomcat 10.1.x ise jakarta namespace kullanır. Bu uyumsuzluk nedeniyle `hibernate-core-jakarta` 5.6.15.Final alternatif olarak değerlendirilebilir, ya da javax→jakarta bridge kullanılabilir. Sizin onayınızla en uygun çözümü seçeceğiz.

---

### Faz 3: Konfigürasyon Katmanı

#### [NEW] AppConfig.java
- `@Configuration`, `@ComponentScan`, `@EnableTransactionManagement`
- PropertySource ile `application.properties` yükleme

#### [NEW] WebConfig.java
- `@EnableWebMvc`
- InternalResourceViewResolver → `/WEB-INF/views/`, `.jsp`
- Interceptor registry (RequestLoggingInterceptor)
- MessageSource bean (i18n)
- LocaleResolver + LocaleChangeInterceptor
- MultipartResolver (dosya yükleme)

#### [NEW] WebAppInitializer.java
- `AbstractAnnotationConfigDispatcherServletInitializer` extend
- XML config olmadan servlet context başlatma

#### [NEW] HibernateConfig.java
- LocalSessionFactoryBean
- c3p0 connection pooling
- HibernateTransactionManager

---

### Faz 4: Model Katmanı (JPA/Hibernate Entities)

#### [NEW] User.java
```java
@Entity @Table(name = "users")
- id (Long, auto-generated)
- username (String, unique)
- email (String)
- passwordHash (String)
- createdAt (LocalDateTime)
- jobs (OneToMany → Job)
```

#### [NEW] Job.java
```java
@Entity @Table(name = "jobs")
- id (Long, auto-generated)
- user (ManyToOne → User)
- originalFilename (String)
- status (String: PENDING, PROCESSING, COMPLETED, FAILED)
- modelUsed (String: MDX-Net / HTDemucs_ft)
- createdAt (LocalDateTime)
- completedAt (LocalDateTime)
- stems (OneToMany → Stem)
```

#### [NEW] Stem.java
```java
@Entity @Table(name = "stems")
- id (Long, auto-generated)
- job (ManyToOne → Job)
- stemType (String: vocals, drums, bass, other)
- filePath (String)
- fileSize (Long)
- downloadUrl (String)
```

---

### Faz 5: DAO Katmanı

#### [NEW] UserDao.java, JobDao.java, StemDao.java
- Hibernate SessionFactory inject
- CRUD operasyonları
- `@Repository` annotation
- `@Transactional` yönetimi

---

### Faz 6: Service Katmanı

#### [NEW] JobService.java
- Dosya yükleme iş mantığı
- Job oluşturma ve durum yönetimi
- `@Service` annotation

#### [NEW] ColabInferenceService.java
- Colab Ngrok URL'e HTTP POST ile dosya gönderme
- Asenkron işlem yönetimi
- Stem sonuçlarını alma ve kaydetme

#### [NEW] UserService.java
- Kullanıcı işlemleri (basit session-based auth)

#### [NEW] StemService.java
- Stem dosyası yönetimi ve indirme

---

### Faz 7: Controller Katmanı

#### [NEW] HomeController.java
- `GET /` → home.jsp
- Dil değiştirme desteği

#### [NEW] UploadController.java
- `GET /upload` → upload.jsp (form)
- `POST /upload` → dosya yükleme + job oluşturma

#### [NEW] JobController.java
- `GET /job/{id}` → processing.jsp / result.jsp
- `GET /job/{id}/status` → JSON durum (AJAX polling)
- `GET /job/{id}/download` → ZIP indirme

#### [NEW] HistoryController.java
- `GET /history` → history.jsp (geçmiş işlemler)

---

### Faz 8: Interceptor ve Loglama

#### [NEW] RequestLoggingInterceptor.java
- `preHandle`: İstek URI, HTTP method, parametreler loglanır
- `afterCompletion`: Response status, işlem süresi loglanır

#### [NEW] log4j.properties
```properties
log4j.rootLogger=INFO, console, file
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.File=logs/bm470.log
```

---

### Faz 9: i18n (Çoklu Dil)

#### [NEW] messages_tr.properties
```properties
app.title=AI StemSep - Müzik Stem Ayırma
upload.title=Şarkı Yükle
upload.button=İşleme Başla
model.select=Model Seçin
...
```

#### [NEW] messages_en.properties
```properties
app.title=AI StemSep - Music Stem Separation
upload.title=Upload Song
upload.button=Start Processing
model.select=Select Model
...
```

---

### Faz 10: JSP Views (Bootstrap + Responsive)

#### [NEW] home.jsp
- Ana sayfa, proje tanıtımı
- Hızlı yükleme butonu
- Bootstrap 5 + custom CSS

#### [NEW] upload.jsp
- Drag & drop dosya yükleme alanı
- Model seçimi (MDX-Net / HTDemucs_ft)
- Max 50MB dosya boyutu kontrolü

#### [NEW] processing.jsp
- İşlem durumu göstergesi (progress bar)
- AJAX polling ile otomatik güncelleme

#### [NEW] result.jsp
- 4 stem kartı (Vocals, Drums, Bass, Other)
- Her stem için audio player + indirme butonu
- ZIP olarak toplu indirme

#### [NEW] history.jsp
- Geçmiş işlemler tablosu
- Tarih, dosya adı, model, durum

---

### Faz 11: JUnit 4 Testleri

5+ birim testi:
1. **HomeControllerTest** — anasayfa view adı kontrolü
2. **JobServiceTest** — job oluşturma ve durum güncelleme
3. **ColabInferenceServiceTest** — mock ile inference çağrısı testi
4. **UserDaoTest** — kullanıcı CRUD
5. **JobDaoTest** — job CRUD

---

### Faz 12: Google Colab Notebook (Ayrı Dosya)

#### [NEW] colab_stemsep_server.py
- Flask/FastAPI servisi
- Demucs (HTDemucs_ft) veya MDX-Net ile stem ayırma
- `/api/separate` endpoint (POST ile dosya al, stem'leri döndür)
- Ngrok ile public URL

---

## Open Questions

> [!IMPORTANT]
> 1. **Veritabanı**: Oracle Cloud MySQL'iniz hazır mı? Yoksa geliştirmede H2/lokal MySQL kullanalım mı?

> [!IMPORTANT]
> 2. **Hibernate Uyumluluk**: Hibernate 5.3.x `javax.persistence`, Tomcat 10.1.x `jakarta.servlet` kullanır. Çözüm seçenekleri:
>    - (A) Hibernate 5.6.15.Final-jakarta kullanmak (javax→jakarta geçişli)
>    - (B) Tomcat 9.0.x'e düşmek (javax uyumlu)
>    - (C) Hoca'nın notlarında tam olarak bu konu nasıl ele alınmış?

> [!WARNING]
> 3. **Kullanıcı Kimlik Doğrulama**: PRD'de kullanıcı kaydı/girişi var mı yoksa anonim kullanıcı mı olacak? Basit session-based auth yeterli mi?

> [!IMPORTANT]
> 4. **Dosya Depolama**: Yüklenen dosyalar ve stem'ler nerede saklanacak?
>    - (A) Sunucu dosya sistemi (basit)
>    - (B) Oracle Cloud Object Storage (PRD'de bahsedilmiş)
>    - (C) Geliştirmede lokal, production'da Object Storage

---

## Verification Plan

### Automated Tests
```bash
# Maven build ve test
mvn clean test

# WAR paketi oluşturma
mvn clean package

# 5+ JUnit 4 testi başarılı geçmeli
```

### Manual Verification
1. Tomcat üzerinde WAR deploy edip tarayıcıdan kontrol
2. Dosya yükleme akışının çalıştığını doğrulama
3. i18n dil değiştirme testi (TR ↔ EN)
4. Log dosyasının (`logs/bm470.log`) tutulduğunun kontrolü
5. Browser subagent ile UI testi

### Build Verification
- `mvn clean compile` — derleme hatası olmamalı
- `mvn clean package -DskipTests` — WAR oluşmalı
- `mvn clean test` — tüm testler geçmeli
