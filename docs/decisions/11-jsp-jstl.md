# 11 — View: JSP + JSTL

**Statü:** ✅ Kabul

## Karar
View teknolojisi: JSP + JSTL. Thymeleaf, FreeMarker vb. **kullanılmıyor**.

## Neden
Slaytlarda View örnekleri JSP üzerinden gösterilmiştir. `WebConfig`'de `InternalResourceViewResolver` + `JstlView` setup'ı slayt referansıdır.

## Uygulama
- JSP konumu: `/WEB-INF/views/*.jsp` (dışarıdan direkt erişilemez).
- JSTL: `jakarta.tags.core` (Spring 6 = Jakarta EE namespace).
- ViewResolver:
  ```java
  resolver.setPrefix("/WEB-INF/views/");
  resolver.setSuffix(".jsp");
  resolver.setViewClass(JstlView.class);
  ```

## Kaynak
Ders slaytları — View Technologies bölümü.
