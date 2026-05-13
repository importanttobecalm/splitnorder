---
name: Playwright MCP — gerçek Chrome profili ile çalışıyor
description: Playwright MCP, Yusuf'un gerçek Chrome profiline bağlı; Grok/Gmail/Twitter vb. login-gerekli sitelerde otomatik oturum açık
type: reference
originSessionId: 5935a5d9-0982-438e-9e74-9853bd7ebf28
---
Playwright MCP (`@playwright/mcp@latest`) `~/.claude.json` içinde şu argümanlarla yapılandırıldı:

```
--browser=chrome
--user-data-dir=/Users/yusufbulut/Library/Application Support/Google/Chrome
```

**Sonuç:**
- Yusuf'un gerçek Chrome profili (cookie + login session) kullanılıyor
- grok.com, gmail.com, twitter.com vb. tüm login-gerekli sitelere otomatik oturum açık geliyor
- Site-specific kurulum yok — `browser_navigate` herhangi bir URL'e gider

**Şart:** Chrome manuel açıkken Playwright çalıştırılamaz (profile lock). Kullanmadan önce Cmd+Q ile kapat.

**Test edildi (2026-05-12):** grok.com'a navigate → "Yusuf BULUT" hesabı zaten login → mesaj gönderildi → cevap alındı. Pipeline çalışıyor.

**Güvenlik notu:** Bu config kalıcı — tüm profil verisi (kayıtlı şifreler, history, tüm site cookieleri) Playwright otomasyonu için erişilebilir. Sadece güvendiğin işler için kullan.
