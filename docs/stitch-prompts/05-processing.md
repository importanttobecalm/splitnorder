# 05 — Processing (İşleme / Bekleme)

## Stitch Promptu

```
Design the "Processing" screen for Splitnorder — shown after a user uploads a song and the AI (Demucs) is separating it into stems. Use the Splitnorder design system (pale icy-blue #EAF2FB dotted-grid background, white surface cards with soft cloud-like shadows, Plus Jakarta Sans headings, Inter body, primary #4A90E2, stem accents vocals=red #E53935, drums=orange #FB8C00, bass=purple #5E35B1, other=teal #00897B). Desktop 1440×900.

────────────────────────
TOP NAV (same sticky white blurred bar with logo, nav, avatar)
────────────────────────

────────────────────────
CENTRAL FOCUS — single large card, max-width 760px, centered vertically (white, radius 24px, padding 56px, soft shadow)
────────────────────────

TOP OF CARD — track summary row:
- Left: 56px album-art placeholder thumbnail (gradient).
- Middle: filename "araba-sevdasi.mp3" 18px 600, caption "MP3 · 4:32 · 6.4 MB" 13px #6B7C93.
- Right: ghost button "İptal" with X icon.

────────────────────────
HEADLINE BLOCK (32px vertical gap from top row, centered text)
────────────────────────
- H2 "Stem'lere ayrılıyor..." — 32px Plus Jakarta Sans 700, #1E3A5F.
- Subhead — current rotating status line, 16px #6B7C93:
  "Vokali çıkarıyoruz..."  (the text rotates through phases as progress advances).

────────────────────────
PROGRESS VISUAL (the centerpiece)
────────────────────────
A circular SVG progress ring, 240×240px:
- Background ring: #E1E8F2, 12px stroke.
- Foreground ring: a gradient stroke going red→orange→purple→teal (the four stem colors blended around the circle), 12px stroke, animated draw-on.
- Inside the ring, large numeric "%" — e.g., "62%" — 56px JetBrains Mono 500, color #1E3A5F.
- Beneath number: caption "tahminî 48 sn kaldı" — 13px #6B7C93.

────────────────────────
STEM STATUS LIST (below ring, 4 rows, each 56px tall, max-width 480px centered)
────────────────────────
Each row:
- Left: stem icon in its accent color circular tint (28px circle), then stem label "Vokal" / "Davul" / "Bas" / "Diğer".
- Middle: a slim horizontal progress bar (height 6px, radius 999px), filled in stem accent color.
- Right: status icon — spinning loader (in-progress) / green check (done) / dot (queued).

Variants per row should illustrate different states:
  • Vocal — ✓ Done (bar full, green check)
  • Drums — in-progress (bar 70%, spinner)
  • Bass — queued (bar empty, dot)
  • Other — queued (bar empty, dot)

────────────────────────
TIPS / FUN-FACT FOOTER (small, below stem list, muted)
────────────────────────
A rotating "Did you know" pill — caption 13px #A4B0C0:
"💡 Demucs modeli her stem'i ayrı bir nöral ağ ile çıkarır."
Pill has a slow fade-cycle through tips.

────────────────────────
BACKGROUND AMBIENT
────────────────────────
Behind the central card, very faint floating waveform shapes in the four stem colors at ~6% opacity, drifting slowly. Should feel like a calm, working studio — never busy or distracting.

────────────────────────
INTERACTIONS / STATES
────────────────────────
- The whole page should hint at "live" — subtle pulse on the ring's leading edge, soft bobbing on the album-art thumbnail.
- "İptal" button hover: turns text and border to #E53935 (warning intent).
- When all stems done, show a transition variant: ring becomes solid green, big check icon appears, and a primary button "Studio'ya git →" slides in.

MOOD: patient, confident, alive. The user must feel something serious is happening but never anxious.
```
