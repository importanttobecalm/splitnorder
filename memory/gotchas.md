---
name: Gotchas
description: Tekrar edilmemesi gereken hatalar / bu projeye özgü tuzaklar
type: project
updated: 2026-05-01
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

## yusufun-dali ≠ origin/main
**Tuzak:** yusufun-dali dalında yapılan docs commit'leri (PROJECT_REVIEW.md, README rewrite) main'e merge edilmedi.
**Doğru:** main'i referans al. yusufun-dali'deki dökümanlar main'de YOK.
