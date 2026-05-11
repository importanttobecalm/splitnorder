# Track C — Rapor Markdown Taslağı (sablon.docx için)

> Bu prompt'u yeni bir Claude Code oturumunda `claude` komutuyla aç, prompt olarak ver. Track A (JSP) ve Track B (backend) paralel yürüyor — bu track yalnızca `docs/report/` altına yazar, kod dosyalarına dokunmaz.

## Proje Bağlamı

`/Users/yusufbulut/Documents/Projelerim/JAVAodev/splitnorder` — BM470 İleri Java Programlama final ödevi (Düzce Üniv. MF BM, Doç. Dr. Abdullah Talha Kabakuş, 2026 Bahar). Müzik kaynağı ayrımı (Demucs) — Java Spring 6 + JSP + Python Flask.

**Önce oku (zorunlu sıra):**
1. `CLAUDE.md` — proje kararları, ADR tablosu
2. `memory/MEMORY.md` + linkler
3. `docs/decisions/` — tüm 12 ADR (mimari kararların gerekçesi)
4. `sablon.docx` — şablon yapısı (BM401/BM498 Mühendislik Tezi şablonu, BM470 final raporu için aynısı kullanılıyor)

**Şablon yapısı (sablon.docx'ten çıkarıldı):**
- Kapak • Değerlendirme tutanağı • BEYAN • AI Kullanım Beyanı • TEŞEKKÜR
- ÖZET (TR) + ABSTRACT (EN)
- İÇİNDEKİLER • ŞEKİL/ÇİZELGE/HARİTA LİSTESİ • KISALTMALAR • SİMGELER
- 1. GİRİŞ
- 2. MATERYAL VE YÖNTEM (teknoloji stack + mimari)
- 3. BÖLÜM 3 (bileşen açıklamaları)
- 4. BÖLÜM 4 (request URI tablosu + endpoint detayları)
- 5. BULGULAR VE TARTIŞMA (i18n, log, test, ekran görüntüleri)
- 6. KAYNAKLARIN YAZIMI (referans tekniği örneği)
- 7. ÖRNEK KAYNAK LİSTESİ
- 8. SONUÇLAR VE ÖNERİLER
- 9. KAYNAKLAR
- 10. EKLER (log örnekleri, açıklamalı testler, ekran görüntüleri)

## Proje Bilgileri (kullanıcıdan onaylı)

- **Ders:** BM470 İleri Java Programlama, Final Ödevi
- **Hoca:** Doç. Dr. Abdullah Talha KABAKUŞ
- **Akademik yıl:** 2026 Bahar
- **Teslim:** 13.05.2026
- **Ekip:**
  - Berkan UZ — 201001026
  - Muhammet Yusuf BULUT — 231002352
  - Alirıza LAÇIN — 231002301
- **Proje başlığı:** "Splitnorder — Müzik Kaynak Ayrımı Web Uygulaması" (kısa) / "Splitnorder — Music Source Separation Web Application"
- **Demo URL:** https://splitnorder.space/
- **GitHub:** https://github.com/importanttobecalm/splitnorder
- **Logo:** Üniversite logosu sablon içinde gömülü, dokunma

## Hedef

`docs/report/rapor.md` — kullanıcının Word'e (sablon.docx'e) kopyala-yapıştır yapabileceği, **bölüm bölüm hazır** rapor taslağı.

Her bölüm Word'e dökülmeye uygun başlık + içerik formatında.

## Yapılacak İşler

### C.1 — Dizin oluştur
- `docs/report/rapor.md` — ana taslak
- `docs/report/screenshots/` — Track A bitince Playwright ekran görüntüleri buraya gelecek
- `docs/report/diagrams/` — ER diyagramı, mimari blok şeması mermaid kaynak dosyaları

### C.2 — Kapak + Beyan + AI Beyan + Teşekkür
- Şablondan kapak formatını çıkar, ekip bilgisini yerleştir
- BEYAN sayfası: standart akademik beyan (şablondaki metni kullan, tarihi 03.05.2026 → 13.05.2026 olarak güncelle)
- **AI KULLANIM BEYANI:** Dürüst beyan — bu projede Claude Code (Anthropic) kullanıldı. Hangi amaçlarla:
  - Mimari karar tartışması (slayt-uyumluluk için)
  - Kod refaktörü (slayt diline çevirme)
  - JSP tasarımı (Stitch çıktısının JSP'ye uyarlanması)
  - Dokümantasyon taslağı
  - **Kontrol:** Tüm AI çıktısı ekip tarafından gözden geçirildi, slayt referansına göre doğrulandı
- TEŞEKKÜR: kısa, hoca + üniversite + ekip arkadaşları için

### C.3 — Özet (TR) + Abstract (EN)
- 1 paragraf, 1 sayfayı aşmasın
- Anahtar kelimeler (TR + EN, 3-5 adet):
  - TR: müzik kaynak ayrımı, Demucs, Spring MVC, Hibernate, JSP
  - EN: music source separation, Demucs, Spring MVC, Hibernate, JSP

### C.4 — 1. GİRİŞ
- Problem: Müzik prodüksiyonunda stem ayrımı manuel zor; AI ile otomatize edilebilir
- Amaç: Yapay zeka destekli (Demucs) müzik stem ayrımı sunan web uygulaması
- Kapsam: Spring MVC 6, Hibernate, JSP, Python Flask, MySQL
- Motivasyon: Hem öğrenci dostu ücretsiz hizmet hem ders ödevi için kapsamlı CRUD + AI entegrasyon örneği

### C.5 — 2. MATERYAL VE YÖNTEM
Alt başlıklar:
- 2.1 **Teknoloji Yığını** (`docs/decisions/`'tan derle): Spring 6.0.4, Hibernate 6.1.7, c3p0, Oracle MySQL, JSP+JSTL, Tomcat 10, JDK 21, Maven, Demucs+Flask, Stitch (UI)
- 2.2 **Mimari Blok Şeması** — mermaid ile çiz: Browser → Tomcat (Spring MVC) → DB(MySQL) + Demucs (Flask/GPU). Bu şemayı `docs/report/diagrams/mimari.mmd` dosyasına yaz.
- 2.3 **Veritabanı Şeması + ER Diyagramı** — `users`, `jobs`, `stems` tablolarını alan bazında tablo + mermaid ER diyagramı (`docs/report/diagrams/er.mmd`). Kaynak: `src/main/java/com/stemsep/model/{User,Job,Stem}.java`
- 2.4 **Veri Akış Şeması** — Upload → Save Job → Demucs API → Create Stem records → Display

### C.6 — 3. BÖLÜM 3 (Bileşen Açıklamaları)
- **Bu rubriğin en önemli maddesi.** "Configuration, Controller, Interceptor, Service, DAO" kullanım amaçlarıyla açıklanmalı.
- Her bileşen için 3-5 cümlelik açıklama:
  - 3.1 Configuration sınıfları: `WebAppInitializer`, `WebConfig`, `AppConfig`, `HibernateConfig`
  - 3.2 Controller sınıfları: `HomeController`, `AuthController`, `UploadController`, `JobController`, `HistoryController`
  - 3.3 Service sınıfları: `AuthService`, `UserService`, `JobService`, `StemService`, `EmailService`, `ColabInferenceService`
  - 3.4 DAO sınıfları: `UserDao`, `JobDao`, `StemDao` (slayt birebir `CriteriaBuilder` kullanımı)
  - 3.5 Interceptor: `RequestLoggingInterceptor` (rubrik gereği parametre/dönüş loglama), `AuthInterceptor` (oturum koruma)
  - 3.6 Filter: `CharacterEncodingFilter` (UTF-8 sağlama)

Track B çalışırken bu sınıfların satır sayısı/içeriği değişiyor olabilir — `git log` ile kontrol et veya doğrudan dosyaları oku.

### C.7 — 4. BÖLÜM 4 (Request URI Tablosu)
**Rubriğin diğer kritik maddesi.** Tablo formatı:

| URI | HTTP | Parametreler (tip) | Dönüş | Açıklama |
|-----|------|---------------------|-------|----------|
| `/` | GET | - | JSP `home` | Ana sayfa |
| `/auth/login` | GET | - | JSP `auth/login` | Giriş sayfası |
| `/auth/login` | POST | email (String), password (String) | redirect:/ veya JSP `auth/login` | Form-based login |
| ... | ... | ... | ... | ... |

Tüm endpoint'leri Controller dosyalarından tara, listele.

### C.8 — 5. BULGULAR VE TARTIŞMA
- 5.1 **i18n çalışma örnekleri** — TR/EN ekran görüntüleri yan yana
- 5.2 **Log örnekleri** — `logs/splitnorder.log` kesitleri (Track B FileAppender ekleyince üretilecek)
- 5.3 **Test sonuçları** — `mvn test` çıktısı + her test sınıfı için açıklama (Track B testleri MockMvc'ye çevirince güncelle)
- 5.4 **Ekran görüntüleri** — Stitch tasarımının çalışan uygulamadaki hali (Playwright ile alınacak, Track A bitince)

### C.9 — 6-7. Kaynak Yazımı + Örnek Liste
- Şablondaki kaynak gösterim formatı (nümerik veya Harvard — şablonda hangi varsa)
- 5-10 referans:
  - Spring Framework Reference Documentation
  - Hibernate ORM 6.x User Guide
  - Jakarta EE 10 Servlet Specification
  - Demucs paper (Defossez et al.)
  - JSTL spec
  - HAuth0/Google OAuth 2.0 RFC
  - Düzce Üniv. BM470 ders sunumları (Kabakuş)

### C.10 — 8. SONUÇLAR VE ÖNERİLER
- Projeden elde edilen sonuçlar: Spring 6 + Hibernate 6 + JSP slayt-uyumlu çalışan uygulama
- Karşılaşılan zorluklar: Spring 6 ↔ Hibernate 5 JtaPlatform uyumsuzluğu (ADR 02), MySQL bastion tunnel
- Gelecek öneriler: WebSocket ile realtime stem progress, multi-language genişletme, Stripe entegrasyon

### C.11 — 9. KAYNAKLAR
- Yukarıdaki referanslar APA veya IEEE formatında

### C.12 — 10. EKLER
- Ek 1: `logs/splitnorder.log` kesiti (örnek 50 satır)
- Ek 2: Açıklamalı birim testler — her test sınıfı için kaynak kod + 3-5 cümlelik amacı (Track B refactor edince güncelle)
- Ek 3: Ekran görüntüleri (Playwright çıktıları)
- Ek 4: Bileşen kodlarından önemli kesitler (örn. `UserDao.findByEmail` Criteria örneği)

## Kurallar

- **Slayt referansı**: Her teknik kararın slayt'taki karşılığı varsa belirt (örn. "Slayt §3 'JSP Bileşenleri'nde gösterilen `<%@ taglib %>` yönergesi kullanıldı")
- **Türkçe yazım**: TDK kuralları, kısa cümle, akademik dil
- **Markdown çıktısı**: Word'e kopyalanmaya uygun (tablolar, başlık seviyeleri, kod blokları)
- **Mermaid diyagramları**: `docs/report/diagrams/*.mmd` — Word'e PNG olarak export edilebilir
- **Ekran görüntüleri yer tutucusu**: Track A henüz bitmediyse `[ŞEKIL X.Y: <açıklama> — Playwright çıktısı bekliyor]` yaz, bitince yerleştirilir

## DOKUNMA (diğer track'ler çalışıyor)

- `src/main/webapp/WEB-INF/views/**` (Track A)
- `src/main/resources/messages_*.properties` (Track A)
- `src/main/java/**` (Track B)
- `src/main/resources/log4j.properties` (Track B)
- `pom.xml` (Track B)

## Sadece YAZ

- `docs/report/rapor.md` (ana taslak)
- `docs/report/diagrams/*.mmd` (mermaid kaynak)
- `docs/report/screenshots/.gitkeep` (sadece dizin)

## Tamamlandı Kriteri

- Tüm 12 alt madde `docs/report/rapor.md` içinde yazılı
- Kapak/Beyan/AI Beyan/Teşekkür/Özet/Abstract dolu
- Bileşen açıklama tablosu eksiksiz (tüm Controller/Service/DAO/Interceptor)
- Request URI tablosu eksiksiz (tüm endpoint'ler)
- Mermaid diyagramları `diagrams/` altında
- Kullanıcı Word'e kopyalayıp şablona dökülecek halde
- `memory/changelog.md`'a tek satır eklenmiş

## Notlar

- AI Beyan dürüst olsun ama detayda boğulma — "Claude Code refaktör + dokümantasyon desteği aldık, çıktıları gözden geçirip slayt referansına göre doğruladık" yeterli.
- Track A ve B paralel çalıştığı için, dosya değişikliklerini izlemek için periyodik `git log --oneline -20` çek
- Rapor son hâline kullanıcının `sablon.docx` dosyasıyla manuel birleştirme adımı kalır — bunu kullanıcıya bırak
