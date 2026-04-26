# Kod Yazım Standartları

> **Bağlayıcı:** `courses/ders_kod_referansi.md` ve `courses/ders7-8-9.md`. Çakışma olduğunda ders kazanır.

## Backend (Java)

### Paket
- Kök: `tr.edu.duzce.mf.bm.bm470`
- Alt paketler sabit: `config`, `web`, `service`, `dao`, `interceptor`, `exception`, `model`

### Naming
- Sınıf: `PascalCase` (örn. `OgrenciService`, `OgrenciDAO` — DAO büyük harf)
- Metot/değişken: `camelCase` (örn. `ogrencileriYukle`, `saveOrUpdateOgrenci`)
- DB sütunları: `snake_case` (örn. `dogum_tarihi`, `bolum_id`) — `@Column(name = "...")` ile eşle
- Tablo adı: snake_case (örn. `ogrenci`, `bolum`) — `@Table(name = "...")` ile

### Annotation Sırası (Sınıf Düzeyi)
```java
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = RuntimeException.class)
public class OgrenciService { ... }
```

```java
@Entity
@Table(name = "ogrenci")
@NoArgsConstructor
@AllArgsConstructor
public class Ogrenci implements Serializable { ... }
```

### Field Düzeyi (Entity)
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "ogrenci_id", nullable = false)
@Getter @Setter
private Long ogrenciId;
```

### Dependency Injection
- Field injection (`@Autowired private OgrenciDAO ogrenciDAO;`) — derste böyle gösteriliyor
- Constructor injection **kullanılmıyor** (modern best practice olsa da derste yok)

### Loglama
```java
private static final Logger logger = LoggerFactory.getLogger(SinifAdi.class);
// SLF4J 1.7.25 → org.slf4j.Logger, org.slf4j.LoggerFactory
```

### CriteriaBuilder Kalıbı (DAO içinde)
```java
Session session = sessionFactory.getCurrentSession();
CriteriaBuilder cb = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> cq = cb.createQuery(Ogrenci.class);
Root<Ogrenci> root = cq.from(Ogrenci.class);
cq.select(root).where(cb.equal(root.get("sinifi"), 2));
return session.createQuery(cq).getResultList();
```

## Frontend

### JSP
- `WEB-INF/views/` altında
- JSTL kullan (`<c:forEach>`, `<c:if>`, `<fmt:message key="..."/>` i18n)
- Inline scriptlet (`<% %>`) **kaçınılır** — derste de minimum

### CSS / JS
- CSS class: kebab-case
- JS: ES6+, vanilla JS yeterli (framework zorunlu değil)

## Audio Service (Python Flask)

- PEP 8
- Virtual environment (`venv/`)
- `requirements.txt` versiyon-pinned

## Dokümantasyon

- Karmaşık iş mantığı yorum satırı ile açıklanır (Türkçe yorum kabul)
- JavaDoc public metotlarda zorunlu (ders teslimi için `maven-javadoc-plugin` çalıştırılır)
- Trivial yorumdan kaçın (örn. `// id'yi ata` gibi getter/setter yorumu yazma)

## Genel Yasaklar

- ❌ `System.out.println` (logger kullan)
- ❌ Static state (config dışında)
- ❌ Boot-stili `application.properties`
- ❌ Modern annotation'lar (`@RestController`, `@SpringBootApplication`, vb.)
