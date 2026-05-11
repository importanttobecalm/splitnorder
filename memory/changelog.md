---
name: Changelog
description: Anlamlı değişikliklerin tarihli listesi (en yeni üstte)
type: project
updated: 2026-05-09
---

## 2026-05-11
- refactor: **Track B backend slayt-uyumlu temizlik tamamlandı** (7 commit, src/main/java net -402 satır). Sebep: AI-stili over-engineered backend → slaytlardan birebir savunulabilir parçalar.
  - B.1 User/Job/Stem entity → Lombok (@Getter/@Setter/@NoArgsConstructor/@AllArgsConstructor).
  - B.2 `src/main/java/com/stemsep/exception/` altına 8 custom `@ResponseStatus` RuntimeException (UserNotFound 404, InvalidCredentials 401, EmailExists/UsernameExists 409, EmailNotVerified 403, InvalidToken 404, GoogleAuth/InferenceFailed 502).
  - B.3 AuthService: BCrypt → SHA-256 (yeni `util/PasswordHasher`); `IllegalArgumentException("CODE")` → custom exception; verifyEmail/resendVerificationEmail artık void. 208→134 satır.
  - B.4 AuthController: class-level `@RequestMapping("/auth")` + `@RequestParam` form-based POST; `ResponseEntity<Map>` + regex email/şifre validasyonu kaldırıldı; başarılı login `session.setAttribute` + `redirect:/`; başarısız `model.addAttribute("error")` + JSP. Google OAuth + logout + profile JSP DOKUNMA kuralı için ayrı `GoogleAuthController` (`/api/auth/*`) ile çıkarıldı (spec'teki `/auth/google/login` yerine). HomeController'dan `/auth/login` + `/auth/register` GET'leri AuthController'a taşındı. 358 → ~75 satır.
  - B.5 EmailService: ~50 satır inline CSS HTML template silindi; düz text `setText(...)`. 139→61 satır.
  - B.6 ColabInferenceService: `MAX_POLL_ATTEMPTS` polling + `mockProcessing` fallback silindi; tek senkron `POST /api/separate` (readTimeout=120s); hata → `InferenceFailedException`. 257→108 satır. (Demucs ~8 sn — bkz. memory/gotchas.md)
  - B.7 AuthInterceptor: `localhost:5173` redirect → `${ctx}/auth/login`; whitelist `/static/**`, `/auth/**`, `/api/auth/**`.
  - B.8 RequestLoggingInterceptor: preHandle'da `getParameterMap()` (password/token/secret maskeleme), postHandle'da view name + model, afterCompletion'da status + süre. Rubrik "istek parametre ve dönüş görüntüsü loglara" maddesi karşılığı.
  - B.9 `log4j.properties` zaten slayt birebir (RollingFileAppender, `${catalina.base}/logs/bm470.log`, 10MB, 10 backup) — NotebookLM ile teyit edildi. `logs/` zaten .gitignore'da. Değişiklik gerekmedi.
  - B.10 pom.xml'den `spring-security-crypto` (BCrypt) kaldırıldı. `jackson-databind` (Google OAuth) + `jakarta.mail` korunuyor.
  - Build: `mvn -Dmaven.test.skip=true compile` ✅
  - **Pre-existing gotcha:** `src/test/java/com/stemsep/integration/OracleMySQLConnectionTest.java` hâlâ JUnit 4 import'larıyla (Ignore/BeforeClass/AfterClass/Test). pom'da junit:junit yok → `mvn test` compile fail. Memory'deki `-Dtest='!OracleMySQLConnectionTest'` trick'i Surefire excludesi olduğundan compile'ı atlamıyor. Migrate veya sil → ayrı gotcha olarak ele alınmalı.
- docs: Track C — proje raporu Markdown taslağı yazıldı (`docs/report/rapor.md`) + mimari/ER/sequence mermaid kaynakları (`docs/report/diagrams/*.mmd`) + ekran görüntüsü dizini (`docs/report/screenshots/.gitkeep`). Sablon.docx'e bölüm bölüm kopyala-yapıştır için hazır: kapak, BEYAN, AI Beyan, Teşekkür, Özet/Abstract, 1-10 ana bölümler, Request URI tablosu (18 endpoint), bileşen tabloları (Config/Controller/Service/DAO/Interceptor), açıklamalı 20 birim test listesi, kaynak kod kesit örnekleri (UserDao Criteria, WebAppInitializer, RequestLoggingInterceptor). Track A (JSP) ve Track B (backend) paralel çalıştığı için kod dosyalarına dokunulmadı; ekran görüntüleri Track A bitince doldurulacak yer tutucu olarak işaretlendi. Sebep: rapor teslimi 13.05.2026.

## 2026-05-09
- feat: `frontend-prototype/` klasörü kuruldu — Vite + React 19 + TS + Tailwind 3 + @xyflow/react + wavesurfer.js + lucide-react + jszip. Studio sayfasının ilk statik tasarımı `src/studio/` altına yazıldı: `StudioApp`, `InputNode`, `StemNode`, `StemEdge` (Bezier), `MasterPlayer`, `MiniWaveform`, `Logo` (4 renk treble clef SVG). Mockup'a (sonucSayfasi.png) sadık node-graph layout. Sebep: kullanıcı Studio sayfasını JSP island olarak yeniden tasarlamak istiyor; React kod Java'ya dokunmadan ayrı build edilecek. Dev: `cd frontend-prototype && npm run dev`. Tema kararı: açık tema + 4 logo rengi (vocals #E8554E, drums #F2A35E, bass #6B5B95, other #3FB8AF) — dark+gold brand kuralı bu sayfa için terk edildi.
- decision: Studio sayfası React island olarak gömülecek, JSP/JSTL korunacak (ADR #11 ihlali yok). Audio engine henüz mock — Faz 4'te wavesurfer-multitrack eklenecek. Backend endpoint'leri (`/api/stem/{id}/{type}`) değişmeyecek.

## 2026-05-08
- decision: Kaggle Flask endpoint'leri **sync + her zaman 200** yaklaşımıyla çözüldü. Sebep: Demucs ~8 sn → async/polling overhead'i değmiyor. `/api/separate` blocking, `/api/job/{id}/status` her zaman completed döner, `/api/stem/...` send_file. Detay: memory/gotchas.md, memory/user_splitnorder_demo_deploy.md
- deploy: splitnorder canlı demoya alındı — `https://splitnorder.space/` (Let's Encrypt TLS, Caddy reverse proxy). vespay Oracle VM'de docker compose (mysql:8.0 + tomcat:10.1-jdk17). Sebep: ödev demo URL ihtiyacı. /home/ubuntu/splitnorder-demo/, memory/user_splitnorder_demo_deploy.md
- fix: WAR `ROOT.war` olarak deploy edildi (önce `stemsep.war`'dı, context `/stemsep/` JSP'lerdeki absolute href'lerle 404 yaratıyordu). Sonuç: tüm linkler doğal çalışır, URL temiz. docker-compose.yml
- chore: `src/main/resources/hibernate.properties` (gitignored) production değerleriyle build öncesi oluşturuldu; WAR'a gömüldü. set-gpu-url.sh ile sadece `colab.api.url` override.
- chore: yusuf2 dalına `origin/main` merge edildi (Kaggle GPU + dev.sh + setup-mac.sh + JSP refresh getirdi). Memory dosyaları korundu (main'de yoktu, conflict yok). -
- ops: vespay sunucusu sağlık kontrolü yapıldı — load 0.00, disk %21, RAM 14 GB boş. `vespay-app-builder:latest` 3.84 GB **orphan multi-stage builder image** tespit edildi (acil değil, ileride prune adayı).

## 2026-05-01
- refactor: 3 DAO (Job/User/Stem) — 6 JPQL sorgusu CriteriaBuilder'a çevrildi (jakarta.persistence.criteria). Sebep: slayt "SQL/HQL yerine Java nesne ve metotları" — NotebookLM citation [1]. ADR 07 ✅. src/main/java/com/stemsep/dao/, docs/decisions/07-criteria-builder.md
- test: tüm testler yeşil — 20 test, 0 hata (Oracle MySQL hariç). pom.xml, src/test/**
- chore: Maven 3.9.15 + OpenJDK 21 ile derleme/test pipeline kuruldu. Sebep: `mvn test` çalıştırma ihtiyacı. Komut: `JAVA_HOME=$(/usr/libexec/java_home -v 21) PATH=$JAVA_HOME/bin:$PATH mvn test`. -
- fix: Mockito bundled byte-buddy JDK 21'i tanımıyor → byte-buddy 1.15.11 explicit override. pom.xml
- fix: log4j 2.25.3 jar Maven Central'da yok (BOM artifact); slf4j-reload4j tek başına yeterli (transitive reload4j = log4j 1.x fork). pom.xml, docs/decisions/09-log4j-surum.md
- fix: hamcrest 2.2 test dependency eklendi (Jupiter'da JUnit 4'ün transitive hamcrest'i yok). pom.xml
- docs: 12 ADR oluşturuldu (`docs/decisions/`) + CLAUDE.md tek bakış tablosu. Sebep: bir sonraki Claude için "neyi neden" özeti. docs/decisions/, CLAUDE.md
- refactor: log4j 1.2.14 → 2.25.3 + slf4j 1.7.25 → 2.0.17 + slf4j-log4j12 → slf4j-reload4j. Sebep: NotebookLM slaytları bu bağımlılıkları gösteriyor. pom.xml, log4j.properties
- refactor: JUnit 4.13.1 → 5 (Jupiter 6.0.3) — 6 test sınıfı migrate edildi (BeforeEach, Assertions, MockMvc setup). Sebep: slayt "JUnit 3/4 deprecated!" diyor. pom.xml, src/test/**
- chore: spring-context'ten commons-logging exclusion eklendi. Sebep: slayt — slf4j ile çakışmasın. pom.xml
- chore: memory sistemi kuruldu (MEMORY.md + çekirdek dosyalar). Sebep: oturumlar arası süreklilik. memory/
- chore: `yusuf2` dalı origin/main 29dd84e üzerinden açıldı. Sebep: temiz başlangıç. -
- chore: local main origin/main'e rebase edildi (1 docs commit yusufun-dali'de korunuyor). Sebep: divergent durumu temizleme. -
