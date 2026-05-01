# 06 — `@Autowired` Field Injection

**Statü:** ✅ Kabul

## Karar
DI tarzı: `@Autowired` private alan üzerinde. Constructor injection **kullanılmıyor**.

## Neden
Modern best-practice constructor injection olsa da, derste field injection gösterildi. Ders kuralı önceliklidir.

## Uygulama
```java
@Service
public class JobService {
    @Autowired
    private JobDao jobDao;
}
```

Yasak: `public JobService(JobDao jobDao) { this.jobDao = jobDao; }` tarzı constructor DI.

## Kaynak
Ders slaytları — Spring DI bölümü (NotebookLM'e konu özelinde sorgu yapılabilir).
