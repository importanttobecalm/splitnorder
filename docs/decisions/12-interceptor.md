# 12 — Interceptor: Request Logging

**Statü:** ✅ Kabul

## Karar
Cross-cutting log için `HandlerInterceptor` kullanılır. Mevcut: `RequestLoggingInterceptor`.

## Neden
NotebookLM citation [1] (proje gereksinimleri):

> *"Bütün isteklerde, (varsa) istek parametre ve değerleri ile geri dönüş görüntüsünün/verisinin log4j ve slf4j kullanılarak loglara basılması, logların konsolun yanı sıra dosya sistemine kaydedilmesi"*

Bu zorunluluk **interceptor** ile karşılanır (her controller'a tek tek eklemek yerine).

## Uygulama
- `com.stemsep.interceptor.RequestLoggingInterceptor` → `preHandle`/`postHandle`/`afterCompletion`.
- `WebConfig.addInterceptors()` ile kayıt.
- Log4j RollingFileAppender (`log4j.properties`) → `logs/app.log`.

## Yeni Cross-Cutting Görevler
Authentication, audit, performance metrics → yeni interceptor + `WebConfig` registrasyonu.

## Kaynak
NotebookLM citation [1].
