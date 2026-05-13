---
name: Grok web UI otomasyon tarifi
description: grok.com'a mesaj atıp cevap almak için Playwright MCP adımları (test edildi 2026-05-12)
type: reference
---

**Ön koşul:** Playwright MCP gerçek Chrome profili ile çalışıyor (`reference_playwright_chrome.md`), grok.com'da Google login açık. Chrome manuel kapalı olmalı.

## Adım adım akış

1. **Navigate:** `browser_navigate("https://grok.com")`
2. **Login kontrolü (opsiyonel):** Snapshot al, "Oturum aç" / "Üye ol" linkleri varsa login DEĞİL. Yoksa kullanıcı adı görünür (örn. "Yusuf BULUT m.yusufzehraa@gmail.com").
3. **Mesajı yaz:**
   - Selector: `getByTestId('chat-input')` içindeki `paragraph` rolü (rich text editor — Lexical/ProseMirror tabanlı)
   - `browser_type(target=<paragraph_ref>, text="...", submit=false)` — submit=true güvenilir DEĞİL, Enter bazen göndermiyor
4. **Gönder butonuna tıkla:**
   - Selector: `getByTestId('chat-submit')` (input dolunca görünür, boşken yok)
   - `browser_click(target=<submit_ref>)`
5. **Cevabı bekle:**
   - URL `/` → `/c/{uuid}?rid=...` olarak değişir (gönderim onayı)
   - Sayfa title cevabın özetine dönüşür (örn. "Test Message Successful - Grok")
   - `browser_wait_for(time=3-5)` genelde yeterli; daha uzun cevaplar için 8-10sn
6. **Cevabı oku:** Snapshot al. Cevap, kullanıcı paragrafından SONRA gelen `paragraph` elementlerinde. "X saniye düşündü" butonu cevabın hemen üstündedir — bu işareti ankraj olarak kullan.

## Cevap konumu (snapshot tree örneği)

```
main → generic → generic →
  generic [user message]:
    paragraph: "Test mesajı, beni duyuyor musun?"
  generic [grok response]:
    button "X saniye düşündü"
    generic:
      paragraph: "Evet, seni duyuyorum! 👋"     ← cevap satırı 1
      paragraph: "Test mesajı başarılı..."        ← cevap satırı 2
```

## Tuzaklar

- **Enter ile submit güvenilir değil** — her zaman Gönder butonuna tıkla.
- **Anonim mod artık çalışmıyor** — login zorunlu. Login yoksa anonim sayfa görünüyor ama submit'te Google OAuth'a redirect ediyor.
- **Model seçimi:** Default "Hızlı/Fast" — yeterli. SuperGrok upsell banner'ı `e232` civarı çıkıyor, görmezden gel.
- **Conversation persistence:** Her gönderim yeni bir `/c/{uuid}` yaratıyor, sol sidebar "Geçmiş"e ekleniyor. Aynı sohbette devam etmek için URL'ye geri gelmek gerek.

## Hızlı tek-satırlık şablon (psödo-kod)

```
navigate(grok.com) → snapshot → type(chat-input, msg) → click(chat-submit)
→ wait(4s) → snapshot → grep paragraph after "X saniye düşündü"
```
