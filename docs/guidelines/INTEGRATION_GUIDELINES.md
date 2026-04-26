# Ders Dökümanları Entegrasyon Kuralları

> **Bağlayıcı kaynak:** `courses/ders7-8-9.md` ve `courses/ders_kod_referansi.md`.
> Modern Spring Boot, JPA Repositories, log4j2, JUnit 5, `@RestController` gibi **dersin DIŞINDAKİ** yapılar **kullanılmaz**.

## Sıkı Kurallar (Negative Constraints — DERSE AYKIRI)

| ❌ KULLANMA | ✅ KULLAN |
|-------------|-----------|
| Spring Boot, `@SpringBootApplication` | Saf Spring 6.0.4 + `WebApplicationInitializer` |
| `JpaRepository` / `CrudRepository` | `@Repository` + Hibernate `Session` + `CriteriaBuilder` |
| `@RestController` | `@Controller` + `@ResponseBody` (gerekiyorsa) |
| log4j 2.x (`org.apache.logging.log4j.*`) | log4j 1.2.14 (`org.apache.log4j.*`) + slf4j 1.7.25 |
| JUnit 5 (`org.junit.jupiter.*`) | JUnit 4.13.1 (`org.junit.Test`) |
| Hibernate 6.x | Hibernate 5.3.20.Final |
| `application.properties` (Boot stili) | `hibernate.properties` (classpath) + `@PropertySource` |
| H2 in-memory | MySQL (ders MySQL gösteriyor) |
| `com.stemsep.*` paketi | `tr.edu.duzce.mf.bm.bm470.*` |

## Pozitif Kurallar (Derste Açıkça Geçen)

### Service Katmanı (`ders7-8-9.md §7.2`)
- `@Service` + sınıf düzeyi `@Transactional(readOnly = true, rollbackFor = RuntimeException.class)`
- Yazma metotlarında `@Transactional(readOnly = false)` override edilir
- DAO'ya `@Autowired` ile field injection (derste constructor injection gösterilmemiş)

### DAO Katmanı (`ders7-8-9.md §7.3, §8`)
- `@Repository` + `@Autowired private SessionFactory sessionFactory;`
- Sorgular **CriteriaBuilder API** ile yazılır (HQL/native SQL değil)
- `Session session = sessionFactory.getCurrentSession();`

### Entity (`ders7-8-9.md §7.9, §7.12`)
- `implements Serializable`
- `@Entity`, `@Table(name="...")`, `@Column(name="...", nullable=...)`
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
- Lombok: `@Getter @Setter` field bazlı, `@NoArgsConstructor @AllArgsConstructor` sınıf bazlı
- İlişkiler: `@ManyToOne` + `@JoinColumn`, `@OneToMany(mappedBy=..., cascade=ALL, orphanRemoval=true)`

### Config (`ders7-8-9.md §7.7`)
- `@Configuration @EnableTransactionManagement @ComponentScan(basePackages = "tr.edu.duzce")`
- `@PropertySource(value = "classpath:hibernate.properties", encoding = "UTF-8")`
- `LocalSessionFactoryBean` + `HibernateTransactionManager` `@Bean`
- `setAnnotatedClasses(...)` ile tüm entity'ler tanıtılır

## Workflow

### Yeni Feature Eklerken
1. İlgili ders bölümünü `courses/` altından oku (CriteriaQuery için §8, loglama için §9)
2. `PROJECT_ARCHITECTURE.md` paket yapısına uygun konumla
3. Yukarıdaki "Sıkı Kurallar" tablosuyla çapraz kontrol
4. CHANGELOG.md güncelle

### Yeni Ders Materyali Eklenince
1. Özet `courses/[isim].md` olarak konur
2. `courses/README.md`'ye listele
3. Yeni kural çıktıysa bu dosyaya (sıkı/pozitif kurallar tablosuna) ekle
4. `PROJECT_ARCHITECTURE.md` "Güncellemeler" satırına tarih düş

## Mevcut Proje ile Uyumsuzluklar (Bilinçli Borç)

Mevcut kod (`com.stemsep`, Hibernate 6.1.7, H2) ders teslimi için **uyumsuz**. Migrasyon planı:
1. Paket adı: `com.stemsep.*` → `tr.edu.duzce.mf.bm.bm470.*`
2. Hibernate 6.1.7 → 5.3.20 (jakarta.persistence yerine javax.persistence dikkat — Spring 6 jakarta'yı zorlar; bu çelişki ders metninde de var, çözüm `INTEGRATION_GUIDELINES.md` revizyonunda kararlaştırılacak)
3. H2 → MySQL (`hibernate.properties`)
4. `application.properties` → `hibernate.properties` + `@PropertySource`

## Checkpoint

- [ ] Ders bölümü okundu (PDF referansı not alındı)
- [ ] Paket yolu `tr.edu.duzce.mf.bm.bm470.*`
- [ ] "Sıkı Kurallar" tablosu ihlal edilmedi
- [ ] CriteriaBuilder kullanıldı (HQL/native değil)
- [ ] Lombok + Hibernate annotation'ları doğru
- [ ] CHANGELOG ve mimari dokümantasyon güncellendi
