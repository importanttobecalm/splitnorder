# 05 — 3 Katmanlı Mimari: Controller / Service / DAO

**Statü:** ✅ Kabul

## Karar
Her istek akışı: `@Controller` → `@Service` → `@Repository` (DAO) → Hibernate `Session` → DB.

## Neden
NotebookLM citation [1] (proje gereksinimleri):

> *"Uygulamanın bütün bileşenleri (Configuration, Controller, Interceptor, Service, DAO, vb.) kullanım amaçlarıyla beraber açıklanmalıdır."*

Slaytlardaki örnek DAO (citation [6]): `getSession().remove(object)` → `SessionFactory` üzerinden.

## Uygulama
- **Controller:** `@Controller` (View döndüren), `@RestController` **kullanılmıyor** (JSP view döndürüyoruz).
- **Service:** `@Service`, `@Transactional` iş mantığı katmanı.
- **DAO:** `@Repository`, `SessionFactory.getCurrentSession()` ile Hibernate Session. `JpaRepository`/`CrudRepository` **yasak**.
- **Model:** `@Entity` POJO'lar, JPA annotation'ları.

## Kaynak
NotebookLM citations [1], [6].
