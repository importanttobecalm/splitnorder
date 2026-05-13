# 04 — Upload (Dosya Yükleme)

## Stitch Promptu

```
Design the "Upload" screen for Splitnorder where users drop or pick a song file to be split into stems. Use the Splitnorder design system (pale icy-blue #EAF2FB dotted-grid background, white cards with cloud-like soft shadows, Plus Jakarta Sans headings, Inter body, primary #4A90E2, stem accents vocals=red, drums=orange, bass=purple, other=teal). Desktop 1440px.

────────────────────────
TOP NAV (same as Home — sticky, white blur, logo + nav links + avatar). "Ana Sayfa" inactive, no active item here (or "Yükle" active if you add it).
────────────────────────

────────────────────────
PAGE HEADER (64px top padding)
────────────────────────
- H1 "Şarkını yükle" — 40px Plus Jakarta Sans 700, #1E3A5F.
- Subhead "MP3, WAV veya FLAC. Maksimum 50 MB, 10 dakikaya kadar." — 16px #6B7C93.

────────────────────────
MAIN DROPZONE (large central card, max-width 920px, centered)
────────────────────────
- White surface, radius 24px, padding 56px, soft cloud shadow, **dashed 2px #4A90E2 inner border at 50% opacity** to indicate drop area.
- Inner content centered:
  1. Large icon (88px, primary #4A90E2 inside a 140px soft circular tint #E8F1FC): cloud-upload icon.
  2. H2 "Dosyanı buraya sürükle" — 28px 700.
  3. Caption "veya" — 14px #6B7C93.
  4. Primary button "Dosya Seç" — bg #4A90E2, white text 600, 48×24 padding, 12px radius, folder icon left.
  5. 16px gap, fine print "Dosyaların 30 gün boyunca saklanır." 13px #A4B0C0.

ACTIVE DRAG STATE (show as a second variant beneath, labeled "Drag active"):
- Border becomes solid 2px #4A90E2.
- Background tints to #F0F7FF.
- Icon scales up subtly and a soft pulsing ring radiates from it.

────────────────────────
ADVANCED OPTIONS (collapsed by default — show expanded variant)
────────────────────────
A subtle row below the dropzone:
- "Gelişmiş ayarlar" toggle (chevron). When expanded, reveals an inline panel:
  • Radio group "Model kalitesi": [Hızlı (htdemucs)] [Standart (htdemucs_ft)] [En Yüksek (mdx_extra)] — pill-style radios with stem accent border on selected.
  • Checkbox row: ☑ "Vokal" ☑ "Davul" ☑ "Bas" ☑ "Diğer" — each colored with its stem accent dot. User can deselect to skip a stem.
  • Slider "Çıktı bit-rate": 128 / 192 / 320 kbps.

────────────────────────
ALTERNATIVE INPUT (small ghost row beneath dropzone)
────────────────────────
- "Veya bir URL yapıştır" — input field 60% width + primary button "Getir". (YouTube / SoundCloud destek bilgisi small caption.)

────────────────────────
FILE-SELECTED VARIANT (second screen state, show as a third frame)
────────────────────────
Same page, but the dropzone is replaced by a "selected file" card:
- Left: 64px album-art placeholder (gradient).
- Middle: filename "araba-sevdasi.mp3", caption "MP3 · 4:32 · 6.4 MB", a tiny waveform preview 24px tall #4A90E2.
- Right: "Kaldır" ghost button + primary "Ayırmaya Başla" button (large, 56px, gradient bg red→orange #E53935→#FB8C00 to hint at the stems).

────────────────────────
TIPS SECTION (below main card)
────────────────────────
Three small inline tip pills in a row:
- 💡 "En iyi sonuç için stereo, sıkıştırılmamış kaynak kullan."
- 🎚️ "Vokali öne çıkmış miks daha temiz ayrılır."
- ⏱️ "İşlem süresi: ortalama şarkı süresinin %30'u."

MOOD: invitational, frictionless. Dropzone is the unmistakable focal point. Everything else is a quiet sidekick.
```
