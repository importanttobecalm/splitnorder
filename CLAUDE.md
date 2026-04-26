# splitnorder — Claude Çalışma Talimatları

> **BM470 İleri Java Programlama** ders projesi (Düzce Üniv. MF BM, Doç. Dr. Talha KABAKUŞ).
> Müzik kaynağı ayrımı (Demucs) — Java backend + Python Flask audio service + JSP frontend.

## ⚠️ ÖNCELİKLİ OKUMA (Her Konuşmada)

Kod yazmadan / öneri vermeden önce şu sırayla:

1. [docs/guidelines/README.md](docs/guidelines/README.md) — giriş, okuma sırası
2. [docs/guidelines/INTEGRATION_GUIDELINES.md](docs/guidelines/INTEGRATION_GUIDELINES.md) — **SIKI KURALLAR tablosu (kullanma/kullan)**
3. [docs/guidelines/PROJECT_ARCHITECTURE.md](docs/guidelines/PROJECT_ARCHITECTURE.md) — paket yapısı, 3 katmanlı şema
4. Çalışılan konuya göre [docs/guidelines/courses/](docs/guidelines/courses/) altındaki ilgili ders dökümanı

## Çekirdek Kurallar (Ezber)

- **Ders bağlayıcıdır**: Modern best-practice ile çakışınca ders kazanır
- **Spring Boot YOK** — Saf Spring 6.0.4 + `WebApplicationInitializer`
- **Hibernate 5.3.20** (6.x değil), **CriteriaBuilder** zorunlu (HQL/native opsiyonel)
- **log4j 1.2.14** + slf4j 1.7.25 (log4j 2.x **yasak**)
- **JUnit 4.13.1** (5 değil), **MySQL** (H2 değil), packaging: **war**
- **Paket kökü**: `tr.edu.duzce.mf.bm.bm470` (mevcut `com.stemsep` ders teslimine uyumsuz)
- **i18n**: TR + EN `messages_*.properties` zorunlu
- **DI**: `@Autowired` field injection (constructor injection değil — derste böyle gösterildi)

## Yasak Yapılar

`@SpringBootApplication`, `@RestController`, `JpaRepository`, `CrudRepository`, log4j 2.x API'leri, JUnit 5, `application.properties` Boot stili, H2 in-memory.

## Yeni Feature / Update Yaparken

1. İlgili ders bölümünü `courses/` altından oku
2. `INTEGRATION_GUIDELINES.md` "Sıkı Kurallar" tablosuyla çapraz kontrol
3. Kod yaz (paket: `tr.edu.duzce.mf.bm.bm470.*`)
4. `PROJECT_ARCHITECTURE.md` "Güncellemeler" satırına tarih düş
5. Yeni kural çıktıysa `INTEGRATION_GUIDELINES.md`'ye ekle
6. CHANGELOG.md güncelle

## Ders Dökümanları (courses/)

| Dosya | Kapsam |
|-------|--------|
| [ders7-8-9.md](docs/guidelines/courses/ders7-8-9.md) | Service & DAO + Criteria Query API + Loglama (1923 satır) |
| [ders_kod_referansi.md](docs/guidelines/courses/ders_kod_referansi.md) | Kod referansı: pom.xml, WebAppInitializer, Controller, Interceptor, i18n, JSP, JUnit (1006 satır) |

**Eksik:** Ders 1-3 (`ilk3pdf.md`), Ders 4-5-6 ham özetleri.

## Mevcut Proje Borçları

`com.stemsep` paketi + Hibernate 6.1.7 + H2 — ders teslimine uyumsuz. Migrasyon planı `INTEGRATION_GUIDELINES.md` "Mevcut Proje ile Uyumsuzluklar" bölümünde.
