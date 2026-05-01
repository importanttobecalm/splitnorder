---
name: Changelog
description: Anlamlı değişikliklerin tarihli listesi (en yeni üstte)
type: project
updated: 2026-05-01
---

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
