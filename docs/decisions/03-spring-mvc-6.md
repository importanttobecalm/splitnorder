# 03 — Spring MVC 6.0.4 + Annotation Config

**Statü:** ✅ Kabul

## Karar
Spring 6.0.4 + saf annotation config. Spring Boot **kullanılmayacak**.

## Neden
NotebookLM citation [1] (proje gereksinimleri):

> *"Annotation'larla yapılandırılmış **Spring MVC 6** mimarisi kullanılarak web projesinin inşa edilmesi"*

Slaytlardaki `pom.xml` örneği: `<spring.framework.version>6.0.4</spring.framework.version>` (citation [2]).

## Uygulama
- `@Controller`, `@Service`, `@Repository`, `@Configuration`, `@ComponentScan`, `@EnableTransactionManagement`.
- Yasak: `@SpringBootApplication`, `spring-boot-starter-*`, `application.properties` Boot stili.
- Property dosyaları için `@PropertySource("classpath:hibernate.properties")` (manuel).

## Kaynak
NotebookLM citations [1], [2], [3].
