# 07 — `CriteriaBuilder` Tüm DAO Sorgularında

**Statü:** ✅ Kabul (2026-05-01 — JPQL → Criteria geçişi tamamlandı)

## Karar
Tüm DAO sorguları **`CriteriaBuilder` (jakarta.persistence.criteria)** ile yazılır. JPQL/HQL kullanılmaz.

## Neden
NotebookLM citation [1] (Hibernate Criteria Query API slaytı, Doç. Dr. Talha Kabakuş):

> *"SQL ya da HQL komutları yerine Java nesne ve metotlarının kullanılmasını sağlar. Session arayüzünden `getCriteriaBuilder()` ile elde edilen CriteriaBuilder nesnesi ile SQL sorgularını nesne tabanlı olarak tanımlanması sağlanır."*

Slaytlarda 30+ örnek sadece Criteria API ile gösterilmiş; HQL/JPQL örneği yok.

## Slayt İskeleti (citation [2])
```java
Session session = sessionFactory.getCurrentSession();
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
// Predicate nesneleri
criteriaQuery.select(root).where(predicate);
Query<Ogrenci> query = session.createQuery(criteriaQuery);
List<Ogrenci> ogrenciListesi = query.getResultList();
```

## Migrate Edilen Sorgular

| DAO | Metod | Slayt karşılığı |
|---|---|---|
| `JobDao` | `findBySessionId` | equal + orderBy desc (citation [6], [13]) |
| `JobDao` | `findAll` | sadece select + orderBy desc |
| `UserDao` | `findByUsername` | equal (citation [6]) |
| `UserDao` | `findByEmail` | equal |
| `StemDao` | `findByJobId` | foreign-key path: `root.get("job").get("id")` (citation [18]) |
| `StemDao` | `findByJobIdAndType` | iki Predicate + `cb.and(...)` (citation [3]) |

## Kullanılan Slayt Yapıları
- **`equal`** — citation [6]
- **`and`** — citation [3] *("CriteriaBuilder Sorgu Metotları (I) — and() = and")*
- **`desc(...)` Order** — citation [13]
- **Foreign-key path** (`root.get("job").get("id")`) — citation [18] *("ogrenciRoot.get(\"danisman\").get(\"danismanId\")"*)
- **`getResultList()`** — citation [2]

## Test Sonucu
`mvn test -Dtest='!OracleMySQLConnectionTest'` → **20/20 PASS**

## Yapılmadı (slaytta var ama bu projede gerekmedi)
- `like` (arama özelliği yok)
- `in` (çoklu değer filtresi yok)
- `count`, `sum`, `avg`, `min`, `max` projeksiyonları
- `multiselect`, `groupBy`
- `setFirstResult` / `setMaxResults` (sayfalama)
- `getSingleResult` (uniqueResult yerine `isEmpty` ile null-safe pattern tercih edildi — slayt da exception riskini citation [8]'de uyarıyor)

İleride bu özelliklerden biri gerektiğinde slayt birebir uygulanır.

## Kaynak
NotebookLM citations [1]–[32] (tek source: Hibernate Criteria Query API slaytı).
