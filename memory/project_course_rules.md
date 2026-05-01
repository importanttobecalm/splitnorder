---
name: BM470 Ders Kuralları
description: Bağlayıcı ders kuralları ve yasak yapılar
type: project
updated: 2026-05-01
---

## Bağlayıcı Sürümler
- Spring 6.0.4 (Boot YOK)
- Hibernate 5.3.20 (6.x değil)
- log4j 1.2.14 + slf4j 1.7.25
- JUnit 4.13.1 (5 değil)
- MySQL (H2 değil)
- Packaging: **war**

## Bağlayıcı Yapılar
- `WebApplicationInitializer` (Spring Boot init değil)
- `CriteriaBuilder` zorunlu (HQL/native opsiyonel)
- `@Autowired` field injection
- Paket kökü: `tr.edu.duzce.mf.bm.bm470`
- i18n: TR + EN `messages_*.properties`

## Yasak
`@SpringBootApplication`, `@RestController` (REST için ayrı kural varsa kontrol et), `JpaRepository`, `CrudRepository`, log4j 2.x API, JUnit 5, Boot stili `application.properties`, H2 in-memory.

## Referans Dokümanlar
- `docs/guidelines/INTEGRATION_GUIDELINES.md` — sıkı kurallar tablosu
- `docs/guidelines/PROJECT_ARCHITECTURE.md` — paket yapısı
- `docs/guidelines/courses/ders7-8-9.md` — Service, DAO, Criteria, log
- `docs/guidelines/courses/ders_kod_referansi.md` — kod örnekleri

## Mevcut Borçlar
- `com.stemsep` → `tr.edu.duzce.mf.bm.bm470` migrasyonu
- Hibernate 6.1.7 → 5.3.20 düşürme
