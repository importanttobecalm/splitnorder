# 10 — i18n: TR + EN

**Statü:** ✅ Kabul

## Karar
İki dil zorunlu: Türkçe + İngilizce. `messages_tr_TR.properties` + `messages_en_US.properties`.

## Neden
NotebookLM citation [1] (proje gereksinimleri):

> *"En az 2 dil desteğinin sunulması (Türkçe zorunlu)"*

## Uygulama
- `WebConfig`: `ReloadableResourceBundleMessageSource` (basename: `classpath:messages`) + `LocaleResolver` + `LocaleChangeInterceptor` (`?lang=tr` / `?lang=en`).
- JSP'lerde `<spring:message code="..."/>` ile çekilir.
- Yeni metin eklenirken **iki dosyaya da ekle**.

## Kaynak
NotebookLM citation [1].
