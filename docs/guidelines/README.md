# Proje Yönergeleri & Ders Entegrasyonu

> **splitnorder** — BM470 İleri Java Programlama dersi için müzik ayrım projesi.
> Düzce Üniversitesi MF BM | Doç. Dr. Talha KABAKUŞ.

## Yapı

```
docs/guidelines/
├── README.md                      ← Bu dosya (giriş)
├── PROJECT_ARCHITECTURE.md        ← Mimari, paket yapısı, 3 katmanlı şema
├── INTEGRATION_GUIDELINES.md      ← Ders kurallarına uyum (sıkı/pozitif kurallar)
├── CODING_STANDARDS.md            ← Naming, annotation sırası, DI, loglama kalıpları
├── DEPLOYMENT_GUIDE.md            ← Tomcat deploy, lokal test
└── courses/
    ├── README.md                  ← Ders dökümanları dizini, çekirdek kararlar
    ├── ders7-8-9.md               ← Service/DAO + Criteria + Loglama (1923 satır)
    └── ders_kod_referansi.md      ← Kod referansı (1006 satır)
```

## Okuma Sırası (Yeni Konuşma Açıldığında)

1. **Bu dosya** — genel bakış
2. `PROJECT_ARCHITECTURE.md` — paket yapısı ve katman şeması
3. `INTEGRATION_GUIDELINES.md` — **sıkı kurallar tablosu (kullanma/kullan)**
4. Çalışılan konuya göre ilgili `courses/*.md` bölümü
5. `CODING_STANDARDS.md` — kod yazarken referans

## Çekirdek Prensipler

- **Ders bağlayıcıdır**: Modern best-practice ders ile çakışırsa **ders kazanır**
- **Spring Boot YOK**: Saf Spring 6.0.4 + `WebApplicationInitializer`
- **Hibernate 5.3.20**: Hibernate 6.x değil; CriteriaBuilder zorunlu, HQL/native opsiyonel
- **Paket kökü `tr.edu.duzce.mf.bm.bm470`**: `com.stemsep` mevcut kod ders teslimine uyumsuz, migrasyon gerekecek
- **i18n zorunlu**: TR + EN `messages_*.properties`
- **log4j 1.2.14 + slf4j 1.7.25**: log4j 2.x değil

## Son Güncellemeler

- **2026-04-25**: Ders dökümanları (ders7-8-9, kod referansı) entegre edildi. Tüm guideline'lar ders gereksinimlerine göre revize edildi.
- **2026-04-22**: İlk yapı oluşturuldu.
