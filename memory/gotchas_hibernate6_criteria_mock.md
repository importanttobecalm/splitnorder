---
name: Hibernate 6 Criteria mock'larken JPA tipleri patlar
description: Hibernate 6 covariant return — Session.getCriteriaBuilder() artık HibernateCriteriaBuilder döndürür; Mockito stub'ları için Jpa* tiplerini kullan
type: feedback
---

DAO testlerinde Criteria zincirini mock'larken `jakarta.persistence.criteria.CriteriaBuilder` / `CriteriaQuery` / `Root` / `Predicate` / `Path` tiplerini kullanırsan compile patlar:

```
no suitable method found for thenReturn(jakarta.persistence.criteria.CriteriaBuilder)
  method ...thenReturn(org.hibernate.query.criteria.HibernateCriteriaBuilder) is not applicable
```

**Why:** Hibernate 6'da `Session.getCriteriaBuilder()` ve dolayısıyla tüm Criteria fluent API covariant olarak Hibernate-spesifik subtype döndürür. Mockito generic inference jakarta tipini almaz.

**How to apply:** Mock'larda şu eşlemeyi kullan:

| jakarta.persistence.criteria.* | org.hibernate.query.criteria.* |
|--------------------------------|-------------------------------|
| `CriteriaBuilder`              | `HibernateCriteriaBuilder`    |
| `CriteriaQuery<T>`             | `JpaCriteriaQuery<T>`         |
| `Root<T>`                      | `JpaRoot<T>`                  |
| `Path`                         | `JpaPath`                     |
| `Predicate`                    | `JpaPredicate`                |

Üretim DAO kodu jakarta tiplerini kullanmaya devam edebilir (covariant return zaten geriye uyumlu), sadece **mock**'larda Hibernate tipleri zorunlu. Örnek: `src/test/java/com/stemsep/dao/UserDaoTest.java` → `stubCriteriaQuery()` helper.
