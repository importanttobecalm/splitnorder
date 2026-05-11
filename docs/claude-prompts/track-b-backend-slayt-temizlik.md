# Track B — Backend Slayt-Uyumlu Temizlik

> Bu prompt'u yeni bir Claude Code oturumunda `claude` komutuyla aç, prompt olarak ver. Track A (JSP üretimi) **paralel** olarak yürüyor — bu yüzden bu track view dosyalarına ve `messages_*.properties`'e **dokunmaz**.

## Proje Bağlamı

`/Users/yusufbulut/Documents/Projelerim/JAVAodev/splitnorder` — BM470 İleri Java Programlama final ödevi (Düzce Üniv., Doç. Dr. Abdullah Talha Kabakuş). Müzik kaynağı ayrımı (Demucs) — Java Spring 6 + JSP frontend + Python Flask audio service.

**Önce oku (zorunlu sıra):**
1. `CLAUDE.md` — kullanılabilir/yasak yapılar, ADR tablosu
2. `memory/MEMORY.md` + tüm linkler (özellikle `feedback_notebooklm_first.md`, `project_status.md`, `gotchas.md`)
3. `docs/decisions/05-katman-mimarisi.md` + `docs/decisions/06-field-injection.md` + `docs/decisions/07-criteria-builder.md`

**Slayt kaynağı:** NotebookLM MCP'de `c8b058a9-dc43-43db-89d9-62989cf89822` notebook'u (`bm470`). Karar verirken **önce sorgula**, koddan deneme.

## Hedef

Mevcut backend, AI-stili over-engineered. Hedefe getir:
- Tüm özellikler **korunur** (kayıt, giriş, Google OAuth, email doğrulama, Demucs entegrasyonu)
- Her satır slayt-uyumlu parça kullansın (`@Controller`, `@RequestParam`, `@ResponseStatus(...)` + custom `RuntimeException`, `@Service` + `@Transactional`, `@Repository` + `CriteriaBuilder`, Lombok)
- ~700 satır azalsın
- Sunumda "bu satır slayt N'deki örnekten geldi" diye savunulabilir olsun

## Yapılacak İşler (sırasıyla)

### B.1 — `User` entity Lombok'a çevir
- Dosya: `src/main/java/com/stemsep/model/User.java`
- `@Entity @Table(name="users")` + Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`
- Manuel getter/setter'ı sil
- `Job.java` ve `Stem.java` da Lombok kullanmıyorsa onları da çevir
- `@Temporal` slaytta var ama bizim `LocalDateTime` field'larımız var — `@Temporal` koymaya gerek yok, onlar JPA 2.2+ otomatik mapping
- Build et: `JAVA_HOME=$(/usr/libexec/java_home -v 21) PATH=$JAVA_HOME/bin:$PATH mvn -Dmaven.test.skip=true compile`

### B.2 — Exception sınıfları (slayt birebir `@ResponseStatus`)
Yeni dizin: `src/main/java/com/stemsep/exception/`. Şu sınıfları ekle:
- `UserNotFoundException` → `@ResponseStatus(HttpStatus.NOT_FOUND)`
- `InvalidCredentialsException` → `@ResponseStatus(HttpStatus.UNAUTHORIZED)`
- `EmailExistsException` → `@ResponseStatus(HttpStatus.CONFLICT)`
- `UsernameExistsException` → `@ResponseStatus(HttpStatus.CONFLICT)`
- `EmailNotVerifiedException` → `@ResponseStatus(HttpStatus.FORBIDDEN)`
- `InvalidTokenException` → `@ResponseStatus(HttpStatus.NOT_FOUND)`
- `GoogleAuthException` → `@ResponseStatus(HttpStatus.BAD_GATEWAY)`
- `InferenceFailedException` → `@ResponseStatus(HttpStatus.BAD_GATEWAY)`

Her biri slayt'taki kalıba uy:
```java
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String msg) { super(msg); }
}
```

### B.3 — `AuthService` sadeleştir
- Dosya: `src/main/java/com/stemsep/service/AuthService.java`
- `BCryptPasswordEncoder` ÇIKAR. Yerine SHA-256 utility kullan (Java SE `MessageDigest`). Okul ödevi için yeterli.
- `IllegalArgumentException("STRING_CODE")` kalıbını değiştir → yukarıdaki custom exception'ları fırlat
- E-posta doğrulama akışı KORUNUR ama exception kalıbı `InvalidTokenException` tarzı
- Google OAuth `loginOrRegisterGoogle` mantığı KORUNUR
- Hedef: ~120 satır (mevcut 208 satır)

### B.4 — `AuthController` slayt tarzına çevir
- Dosya: `src/main/java/com/stemsep/controller/AuthController.java`
- `@RequestBody Map<String,String> body` → **kaldır**. Yerine `@RequestParam("email") String email, @RequestParam("password") String password` kullan (slayt birebir `/giris` örneği)
- `ResponseEntity<Map<String,Object>>` → kaldır. Form-based POST için `Model.addAttribute` + JSP view döndür (redirect veya forward)
- Class-level `@RequestMapping("/api/auth")` → `@RequestMapping("/auth")` yap (Track A login form `${ctx}/auth/login` action'ı kullanıyor)
- `/login` POST: başarılıysa `session.setAttribute("user", user)` + `redirect:/` → ana sayfaya
- `/login` POST: başarısızsa `model.addAttribute("error", "INVALID_PASSWORD")` + return "auth/login"
- `/register` POST: başarılıysa `redirect:/auth/login?message=REGISTRATION_SUCCESS`
- `/verify-email`: `redirect:/auth/login?message=EMAIL_VERIFIED`
- Google OAuth `/google/login` + `/google/callback`: yaklaşımı koru ama path `/auth/google/login`'e gel
- Regex email + password strength **TAMAMEN KALDIR** — slayt seviyesinde gerek yok
- Hedef: ~150 satır (mevcut 358 satır)
- **Önemli:** Track A `HomeController.java`'ya `GET /auth/login` ve `GET /auth/register` mapping'leri koymuş — onları **silme**, sadece `AuthController`'a POST endpoint'lerini ekle. Veya HomeController'daki GET'leri buraya taşı ve HomeController sadece `/` `home` döndürsün

### B.5 — `EmailService` HTML template → düz text
- Dosya: `src/main/java/com/stemsep/service/EmailService.java`
- `buildVerificationEmailHtml(...)` metodunu sil
- `setContent(htmlContent, "text/html; charset=UTF-8")` yerine `setText(plainBody, "UTF-8")`
- Body'yi düz text yap: `"Merhaba <username>, e-postanı doğrulamak için: <url>"`
- Hedef: ~50 satır (mevcut 139 satır)

### B.6 — `ColabInferenceService` senkron tek çağrı
- Dosya: `src/main/java/com/stemsep/service/ColabInferenceService.java`
- `MAX_POLL_ATTEMPTS` polling döngüsü → KALDIR
- `mockProcessing` fallback → KALDIR
- Tek senkron `HttpURLConnection` çağrısı: POST /api/separate, 200 dönerse `createStemRecords`, değilse `InferenceFailedException` fırlat
- Memory'deki nota göre Demucs 8 saniyede tamamlanıp 200 dönüyor — polling gereksiz
- Hedef: ~80 satır (mevcut 257 satır)

### B.7 — `AuthInterceptor` JSP redirect
- Dosya: `src/main/java/com/stemsep/interceptor/AuthInterceptor.java`
- `localhost:5173/login` redirect → `${ctx}/auth/login` redirect
- Whitelist path'leri: `/static/**`, `/auth/**`, `/api/auth/**` (Google OAuth callback için)

### B.8 — `RequestLoggingInterceptor` zenginleştir (rubrik gereği)
- Dosya: `src/main/java/com/stemsep/interceptor/RequestLoggingInterceptor.java`
- `preHandle`: URI + tüm `request.getParameterMap()` parametrelerini logla
- `afterCompletion`: response status + süre + Controller dönüş değeri (varsa) logla
- **Rubrik:** "Bütün isteklerde, istek parametre ve değerleri ile geri dönüş görüntüsünün/verisinin log4j ve slf4j kullanılarak loglara basılması" — bu maddeyi karşılayan örnek loglar üret

### B.9 — `log4j.properties` FileAppender doğrula
- Dosya: `src/main/resources/log4j.properties`
- FileAppender var mı kontrol et, yoksa ekle: `logs/splitnorder.log` (rolling daily, max 10MB)
- **Rubrik:** "Logların sadece konsola değil, aynı zamanda dosya sistemine de kaydedilmesi gerekmektedir"
- `logs/` dizinini `.gitignore`'a ekle (yoksa)

### B.10 — Eski Maven dependency'leri çıkar
- Dosya: `pom.xml`
- B.3'te BCrypt çıktıysa: `spring-security-crypto` dependency'sini sil
- B.4'te Map-based JSON response kalktıysa, `jackson-databind` muhtemelen hâlâ Google OAuth için kullanılıyor — orada kalsın
- `EmailService` hâlâ kullanılıyor — `jakarta.mail` kalsın
- Build et: `mvn -Dmaven.test.skip=true compile`

## Kurallar

- **NotebookLM-first:** Yeni bir karar gerektiğinde önce `bm470` sorgula
- **Yasak yapılar:** `@SpringBootApplication`, `spring-boot-starter-*`, `@RestController` (JSP döndürüyoruz), `JpaRepository`, constructor injection, BCrypt (kaldır)
- **Saf annotation Spring 6 + saf Hibernate 6 + saf JSP** — Boot yok
- **Tüm değişiklik sonrası:** Build et + test çalıştır (`mvn -Dmaven.test.skip=true compile`)
- **Commit:** Her ana adım sonrası anlamlı commit (`feat:` veya `refactor:` prefix), **push etme**

## DOKUNMA (Track A bu dosyalarda çalışıyor)

- `src/main/webapp/WEB-INF/views/**` (tüm JSP'ler)
- `src/main/resources/messages_tr_TR.properties` + `messages_en_US.properties`
- `src/main/java/com/stemsep/controller/HomeController.java` (sadece GET routes — sadece sil + AuthController'a taşı, başka değişiklik yapma)

## Tamamlandı Kriteri

- Tüm 10 alt madde tamam, her biri commit'li
- `mvn -Dmaven.test.skip=true compile` BAŞARILI
- Mevcut özellikler hâlâ çalışıyor (lokal test gerekiyorsa: `./dev.sh` çalıştır, `localhost:8090/auth/login`'i tarayıcıda dene)
- `memory/changelog.md` güncellendi (her commit için 1 satır)

## Kaynaklar

- ADR'lar: `docs/decisions/`
- Slayt sorguları: `mcp__notebooklm-mcp__notebook_query` `bm470` (`c8b058a9-dc43-43db-89d9-62989cf89822`)
- Mevcut tasarım kararları: `CLAUDE.md` "Karar Özetleri" tablosu
