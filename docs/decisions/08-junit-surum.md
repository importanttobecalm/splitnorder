# 08 — JUnit 5 (Jupiter) Kullanılır

**Statü:** ✅ Kabul (2026-05-01 — JUnit 4 → 5 geçişi tamamlandı)

## Karar
JUnit 5 (Jupiter) kullanılır. JUnit 4 kaldırıldı.

## Neden
NotebookLM citation [8] (test slaytları):

> *"JUnit 3/4 deprecated!"*

Slayttaki birebir bağımlılık (citation [1]):
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>6.0.3</version>
    <scope>test</scope>
</dependency>
```

## Uygulama
**`pom.xml`:**
- `junit-jupiter-api` 6.0.3 + `junit-jupiter-engine` 6.0.3 (Surefire için)
- Eski `junit:junit:4.13.1` kaldırıldı
- Mockito 5.14.2'ye yükseltildi (JUnit 5 uyumu)

**Test sınıflarında:**
| Eski (JUnit 4) | Yeni (JUnit 5) |
|---|---|
| `org.junit.Test` | `org.junit.jupiter.api.Test` |
| `org.junit.Before` | `org.junit.jupiter.api.BeforeEach` |
| `org.junit.BeforeClass` | `org.junit.jupiter.api.BeforeAll` |
| `org.junit.AfterClass` | `org.junit.jupiter.api.AfterAll` |
| `org.junit.Assert` | `org.junit.jupiter.api.Assertions` |
| `assertX(msg, ...)` | `assertX(..., msg)` (msg arg sona) |

**Spring Controller test iskeleti (slayt citation [4-6]):**
```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WebConfig.class, AppConfig.class})
@WebAppConfiguration
public class TestOgrenciController {
    @Autowired private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
}
```

## Migrate Edilen Dosyalar
- `HomeControllerTest`, `JobDaoTest`, `UserDaoTest`, `JobServiceTest`, `ColabInferenceServiceTest`, `OracleMySQLConnectionTest`

## Kaynak
NotebookLM citations [1], [2], [3], [4], [5], [6], [7], [8].
