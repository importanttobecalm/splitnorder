# 07 — History (Geçmiş İşlemler)

## Stitch Promptu

```
Design the "History" page for Splitnorder where the user reviews all songs they've previously separated into stems. Use the Splitnorder design system (pale icy-blue #EAF2FB dotted-grid background, white cloud-like cards with soft shadow, Plus Jakarta Sans headings, Inter body, primary #4A90E2, stem accents vocals=red, drums=orange, bass=purple, other=teal). Desktop 1440px.

────────────────────────
TOP NAV (same sticky white blurred bar, "Geçmiş" nav link active)
────────────────────────

────────────────────────
PAGE HEADER (64px top padding, max-width 1280px)
────────────────────────
- Left: H1 "Geçmiş Projelerin" 40px 700 #1E3A5F + caption "12 proje · 47 dakika ayrıştırılmış ses" 14px #6B7C93.
- Right: button cluster:
  • Search input (320px wide, search icon left, placeholder "Şarkı veya sanatçı ara…")
  • Filter dropdown pill "Tüm tarihler ▾"
  • Sort dropdown pill "En yeni ▾"
  • Primary button "+ Yeni Yükleme"

────────────────────────
VIEW TOGGLE (right under header)
────────────────────────
Small pill segmented control: [Grid] [List]. Default Grid active.

────────────────────────
GRID VIEW (default)
────────────────────────
Grid of project cards, 4 columns × responsive rows, 24px gap. Each card (white, radius 16px, soft shadow, 280×320px):

- Top: 160px album-art block (gradient placeholder, slight rounded top corners only). Overlayed on bottom-right of art: a small dark pill "4:32" duration.
- Body padding 16px:
  • Song title 16px 600 #1E3A5F, single line ellipsis. e.g., "Araba Sevdası".
  • Subtitle 13px #6B7C93 "araba-sevdasi.mp3 · MP3".
  • Tiny row of 4 colored dots representing available stems (red·orange·purple·teal). If a stem is missing, dot is gray.
  • 12px gap.
  • Footer row: timestamp 12px #A4B0C0 "2 saat önce" + right-aligned icon button cluster: play (▶) + download (↓) + more (⋯).
- Status ribbon on top-left of art: small pill with color:
  • Green "✓ Tamamlandı"  ·  Orange "⏳ İşleniyor"  ·  Red "✕ Başarısız"
- Hover: card lifts -3px, shadow deepens, a faint overlay "Aç" appears centered with arrow icon.

────────────────────────
LIST VIEW (variant — show as a second frame)
────────────────────────
Table-like rows, each 72px tall, white card row with soft separators:
Columns (left → right): 48px thumb · Title + subtitle · Duration · Stems available (4 colored dots) · Status pill · Created date · Action icons (play / download / delete / more).

────────────────────────
EMPTY STATE (third variant — for users with no projects yet)
────────────────────────
Centered illustration: a quiet musical staff with a paused note + four small ghost stem cards faded around it.
- H2 "Henüz proje yok"
- Caption "İlk şarkını yükle ve stem'lerine ayır."
- Primary button "Şarkı Yükle" (with upload icon).

────────────────────────
FILTERS PANEL (when "Filter" pill is opened — show as a popover variant)
────────────────────────
A small floating white sheet 320px wide, padding 20px, radius 16px, soft shadow.
- "Tarih Aralığı" — two date inputs.
- "Durum" — checkbox list: Tamamlandı / İşleniyor / Başarısız.
- "Dosya türü" — chip selector: MP3 / WAV / FLAC.
- Bottom row: ghost "Sıfırla" + primary "Uygula".

────────────────────────
PAGINATION (bottom, centered)
────────────────────────
Pill-shaped pager: ← 1 [2] 3 4 5 → · "Sayfa başına 24 ▾".

────────────────────────
BULK SELECT MODE (fourth variant)
────────────────────────
When user hovers a card, a checkbox appears top-left. Selecting one enters bulk mode:
- A floating bottom action bar appears (glass effect, centered, max-width 600px): "3 proje seçili" + buttons "İndir (ZIP)", "Sil" (red ghost), "İptal".

MOOD: organized, glanceable, library-like. The user must feel ownership of their work.
```
