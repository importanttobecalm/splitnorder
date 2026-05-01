# 02 — Hibernate 6.1.7 Kalır

**Statü:** ✅ Kabul (5.3.20'ye düşürme iptal)

## Karar
Hibernate 6.1.7.Final korunacak. 5.3.20'ye downgrade yapılmayacak.

## Neden
NotebookLM sorgusu (`bm470`, "Hibernate sürüm zorunluluğu"):

> *"Proje teslim kurallarını belirten belgede Hibernate için **kesin bir sürüm zorunluluğu belirtilmemiştir**. İlgili gereksinim sadece: 'Veri katmanında veri erişim kütüphaneleri Hibernate ve c3p0'nun kullanılması'."*

Slaytlardaki örneklerde 5.3.20.Final görünüyor ama **örnek**, zorunluluk değil. Spring 6 + Jakarta EE uyumluluğu için Hibernate 6.x doğru tercih (v0.4.0'da downgrade denendi, runtime hataları çıktı, v0.4.1'de revert edildi — `gotchas.md`).

## Uygulama
- `pom.xml` `<hibernate.version>6.1.7.Final</hibernate.version>` kalır.
- `HibernateConfig.java` mevcut hali korunur.

## Kaynak
NotebookLM citation [1] (proje gereksinimleri) + [2] (slayt örnek pom).
