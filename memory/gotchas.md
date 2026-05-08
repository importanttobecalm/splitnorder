---
name: Gotchas
description: Tekrar edilmemesi gereken hatalar / bu projeye özgü tuzaklar
type: project
updated: 2026-05-08
---

## Ders kuralları > modern best-practice
**Tuzak:** Spring Boot, JpaRepository, JUnit 5, log4j 2.x, H2 önermek.
**Sebep:** BM470 ödevi bağlayıcı; aksi puan kaybı.
**Doğru:** Saf Spring 6 + Hibernate 5.3.20 + JUnit 4 + log4j 1.2.14 + MySQL.

## Constructor injection değil, field injection
**Tuzak:** `@Autowired` constructor önermek (modern best-practice).
**Sebep:** Derste field injection gösterildi.
**Doğru:** `@Autowired` field injection.

## Paket kökü `com.stemsep` değil
**Tuzak:** Mevcut `com.stemsep.*` paketi ders teslimine uyumsuz.
**Doğru:** `tr.edu.duzce.mf.bm.bm470.*`. Migrasyon henüz yapılmadı (borç).

## Hibernate 6 → 5.3.20 düşürmek kolay değil
**Semptom:** v0.4.0'da denendi, derleme/runtime sorunları çıktı, v0.4.1'de revert edildi.
**Doğru yaklaşım:** Düşürmeden önce CriteriaBuilder + entity mapping uyumluluğu kontrolü.

## Homebrew Python 3.14 → pip bozuk
**Semptom:** `pip3 install ...` → `ImportError: pyexpat ... Symbol not found: _XML_SetAllocTrackerActivationThreshold`.
**Sebep:** `/opt/homebrew` Python 3.14.4 ile sistem `libexpat` uyumsuz.
**Doğru:** `pipx install <paket>` veya `uv tool install <paket>`. pip3'e dokunma.

## Maven testleri için JDK 21 zorunlu (JDK 25 değil)
**Semptom:** Mockito `Failed to resolve the class file version of the current VM: Unknown Java version: 0`.
**Sebep:** Brew `mvn` paketiyle JDK 25 geldi; Mockito'nun bundled byte-buddy'si yeni JVM'i tanımıyor.
**Doğru:** `JAVA_HOME=$(/usr/libexec/java_home -v 21) PATH=$JAVA_HOME/bin:$PATH mvn test` + `pom.xml`'de `byte-buddy/byte-buddy-agent 1.15.11` explicit override.

## Slaytın `log4j 2.25.3` artifact'ı Maven Central'da yok
**Semptom:** `Could not find artifact org.apache.logging.log4j:log4j:jar:2.25.3`.
**Sebep:** Slaytta gösterilen artifact aslında BOM (POM packaging), JAR yok. log4j 2.x'te `log4j-core`/`log4j-api` ayrı çekilir.
**Doğru:** `slf4j-reload4j` tek başına yeterli — reload4j'i (log4j 1.x maintained fork) transitif çekiyor; slayttaki 1.x stil `log4j.properties` config'i çalışır.

## JSP'lerde absolute href → context path `/stemsep/` ile bozulur
**Semptom:** Tomcat'e `stemsep.war` deploy edildi → context `/stemsep/`. Anasayfada "Hemen Başla" → `/upload` → 404. JSP'lerdeki `<a href="/upload">` absolute (context-relative değil).
**Sebep:** JSP'lerde `<c:url value="/upload"/>` veya `${pageContext.request.contextPath}/upload` kullanılmamış.
**Doğru (deploy):** WAR'ı **`ROOT.war`** olarak mount et → context `/` → tüm absolute href'ler doğal çalışır.
**Doğru (kod):** Yeni JSP yazarken `<c:url>` veya context path kullan.

## Caddy `caddy reload` her zaman uygulamıyor (autosave.json)
**Semptom:** `docker exec caddy caddy reload --config /etc/caddy/Caddyfile` çalışıyor görünüyor (çıktı warning'ler) ama yeni Caddyfile değişiklikleri canlıya yansımıyor. `/config/caddy/autosave.json` eski state'i tutuyor.
**Doğru:** `docker restart n8n-merkezi-caddy-1` (3 sn downtime, vespay/n8n etkilenir ama hızlı toparlar). Cleanup script'lerinde reload yerine restart yaz.

## Java backend Kaggle Flask'tan 3 endpoint bekliyor
**Semptom:** Upload sonrası "Demucs API isteği reddedildi" / "İşleniyor..." takılır. Java log'da `HTTP 404 /api/separate` veya `/api/job/{id}/status`.
**Sebep:** Kaggle notebook'undaki Flask app endpoint'leri Java'nın beklediği path'lerle uyumsuz.
**Java'nın beklediği (kaynak: `ColabInferenceService.java`):**
- `POST /api/separate` (multipart: file, model, job_id) → 200/202
- `GET /api/job/{id}/status` → `{"status":"processing|completed|failed","progress":N,"message":"..."}`
- `GET /api/stem/{id}/{stemType}` → stem WAV dosyası
**Doğru:** Notebook'ta Flask'a 3 route'u da ekle. Header `ngrok-skip-browser-warning: true` Java tarafından gönderiliyor, sıkıntı yok.
**Bu projede uygulanan (2026-05-08):** Demucs ~8 sn sürdüğü için async/polling overhead'i değmiyor → **sync + her zaman 200** yaklaşımı seçildi. `/api/separate` Demucs'ı blocking çalıştırır 200 döner, `/api/job/{id}/status` her zaman `{"status":"completed"}` döner (Java'nın polling loop'u ilk denemede biter), `/api/stem/{id}/{type}` `send_file` ile WAV döner. Mimari olarak async daha temiz ama bu ödev için pratik tercih.

## yusufun-dali ≠ origin/main
**Tuzak:** yusufun-dali dalında yapılan docs commit'leri (PROJECT_REVIEW.md, README rewrite) main'e merge edilmedi.
**Doğru:** main'i referans al. yusufun-dali'deki dökümanlar main'de YOK.
