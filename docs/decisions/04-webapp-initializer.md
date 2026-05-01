# 04 — `WebApplicationInitializer` (web.xml YOK)

**Statü:** ✅ Kabul

## Karar
Servlet bootstrap için Java sınıfı kullanılır: `WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer`. `web.xml` **yok**.

## Neden
Slaytlardaki Spring MVC 6 örneklerinde Java-based bootstrap gösterilmiştir. `pom.xml`'de `<failOnMissingWebXml>false</failOnMissingWebXml>` (slayt citation [4]).

## Uygulama
- `com.stemsep.config.WebAppInitializer`:
  - `getRootConfigClasses()` → `AppConfig.class`, `HibernateConfig.class`
  - `getServletConfigClasses()` → `WebConfig.class`
  - `getServletMappings()` → `"/"`
- Filter/listener/multipart config Java tarafında.

## Kaynak
Slaytlar — bootstrap & DispatcherServlet bölümü.
