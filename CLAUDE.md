# splitnorder — Claude Çalışma Talimatları

> **BM470 İleri Java Programlama** ders projesi (Düzce Üniv. MF BM, Doç. Dr. Talha KABAKUŞ).
> Müzik kaynağı ayrımı (Demucs) — Java backend + Python Flask audio service + JSP frontend.

## ⚠️ ÖNCELİKLİ OKUMA (Her Konuşmada)

1. **`memory/MEMORY.md`** — son oturum durumu + feedback kuralları
2. **`memory/feedback_notebooklm_first.md`** — NotebookLM-First kuralı (kod yazmadan önce `bm470` sorgula)
3. **Bu dosyanın "Karar Özetleri" tablosu** (aşağıda) — ne neden böyle yapılmış, tek bakışta gör
4. Konuya göre ilgili karar dosyası: [`docs/decisions/`](docs/decisions/README.md)

## 🧭 Karar Özetleri (Her bir başlık tek satır — detay için ilgili .md)

> Her satır: **konu → karar → tek cümle gerekçe**. Tam gerekçe + slayt alıntısı için linkteki ADR dosyasını aç.

| # | Konu | Karar (özet) | Statü | Detay |
|---|------|--------------|-------|-------|
| 01 | **Paket kökü** | `com.stemsep` korunur (migrasyon iptal) — slaytlar zorunluluk koymuyor | ✅ | [01](docs/decisions/01-paket-adlandirma.md) |
| 02 | **Hibernate sürümü** | 6.1.7 kalır — slaytlar 5.3.20 örnek ama zorunluluk değil | ✅ | [02](docs/decisions/02-hibernate-surum.md) |
| 03 | **Spring** | 6.0.4 + saf annotation, **Boot yok** — proje gereksinimi `Spring MVC 6` | ✅ | [03](docs/decisions/03-spring-mvc-6.md) |
| 04 | **Bootstrap** | `WebApplicationInitializer` + Java config, `web.xml` yok | ✅ | [04](docs/decisions/04-webapp-initializer.md) |
| 05 | **Mimari** | Controller → Service → DAO → Hibernate Session, JpaRepository yasak | ✅ | [05](docs/decisions/05-katman-mimarisi.md) |
| 06 | **DI tarzı** | `@Autowired` field injection (constructor değil) | ✅ | [06](docs/decisions/06-field-injection.md) |
| 07 | **Sorgu API** | Tüm DAO'lar `CriteriaBuilder` (jakarta.persistence.criteria) ile, JPQL kalmadı | ✅ | [07](docs/decisions/07-criteria-builder.md) |
| 08 | **JUnit** | JUnit 5 (Jupiter) 6.0.3 — slayt birebir; 4'ten geçiş yapıldı | ✅ | [08](docs/decisions/08-junit-surum.md) |
| 09 | **log4j** | log4j 2.25.3 + slf4j-reload4j 2.0.17 — slayt birebir; 1.2.14'ten geçiş yapıldı | ✅ | [09](docs/decisions/09-log4j-surum.md) |
| 10 | **i18n** | TR + EN `messages_*.properties`, TR zorunlu | ✅ | [10](docs/decisions/10-i18n-yapisi.md) |
| 11 | **View** | JSP + JSTL (`/WEB-INF/views/`), `jakarta.tags.core` namespace | ✅ | [11](docs/decisions/11-jsp-jstl.md) |
| 12 | **Interceptor** | `RequestLoggingInterceptor` cross-cutting log için (proje gereksinimi) | ✅ | [12](docs/decisions/12-interceptor.md) |

**Statü:** ✅ Kabul / 🔧 Plan (yapılacak) / ❓ Belirsiz (kullanıcı onayı bekliyor)

## Yasak Yapılar (Net)

`@SpringBootApplication`, `spring-boot-starter-*`, `@RestController` (JSP döndürüyoruz), `JpaRepository`, `CrudRepository`, `application.properties` Boot stili, H2 in-memory, constructor injection.

## Yeni Feature / Update Akışı

1. **NotebookLM `bm470` sorgula** (kural: `feedback_notebooklm_first.md`)
2. İlgili **ADR'ı aç** (yukarıdaki tablodan), karar/gerekçeyi gör
3. Kod yaz — yasak yapıları kullanma
4. Yeni karar gerektiyse `docs/decisions/` altına yeni `NN-konu.md` ekle + bu tablodaki listeye **bir satır** ekle
5. Memory güncelle (`changelog.md` + gerekirse `decisions.md`)

## Birincil Kaynak Hiyerarşisi
1. **NotebookLM `bm470`** — ham PDF'ler (en güvenilir)
2. **`docs/decisions/*.md`** — bu projeye özel kararlar (NotebookLM alıntılarıyla)
3. `docs/guidelines/courses/*` — ders ham özetleri (yardımcı)

> Eski `INTEGRATION_GUIDELINES.md` ve `PROJECT_ARCHITECTURE.md` belgelerinin yerini bu ADR seti alıyor. Çelişki olursa **ADR doğrudur**.
