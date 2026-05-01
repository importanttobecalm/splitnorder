---
name: Proje Durumu
description: splitnorder güncel durumu ve sıradaki işler
type: project
updated: 2026-05-01
---

## Proje: splitnorder (BM470 İleri Java Programlama ödevi)

Müzik kaynak ayrımı (Demucs) — Java Spring backend + Python Flask audio servisi + JSP frontend.

### Mevcut Durum (2026-05-01)
- **Aktif dal:** `yusuf2` (origin/main 29dd84e'den açıldı)
- **Son commit:** `29dd84e deploy: setenv.sh ile JVM args + sade systemd unit`
- **Faz 3:** Oracle Compute deploy hazırlığı yapıldı
- Demucs Flask API entegre, Tomcat deploy hazır, Oracle Cloud MySQL bağlantısı yapıldı

### ✅ Tamamlanan
- Faz 1: Local Demucs Flask API + Java backend entegrasyonu
- Faz 2: UI fix + Tomcat deployment desteği
- v0.4.0: BM470 ders kurallarına uyum + Oracle Cloud MySQL
- v0.4.1: Hibernate 6.1.7'ye revert + Spring MVC controller scan fix
- Faz 3 hazırlığı: Oracle Compute deploy + GPU swap stratejisi
- setenv.sh ile JVM args + sade systemd unit

### 🚧 Açık Borçlar (ders teslimine göre)
- `com.stemsep` paketi → `tr.edu.duzce.mf.bm.bm470` migrasyonu
- Hibernate 6.1.7 → 5.3.20 düşürme (v0.4.1'de geri alındı, hâlâ borç)
- log4j 1.2.14 + slf4j 1.7.25 doğrulanmalı
- JUnit 4.13.1 doğrulanmalı
- i18n: TR + EN messages_*.properties

### Son Oturum Notu (2026-05-01)
- Memory sistemi kuruldu
- `yusuf2` dalı açıldı
- Geliştirmeye başlamak için hazır — sıradaki iş kullanıcıdan netleşecek
