---
name: Kararlar
description: Mimari/teknik meta-kararlar (detaylı ADR'lar için docs/decisions/)
type: project
updated: 2026-05-01
---

> **NOT:** Bu dosya kısa meta-kararlardır. Konu bazlı tam gerekçe + slayt alıntısı için **`docs/decisions/`** altındaki ADR'lara bak (CLAUDE.md tablosundan link).

## 2026-05-01 — Memory dosyaları repo'ya dahil
**Bağlam:** Memory sistemi başlangıçta `.gitignore`'daydı; klonlayan ekip hafızasız başlıyordu.
**Karar:** `memory/user_*.md` hariç tüm memory dosyaları git'te. CLAUDE.md ÖNCELİKLİ OKUMA listesine 5 memory dosyası eklendi.
**Sonuç:** `git clone` yapan kişi/Claude doğrudan ortak hafızayla başlar.

## 2026-05-01 — ADR sistemi birincil belge
**Bağlam:** Eski `INTEGRATION_GUIDELINES.md` ve `PROJECT_ARCHITECTURE.md` belgelerinde NotebookLM ile çelişen sıkı kurallar vardı (paket migrasyonu, Hibernate 5.3.20, JUnit 4 zorunluluğu).
**Karar:** 12 ADR (`docs/decisions/NN-konu.md`) NotebookLM citation'larıyla yazıldı. Çelişki olursa **ADR doğrudur**.
**Sonuç:** Eski borç listesinin çoğu iptal (paket, Hibernate sürümü); JUnit ve log4j slayta hizalandı; CriteriaBuilder eklendi.

## 2026-05-01 — NotebookLM-First geliştirme akışı
**Bağlam:** Modern best-practice ile ders kuralları çakışınca ders kazanır kuralı vardı; ama "ders kuralı" ne diye bilinmesi için kaynağa bakılmalı.
**Karar:** Kod yazmadan ÖNCE NotebookLM `bm470` notebook'una sor. Detay: `memory/feedback_notebooklm_first.md`.
**Sonuç:** Bu oturumda 4 kritik sorgu → eski varsayımların 4'ü revize edildi.

## 2026-05-01 — yusuf2 dalı, yusufun-dali yerine
**Bağlam:** Kullanıcı yusufun-dali'yi terk etti, origin/main güncel halinden yeni dal istedi.
**Karar:** `yusuf2` dalı `origin/main` (29dd84e) üzerinden açıldı.
**Sonuç:** yusufun-dali korunuyor (silinmedi), yusuf2 üzerinde geliştirme yapıldı + push edildi.

## 2026-04 — Hibernate 6.1.7 (v0.4.1 revert) — KARAR KESİNLEŞTİ
**Bağlam:** v0.4.0 ile 5.3.20 hedeflenmişti, derleme/runtime sorunları çıktı, v0.4.1'de revert.
**Karar:** Hibernate 6.1.7 KALICI (ADR 02). NotebookLM doğruladı: ders kesin sürüm zorunlu kılmıyor.
**Sonuç:** Önceki "borç" notu iptal.

## Erken karar — Saf Spring 6.0.4 (Boot YOK)
**Bağlam:** BM470 dersi Spring Boot kullanmıyor.
**Karar:** `WebApplicationInitializer` + war packaging + Tomcat deploy.
**Sonuç:** `@SpringBootApplication`, `application.properties` Boot stili yasak. Detay: ADR 03 + ADR 04.
