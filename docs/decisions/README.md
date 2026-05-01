# Karar Belgeleri (ADR)

Bu klasör, projedeki teknik kararların **kısa gerekçesini** ve **NotebookLM/slayt bağlantısını** içerir. Her dosya tek konu, tek karar.

## Statü Etiketleri
- ✅ **Kabul:** Mevcut kod uyumlu, dokunulmuyor.
- 🔧 **Plan:** Henüz uygulanmadı, yapılacak değişiklik.
- ❓ **Belirsiz:** Karar netleşmedi, kullanıcı onayı bekliyor.

## Konu Dizini

| # | Konu | Statü | Ne zaman oku? |
|---|------|-------|---------------|
| [01](01-paket-adlandirma.md) | Paket kökü (`com.stemsep`) | ✅ | Yeni paket/sınıf eklerken |
| [02](02-hibernate-surum.md) | Hibernate 6.1.7 | ✅ | Hibernate config / entity dokunurken |
| [03](03-spring-mvc-6.md) | Spring 6.0.4 + annotation | ✅ | Controller/config dokunurken |
| [04](04-webapp-initializer.md) | `WebApplicationInitializer` (web.xml yok) | ✅ | Servlet/filter/dispatcher değişikliği |
| [05](05-katman-mimarisi.md) | Controller / Service / DAO / Model | ✅ | Yeni özellik eklerken (her zaman) |
| [06](06-field-injection.md) | `@Autowired` field injection | ✅ | DI yaparken |
| [07](07-criteria-builder.md) | `CriteriaBuilder` (JPQL → Criteria) | 🔧 | DAO'ya yeni sorgu eklerken |
| [08](08-junit-surum.md) | JUnit 4 vs 5 | ❓ | Test yazarken |
| [09](09-log4j-surum.md) | log4j 1.2.14 vs 2.x | ❓ | Loglama config'i değişirken |
| [10](10-i18n-yapisi.md) | TR/EN `messages_*.properties` | ✅ | Yeni metin/etiket eklerken |
| [11](11-jsp-jstl.md) | JSP + JSTL view | ✅ | View dokunurken |
| [12](12-interceptor.md) | `RequestLoggingInterceptor` | ✅ | Cross-cutting log/auth eklerken |

## NotebookLM Birincil Kaynak
Karar belgelerinde alıntılanan slayt parçaları **NotebookLM bm470** notebook'undan (alias `bm470`, ID `0e624500-c0de-4aa2-b2f4-a3b290c04257`) çekilmiştir. Şüpheli durumda **önce NotebookLM'e sor**, sonra kod yaz (kural: `memory/feedback_notebooklm_first.md`).
