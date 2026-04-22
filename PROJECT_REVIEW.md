# AI StemSep (splitnorder) – Proje İnceleme Raporu

**İnceleme Tarihi:** 2026-04-22
**İnceleyen:** Claude Code (Opus 4.7)
**Depo:** https://github.com/importanttobecalm/splitnorder
**Commit:** `60a7121` (Fix merge conflict in .gitignore)

---

## 1. Genel Bakış

Proje, kullanıcıların yüklediği müzik dosyalarından (mp3, wav, flac) AI modelleri (MDX-Net / HTDemucs_ft) ile vokal, davul, bas ve diğer enstrüman stem'lerini ayıran bir **Spring MVC web uygulaması**. BM470 dersi kapsamında geliştirilmiş.

**Teknoloji Yığını:**
- Java 18, Maven (WAR packaging)
- Spring MVC 6.0.4, Spring ORM
- Hibernate ORM 6.1.7.Final
- c3p0 connection pool
- MySQL 8 (prod) / H2 (dev)
- JSP + JSTL (Jakarta), Bootstrap 5
- Log4j 1.2.14 + slf4j 1.7.25
- JUnit 4.13.1, Mockito
- Jakarta Servlet 6 / Tomcat 10.1.8
- Harici: Google Colab + Ngrok (GPU inference)

**Toplam kod:** ~3.6K satır (Java: ~1.2K, JSP: ~2.1K, test: ~380).

---

## 2. Mimari

Klasik **katmanlı mimari** temiz bir şekilde uygulanmış:

```
Controller → Service → DAO → Hibernate → DB
                 ↓
        ColabInferenceService (HTTP) → Colab GPU
```

- **config/**: `AppConfig`, `WebConfig`, `WebAppInitializer`, `HibernateConfig` — XML yok, tamamen annotation-based. `AbstractAnnotationConfigDispatcherServletInitializer` doğru kullanılmış.
- **model/**: `User`, `Job`, `Stem`, `JobStatus` (enum) — JPA entities, `jakarta.persistence`.
- **dao/**: Hibernate `SessionFactory` + `getCurrentSession()` pattern.
- **service/**: `@Transactional` ile işlem sınırları.
- **controller/**: Home, Upload, Job, History — REST + MVC karışık (JSON status endpoint, view render).
- **interceptor/**: `RequestLoggingInterceptor` tüm istekleri loglar.
- **i18n**: TR/EN mesaj kaynakları, cookie tabanlı locale.

---

## 3. Güçlü Yönler

1. **Temiz katman ayrımı**: Controller / Service / DAO sorumlulukları net, circular dependency yok.
2. **Annotation-based config**: `web.xml` kullanılmamış, modern Jakarta Servlet 6 uyumlu.
3. **i18n desteği**: TR/EN messages + `LocaleChangeInterceptor` + cookie resolver doğru konfigüre.
4. **Asenkron işleme**: `@Async` + `@EnableAsync` ile uzun süren Colab çağrıları UI'ı bloklamıyor.
5. **Mock fallback**: `ColabInferenceService` Colab erişilemediğinde mock mode'a düşüyor — dev deneyimi iyi.
6. **Test altyapısı**: 5 test dosyası (Controller, Service, DAO) — PRD'deki 5+ test hedefi karşılanmış.
7. **File upload güvenliği (kısmi)**: `MAX_FILE_SIZE` kontrolü, uzantı whitelist'i (mp3/wav/flac), `MultipartConfigElement` ile servlet seviyesi sınır.
8. **Implementation plan.md**: Proje kararları, soru işaretleri ve alternatifler belgelenmiş.

---

## 4. Kritik Sorunlar (Build/Runtime'ı Kıranlar)

### 4.1 ⛔ `HibernateConfig` – Spring 6'da kaldırılan paketi kullanıyor
`HibernateConfig.java` şunları import ediyor:
```java
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
```
**Spring 6.x, `spring-orm` içinden `hibernate5` paketini kaldırdı.** Spring 6 yalnızca JPA ile çalışır veya `spring-orm-hibernate5` ayrı modülü gerekir. pom'da Hibernate **6.1.7** kullanılıyor, fakat kod `hibernate5` package'ını import ediyor. Bu **derleme hatası** verir.

**Çözüm seçenekleri:**
- (A) Hibernate 6 native SessionFactory + `HibernateTransactionManager` için `org.springframework:spring-orm` yerine doğrudan Hibernate 6 API'sini kullan (`org.hibernate.cfg.Configuration` veya JPA'ya geç).
- (B) Spring 5.3.x + Hibernate 5.6.15 'e düş (stack uyumlu).
- (C) `JpaTransactionManager` + `LocalContainerEntityManagerFactoryBean` ile JPA-native yaklaşıma geç.

`implementation_plan.md` bu uyumsuzluğun farkında ama çözüm seçilmemiş; kod Spring 5 dönemine ait kalıp ile yazılmış.

### 4.2 ⛔ Ders gereksinimleri ile pom.xml çelişkisi
`implementation_plan.md` "Hibernate 5.3.20.Final (Hibernate 7 DEĞİL)" diyor; ama `pom.xml` **6.1.7.Final** kullanıyor. Dersin zorunlu versiyon listesi ile mevcut kod arasında uyuşmazlık var. Not kaybı riski.

### 4.3 ⚠️ `User` entity kullanılmıyor ama DAO/Service var
`UploadController` session id üzerinden anonim iş akışı kullanıyor (`session.getId()`), `Job.sessionId` alanı var. `UserService` ve `UserDao` tanımlı fakat hiçbir controller `User`'a bağlı değil. Ya auth eklenmeli ya da ölü kod temizlenmeli.

---

## 5. Fonksiyonel Hatalar / Bug Riskleri

### 5.1 `ColabInferenceService.createStemRecords` — dosyalar yok, yine de kayıt oluşuyor
`mockProcessing` `true` döndürüyor, sonra `createStemRecords` 4 adet `Stem` kaydını DB'ye yazıyor **fakat gerçek ses dosyası yok**. Download endpoint'leri 404 dönecek. Mock modda, stemler için boş/placeholder wav üretilmeli veya UI mock modu ayrıca göstermeli.

### 5.2 Colab entegrasyonu – dosya göndermiyor, yalnızca **path** gönderiyor
```java
String jsonPayload = "{\"file_path\":\"%s\", ...}"
```
Colab sunucusu (ayrı bir makine/notebook) yerel sunucu dosya yolunu **okuyamaz**. Gerçek kullanımda `multipart/form-data` ile dosyanın kendisinin gönderilmesi veya URL üzerinden erişilebilir bir storage (Oracle Object Storage) gerekir. Mevcut kod yalnızca aynı makinede hem Java hem Colab çalışsa işler.

### 5.3 Stream response'unda dönen stem dosyasının kaydedilmediği
`sendToColabForProcessing` HTTP 200 kontrol ediyor fakat **response body'yi okumuyor**; Colab'dan dönen stem dosyalarının nasıl diske yazıldığı belirsiz. `createStemRecords` sadece boş path'ler atıyor. Entegrasyon yarım.

### 5.4 `new URL(...)` deprecated
Java 18'de `new URL(String)` deprecated değil ama Java 20+ deprecated. `URI.create(...).toURL()` tercih edilmeli.

### 5.5 `@Async` + `@Transactional` self-invocation riski
`JobService.processJobAsync` → `colabService.processJob`: farklı bean olduğu için OK. Fakat `JobService` içinden `this.processJobAsync(...)` çağrılsaydı proxy bypass olurdu. Mevcut kullanımda sorun yok, gelecekteki refactor'larda dikkat.

### 5.6 `CookieLocaleResolver` deprecated constructor
Spring 6'da `new CookieLocaleResolver()` deprecated; `new CookieLocaleResolver("lang")` veya yeni API tercih edilmeli.

### 5.7 `new Locale("tr")` deprecated
Java 19+ `Locale.of("tr")` önerilir.

---

## 6. Güvenlik Endişeleri

| # | Sorun | Risk | Öneri |
|---|-------|------|-------|
| 1 | **Path traversal**: `Stem.filePath` veritabanından geliyor, `new File(stem.getFilePath())` kontrolsüz açılıyor. | Orta | Stems dir root'una `toAbsolutePath().normalize().startsWith(stemsRoot)` kontrolü |
| 2 | **Content-Disposition filename injection**: `"stems_" + job.getOriginalFilename() + ".zip"` kullanıcı girdisi header'a yansıtılıyor. CRLF injection potansiyeli. | Orta | Filename sanitize / RFC 5987 encode |
| 3 | **Kullanıcı adı/şifre plaintext** `application.properties` içinde (production için yorum satırı). Repo'ya sızma riski. | Yüksek | Env var veya secret manager kullan |
| 4 | **CSRF koruması yok**. Spring Security entegre değil, upload endpoint korumasız. | Orta | Spring Security veya CSRF token filtresi |
| 5 | **`hibernate.hbm2ddl.auto=update`** production'da tehlikeli. | Orta | Prod'da `validate` + Flyway/Liquibase |
| 6 | **Dosya içerik doğrulaması yok** — yalnızca uzantı kontrolü. MIME / magic-byte kontrolü yok. | Düşük-Orta | Apache Tika ile content-type detection |
| 7 | **Session-based job access control yok** — `/job/{id}` her sessiona açık; başka oturumdaki job'lar görünebilir. | Orta | `job.sessionId == session.getId()` kontrolü |
| 8 | **`hibernate.show_sql=true`** prod'da performans + log leak. | Düşük | Profile bazlı ayar |

---

## 7. Kod Kalitesi Notları

- **Constructor injection yerine field injection** (`@Autowired` field): test edilebilirlik düşer, immutability kaybolur. Constructor injection önerilir.
- **Magic strings**: stem type'ları (`"vocals"`, `"drums"`, …) string olarak dağılmış. Enum yap.
- **`HttpURLConnection`**: Java 11+ `HttpClient` veya Spring `RestTemplate/WebClient` modern alternatif.
- **Manuel stream kopyalama**: Java NIO `Files.copy` veya `InputStream.transferTo` kullanılabilir.
- **`@EnableAsync` varsayılan SimpleAsyncTaskExecutor** kullanır (thread pool yok) — her çağrıda yeni thread. `TaskExecutor` bean tanımla.
- **`JobStatus` bir enum iken `modelUsed` hâlâ String** — tutarsızlık. `ModelType` enum yap.
- **`.gitignore`** merge conflict geçmişi var — proje başlangıcında tempo indicator.

---

## 8. Test Kalitesi

- 5 test dosyası mevcut, framework JUnit 4 + Mockito.
- **Eksik:** `UploadController`, `JobController` controller-level testler, `MockMvc` ile integration test yok.
- Test coverage raporu (JaCoCo vb.) tanımlı değil.
- H2 ile DAO testleri çalışıyor olmalı fakat `HibernateConfig` kritik hatasıyla birlikte testlerin gerçekten geçip geçmediği şüpheli — **`mvn test` çalıştırılmamış durumda** (commit hash'e göre).

---

## 9. Dokümantasyon

- `README.md` çok yetersiz (2 satır, "splicing" yazım hatası).
- `implementation_plan.md` güzel ama **kapalı soruların çoğu hâlâ açık** (DB bağlantısı, Hibernate uyumluluğu, auth modeli, storage).
- Kurulum/run talimatı yok, Colab notebook dosyası repo'da yok (`colab_stemsep_server.py` eksik).

---

## 10. Öncelikli Aksiyon Listesi

### P0 – Build çalışsın
1. `HibernateConfig` Spring 6 + Hibernate 6 uyumlu hale getir (JPA `LocalContainerEntityManagerFactoryBean`'a geç).
2. `mvn clean compile` + `mvn test` localde yeşil olmalı.
3. Ders gereksinimleri ile Hibernate sürümünü netleştir (eğitmen ile konuş → 5.6 jakarta mı 6.x mi?).

### P1 – Fonksiyonellik
4. Colab entegrasyonunda gerçek dosya upload + response body'den stem dosyalarını diske yazma.
5. Mock mode için placeholder wav üret veya UI'da "mock" göster.
6. Session-based job erişim kontrolü ekle.

### P2 – Güvenlik & Kalite
7. Path traversal + filename injection fix.
8. Credentials'ı env var/vault'a taşı.
9. CSRF + file content validation.
10. Constructor injection'a geç, magic string'leri enum yap.
11. `TaskExecutor` bean tanımla (`@Async` için).

### P3 – Dokümantasyon
12. README'yi genişlet (kurulum, çalıştırma, env değişkenleri, Colab notebook).
13. `colab_stemsep_server.py` repo'ya ekle veya ayrı repo linki ver.

---

## 11. Özet Puanlama

| Boyut | Puan (10 üzerinden) | Açıklama |
|-------|---------------------|----------|
| Mimari | 7 | Katman ayrımı temiz, modern config |
| Kod Kalitesi | 5 | Field injection, magic strings, deprecated API'ler |
| Güvenlik | 4 | CSRF yok, path traversal riski, plaintext creds |
| Test | 5 | Var ama kapsam dar, MockMvc yok |
| Derlenebilirlik | 2 | HibernateConfig Spring 6'da derlenmez |
| Dokümantasyon | 4 | Plan iyi, README/run talimatı zayıf |
| **Ortalama** | **~4.5** | **MVP öncesi, kritik düzeltmeler gerekli** |

---

## 12. Sonuç

Proje güzel bir iskelete ve net bir vizyona sahip — katmanlı Spring MVC + Hibernate + Colab GPU inference. Ancak şu anki commit **derlenmez durumda** (Spring 6 ↔ Hibernate 6 ↔ eski `spring-orm` API'si uyumsuzluğu) ve Colab entegrasyonu pratikte çalışmayacak bir payload kullanıyor. Öncelik: (1) build'i yeşile çıkarmak, (2) Colab'a gerçek dosya yolu yerine dosyanın kendisini göndermek, (3) session bazlı erişim kontrolü + temel güvenlik. Bu üçü tamamlanınca MVP gösterime hazır olur.
