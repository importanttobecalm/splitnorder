# 03 — Home / Landing (Ana Sayfa)

## Stitch Promptu

```
Design the "Home" landing screen for the Splitnorder web app — the page a logged-in user sees right after login, OR a public landing for first-time visitors. Use the Splitnorder design system (pale icy-blue #EAF2FB dotted-grid background, white cards with soft cloud shadows, Plus Jakarta Sans headings, Inter body, primary #4A90E2, stem accents vocals=#E53935, drums=#FB8C00, bass=#5E35B1, other=#00897B). Desktop 1440px wide.

────────────────────────
TOP NAVIGATION (sticky, 72px tall, white with backdrop blur)
────────────────────────
- Left: Splitnorder logo mark (32px) + wordmark "Splitnorder" 20px 700.
- Center: nav links — "Ana Sayfa" (active, weight 600), "Geçmiş", "Hakkında", "İletişim".
- Right: language switcher "TR | EN" pill + user avatar (28px circle with initials) + dropdown caret.

────────────────────────
HERO SECTION (80px top padding, centered text, max 880px)
────────────────────────
- Eyebrow chip: pill "🎵 BM470 Proje · Demucs destekli" — small, white bg, 1px border #E1E8F2, color #6B7C93.
- H1 (Display 56px, Plus Jakarta Sans 700, #1E3A5F, tight tracking): "Müziğini katmanlarına ayır."
- Subhead (20px, Inter 400, #6B7C93, max-width 640px): "Bir şarkı yükle — vokali, davulu, bası ve diğer enstrümanları birkaç saniye içinde stem'lere ayıralım. Stüdyo kalitesinde, tarayıcında."
- 32px gap, button row:
  • Primary button "Şarkı Yükle" — bg #4A90E2, white text 16px 600, 14×28px padding, upload icon left.
  • Ghost button "Demo'yu Dinle" — transparent bg, 1px border, play icon left.
- 64px gap, beneath buttons: a HERO VISUAL — a stylized "input node" white card in the center with four colored bezier curves arcing out to four floating stem cards (vocals red top-right, other teal top-left, bass purple bottom-left, drums orange bottom-right). Each stem card mini-version (180×100px) with its waveform. The connection lines must be smooth bezier curves in the stem accent colors. This is the visual signature of the app — make it feel alive but quiet (no heavy animation).

────────────────────────
FEATURES STRIP (white surface row, 3 cards, 32px gap)
────────────────────────
Each feature card (radius 16px, padding 28px, soft shadow):
  1. Icon (40px, primary #4A90E2 in soft tint circle) — "Hızlı Ayrıştırma" / "Demucs modelimiz dakikalar içinde yüksek kalite stem üretir."
  2. Icon — "Stem Bazlı Kontrol" / "Her stem için bağımsız oynat, sustur, ses ayarla, indir."
  3. Icon — "Geçmişin Saklı" / "Yüklediğin her şarkı hesabında, istediğin zaman tekrar dinle."

────────────────────────
"HOW IT WORKS" — 3 STEP ROW
────────────────────────
Section header: "Nasıl çalışır?"  — H2 32px, centered.
Three big numbered cards side by side (1·2·3), each with a small illustration:
  • 01 — "Şarkını yükle (MP3 / WAV)"
  • 02 — "AI saniyeler içinde ayırır"
  • 03 — "Studio'da dinle ve indir"
A thin animated dotted line connects 1→2→3.

────────────────────────
RECENT PROJECTS (only for logged-in users — show as a section)
────────────────────────
H2 "Son Projelerin" + right link "Tümünü gör →"
Horizontal row of 4 mini project cards (radius 12px, 240×140px):
  • Album thumbnail (gradient placeholder), song title, artist, timestamp "2 saat önce", small badge "✓ Tamamlandı".
  • If user has none: centered empty state — muted music-staff illustration + "Henüz proje yok. İlk şarkını yükle!" + primary button.

────────────────────────
FOOTER (slim, 80px)
────────────────────────
- Left: Splitnorder mark + "© 2026 Splitnorder — BM470 Ders Projesi · Düzce Üniversitesi"
- Right: links — "Hakkında · Gizlilik · GitHub"
- Background: white, top border #E1E8F2.

MOOD: confident, calm, studio-modern. Lots of breathing room. The hero visual is the star.
```
