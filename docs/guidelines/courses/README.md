# Ders Dökümanları Dizini

Bu klasör, **BM470 İleri Java Programlama** (Doç. Dr. Talha KABAKUŞ — Düzce Üniversitesi MF BM) ders materyallerinin özetlenmiş hâlini barındırır.

## Mevcut Dökümanlar

| Dosya | Kapsam | Slayt/Konu |
|-------|--------|------------|
| [ders7-8-9.md](ders7-8-9.md) | Ders 7-8-9 analizi | Spring Service & DAO + Hibernate Criteria Query API + Spring Log ile Loglama |
| [ders_kod_referansi.md](ders_kod_referansi.md) | Kod referansı (4-5-6 PDF'leri) | pom.xml, WebAppInitializer, WebConfig, Controller, Interceptor, i18n, JSP, JUnit |

## Eksik / Beklenen Dökümanlar

- ⚠️ **Ders 1-3** (`ilk3pdf.md`): `ders7-8-9.md` bu dosyaya referans veriyor ama henüz `courses/` altında yok. Java EE giriş, geliştirme ortamı, Maven konularını içeriyor.
- ⚠️ **Ders 4-5-6 PDF özetleri**: `ders_kod_referansi.md` bu PDF'lerden derlenmiş ama orijinal özet metni yok. Controller / Interceptor / i18n detayları için referans gerekli.

## Kullanım Kuralları

1. **Yeni özet eklerken**: Bu tabloya satır ekleyin, `INTEGRATION_GUIDELINES.md`'deki "Sıkı Kurallar" tablosunu güncelleyin.
2. **Kod yazarken**: Bu klasördeki dosyalar **bağlayıcıdır**. Modern best-practice ile çakışırsa ders kazanır.
3. **Çelişki bulursanız**: `ders7-8-9.md` §11 "Versiyon Çelişkileri" bölümüne bakın. Karar verilmemiş çelişkileri kullanıcıya sorun.

## Çekirdek Kararlar (Şimdiye Kadar)

| Konu | Karar |
|------|-------|
| log4j | 1.2.14 (1.x serisi) — log4j 2.x **DEĞİL** |
| slf4j | 1.7.25 |
| MySQL Driver | `mysql-connector-java` 8.0.28 (yeni `mysql-connector-j` 9.x değil) |
| Hibernate | 5.3.20.Final |
| Spring | 6.0.4 (Boot DEĞİL) |
| JUnit | 4.13.1 (5 değil) |
| Paket kökü | `tr.edu.duzce.mf.bm.bm470` |

## Şablon (Yeni Ders Eklerken)

```markdown
# [Ders Adı / PDF Numarası]

## Özet
Konunun kısa özeti (1 paragraf)

## Bağlayıcı Kurallar
- Kural 1 (sınıf annotation'ı, paket, vb.)
- Kural 2

## Kod Kalıpları
\`\`\`java
// derste verilen kod aynen
\`\`\`

## Splitnorder Domain'ine Etki
- Hangi service/DAO/entity etkilenir?
- Hangi mevcut kod uyumsuz?

## Çelişki / Açık Soru
- Varsa not et

## Son Güncelleme
YYYY-MM-DD — açıklama
```
