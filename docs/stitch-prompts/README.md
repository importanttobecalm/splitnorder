# Splitnorder — Stitch Arayüz Promptları

Stitch (stitch.withgoogle.com) için sıralı, kopyala-yapıştır hazır ekran tasarım promptları.

## Kullanım Sırası

**ÖNCE** `00-design-system.md` dosyasını Stitch'e ver — tüm renk/font/şekil dilini öğrensin. Sonra ekranları sırayla:

| # | Dosya | Ekran | JSP Hedef |
|---|-------|-------|-----------|
| 00 | [00-design-system.md](00-design-system.md) | **Design System (foundation)** | (global CSS) |
| 01 | [01-login.md](01-login.md) | Giriş | `auth/login.jsp` (yeni) |
| 02 | [02-register.md](02-register.md) | Kayıt | `auth/register.jsp` (yeni) |
| 03 | [03-home-landing.md](03-home-landing.md) | Ana sayfa / hoş geldin | `home.jsp` |
| 04 | [04-upload.md](04-upload.md) | Dosya yükleme | `upload.jsp` |
| 05 | [05-processing.md](05-processing.md) | İşleme bekleme | `processing.jsp` |
| 06 | [06-studio-result.md](06-studio-result.md) | **Studio / ayrım sonuç (ana ekran)** | `result.jsp` |
| 07 | [07-history.md](07-history.md) | Geçmiş işlemler | `history.jsp` |
| 08 | [08-profile-settings.md](08-profile-settings.md) | Profil / ayarlar | `profile.jsp` (yeni) |
| 09 | [09-error-404.md](09-error-404.md) | Hata / 404 | `error.jsp` (yeni) |

## Akış

```
Login → Home → Upload → Processing → Studio(Result) ↘
                                                      → History
                                          Profile ↗
```

## Akıl Notu

- Her prompt **bağımsız** çalışır, fakat Stitch'in design system'i hatırlaması için aynı oturumda sırayla ver.
- Stem renkleri (vocals=kırmızı, drums=turuncu, bass=mor, other=teal) **tüm ekranlarda aynı** kalmalı.
- Çıktı geldiğinde HTML/CSS'i bana ver — JSP+JSTL'e ben gömerim (i18n key'leriyle).
