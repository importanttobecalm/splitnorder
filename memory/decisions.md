---
name: Kararlar
description: Mimari ve teknik kararlar (ADR lite)
type: project
updated: 2026-05-01
---

## 2026-05-01 — yusuf2 dalı, yusufun-dali yerine
**Bağlam:** Kullanıcı yusufun-dali'yi terk etti, origin/main güncel halinden yeni dal istedi.
**Karar:** `yusuf2` dalı `origin/main` (29dd84e) üzerinden açıldı.
**Sonuç:** yusufun-dali dalı korunuyor (silinmedi), yusuf2 üzerinde geliştirme yapılacak.

## 2026-04 — Hibernate 6.1.7 (v0.4.1 revert)
**Bağlam:** v0.4.0 ile ders kurallarına uymak için 5.3.20 hedeflenmişti, ama derleme/test sorunları çıktı.
**Karar:** v0.4.1 ile geçici olarak Hibernate 6.1.7'ye geri dönüldü.
**Sonuç:** Ders teslimine kadar 5.3.20'ye düşürme borç olarak kaldı.

## Erken karar — Saf Spring 6.0.4 (Boot YOK)
**Bağlam:** BM470 dersi Spring Boot kullanmıyor.
**Karar:** `WebApplicationInitializer` + war packaging + Tomcat deploy.
**Sonuç:** `@SpringBootApplication`, `application.properties` Boot stili yasak.
