# 08 — Profile & Settings (Profil / Ayarlar)

## Stitch Promptu

```
Design the "Profile & Settings" page for Splitnorder. Use the Splitnorder design system (pale icy-blue #EAF2FB dotted-grid background, white cloud-like cards with soft shadow, Plus Jakarta Sans headings, Inter body, primary #4A90E2, stem accents vocals=red, drums=orange, bass=purple, other=teal). Desktop 1440px.

────────────────────────
TOP NAV (same sticky bar; user avatar on the right is active/highlighted)
────────────────────────

────────────────────────
LAYOUT — TWO COLUMN
────────────────────────

LEFT SIDEBAR (260px wide, white card, radius 16px, soft shadow, padding 20px, sticky at top:120px):
A vertical nav list of sections. Each row 48px tall, 12px padding, 10px radius, icon + label, active item has soft #DDEBF8 background and #4A90E2 text + 3px left accent bar.
Items:
  • 👤 Profil bilgileri (active)
  • 🔐 Güvenlik
  • 🎚️ Ayrıştırma tercihleri
  • 🌐 Dil & Bölge
  • 🔔 Bildirimler
  • 💾 Veri & Depolama
  • 🗑️ Hesabı sil  (red text, separated by a divider above)

RIGHT CONTENT AREA (max-width 760px, vertical sections, 32px gap between):

SECTION 1 — Profil bilgileri (white card, radius 16px, padding 32px):
- H2 "Profil bilgileri" 24px 700.
- Avatar row: 96×96 circle avatar with user initials gradient + ghost button "Fotoğraf değiştir" + small muted "JPG/PNG, max 2MB".
- 24px gap.
- Two-column input grid:
  • Ad: "Yusuf"  ·  Soyad: "Bulut"
  • Kullanıcı adı: "yusufb"  ·  E-posta: "yusufbulutm@gmail.com" (with tiny green "✓ Doğrulandı" chip).
- Textarea "Bio" — 3 lines, placeholder "Kendinden bahset…", char counter bottom-right.
- Footer row right-aligned: ghost "İptal" + primary "Kaydet".

SECTION 2 — Güvenlik (card):
- H2 "Güvenlik".
- Row: "Parola" with caption "Son değiştirme: 14 gün önce" → right side ghost button "Parolayı değiştir".
- Row: "İki adımlı doğrulama" — switch toggle (off by default, primary blue when on) + caption.
- Row: "Aktif oturumlar" — link "Oturumları gör (3)".

SECTION 3 — Ayrıştırma tercihleri (card) — STEM-CENTRIC, the personality of this app:
- H2 "Ayrıştırma tercihleri".
- "Varsayılan model" — pill radio row: [Hızlı] [Standart (seçili)] [En Yüksek].
- "Otomatik üretilecek stem'ler" — four big toggle cards in a 4-col grid, each in stem accent color:
    • Vocals (red ring, mic icon)   — toggle on
    • Drums (orange ring, drum icon) — toggle on
    • Bass (purple ring, bass-clef) — toggle on
    • Other (teal ring, music-note) — toggle on
  Each card 160×120px white surface with stem accent 2px left border, icon top, label, switch bottom-right.
- "Varsayılan çıktı formatı": radio MP3 / WAV / FLAC.

SECTION 4 — Dil & Bölge (card):
- "Arayüz dili": dropdown "Türkçe ▾" (options: Türkçe / English). Tiny TR/EN flag chip beside.
- "Saat dilimi": dropdown "Europe/Istanbul (UTC+3) ▾".
- "Tarih biçimi": radio "DD.MM.YYYY" / "MM/DD/YYYY".

SECTION 5 — Bildirimler (card):
- Toggle list:
  • "Ayrıştırma tamamlandığında e-posta gönder"  (on)
  • "Aylık özet"  (off)
  • "Ürün güncellemeleri"  (off)

SECTION 6 — Veri & Depolama (card):
- A horizontal usage bar — gradient red→orange→purple→teal — labeled "1.2 GB / 5 GB kullanıldı".
- Beneath: legend with four color dots and per-stem storage (Vocals 320MB, Drums 280MB, Bass 220MB, Other 380MB).
- Buttons: "Verilerimi dışa aktar" ghost · "30 günden eskileri temizle" warning-tone ghost.

SECTION 7 — Tehlikeli bölge (card with subtle red border #FECACA, padding 24px):
- H3 "Hesabı sil" red #E53935.
- Caption "Bu işlem geri alınamaz. Tüm projelerin ve stem'lerin silinir."
- Ghost button red "Hesabımı sil" (right-aligned).

────────────────────────
INTERACTIONS / FEEDBACK
────────────────────────
- Sticky "Kaydedilmemiş değişikliklerin var" toast appears bottom-center when any field is dirty, with "Kaydet" button.
- Saving state: primary button shows spinner + "Kaydediliyor…", then a green pill toast "✓ Profil güncellendi".

MOOD: clean, controlled, transparent. Should feel like a settings screen built by someone who respects the user's time. The stems section sneaks in personality so it doesn't feel generic.
```
