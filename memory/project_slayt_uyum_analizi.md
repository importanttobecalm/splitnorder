---
name: Slayt Uyum Analizi (2026-05-12)
description: NotebookLM bm470 ile yapılan slayt uyum analizi — slayt-dışı / aşırı karmaşık tespit edilen alanlar ve önerilen aksiyonlar
type: project
---

## Bağlam
2026-05-12 tarihinde NotebookLM `bm470` sorgulanarak proje slaytlarının zorunlu/örnek ayrımı netleştirildi (NotebookLM conversation ID: `d109039c-2614-4999-a01c-7e1ce9ed6aba`). Sonrasında kod tabanı bu standarda karşı tarandı.

## Zorunlu Slayt Gereksinimleri (proje dokümanı [1] alıntısı)
- Spring MVC 6 + annotation + no-XML
- Hibernate + c3p0 + UZAK DB (localhost kabul değil)
- log4j + slf4j (console + dosya)
- JUnit (5, 3/4 deprecated)
- ≥2 dil (TR zorunlu)
- Rapor: Configuration, Controller, Interceptor, Service, DAO açıklanmalı

## Sadece Örnek / Tavsiye Olan (zorunlu DEĞİL)
- Lombok (%52 azaltma örneği)
- @ResponseStatus custom exception (tek `ResourceNotFoundException` örneği)
- JSP+JSTL (REST API de kabul)
- MockMvc test (örnek şablon, başka biçimler kabul)

## Tespit Edilen Slayt-Dışı / Aşırı Karmaşık Alanlar

### 🔴 Kritik (kaldırılması önerilen)
1. **`controller/GoogleAuthController.java` (131 satır)** + `google-http-client-jackson2` + `jackson-databind` (pom): Google OAuth2 slaytta yok, proje gereksiniminde yok.
2. **`config/CorsFilter.java` (67 satır)** + `WebAppInitializer.getServletFilters()` override: Eski Vite React frontend (`localhost:5173/3000`) için yazılmış. JSP'ye geçtikten sonra ÖLÜ KOD.
3. **`HistoryController.showHistory()`**: `return "redirect:http://localhost:5173/login"` — production'da kırık link. `/auth/login`'a çevrilmeli.

### 🟡 Orta (kalsın ama konsolide + raporda "ek özellik" olarak işaretle)
4. **`AuthService` (142 LOC) + `EmailService` (61 LOC) + `jakarta.mail`**: SHA-256 + 24h verification token + SMTP. Slaytta auth katmanı yok. Sil**me**, raporda ek özellik olarak ayrı bölümle.
5. **8 custom exception** (`exception/*.java`): Slayt 1 örnek veriyor; biz 8 tane yazdık. 3-4'e indirgenebilir (AuthException, UserException, InferenceFailedException).

### 🟢 Düşük (öneri)
6. **Controller testleri eksik**: Sadece `HomeControllerTest` var. Slayt MockMvc + `@WebAppConfiguration` örneği veriyor. `UploadControllerTest`, `JobControllerTest` eklenirse rapor "Açıklamalı birim testler" bölümü güçlenir.

## Why
Hoca slayt-dışı ekstra özellikler için bonus vermez, hatta "ders kapsamında değil, neden var?" eleştirisine zemin hazırlar. Ders kazandıracak şey slaytlardaki zorunlu yapıların **temiz ve raporlanabilir** uygulanması. Şu an proje 1910 LOC main + 16 JSP — ders ödevi için fazla geniş, savunması zorlaşır.

## How to apply
- Yeni Java değişikliklerinde önce NotebookLM `bm470` sorgula (feedback_notebooklm_first.md).
- Bu raporu uygulamadan önce kullanıcıya onay sor — aksiyonlar büyük etkili (Google login, CorsFilter kaldırma).
- Tahmini temizlik: ~240 satır net azalma + 1 dep düşüşü.

## Kullanıcı Kararı (2026-05-12)
Kullanıcı raporu inceledikten sonra aksiyonların **hiçbirinin** şu an uygulanmamasına karar verdi:
- **#1 (Google/CORS/HistoryController redirect):** Kod kalsın, kaldırmaya gerek yok.
- **#2 (Exception konsolidasyonu 8→3-4):** Şu an yapılmayacak.
- **#3 (MockMvc controller testleri eklenmesi):** Şu an yapılmayacak.

**Sonuç:** Kod tabanı bu rapor itibarıyla **olduğu gibi** bırakıldı. Rapor ileride savunma/sunum öncesi tekrar değerlendirilebilir, ama otomatik aksiyon yok. Bir sonraki Claude bu maddeleri kendi inisiyatifiyle uygulamasın — kullanıcı bilinçli olarak reddetti.
