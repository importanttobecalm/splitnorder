# 00 — Design System (Foundation)

> **İLK BU PROMPTU VER.** Stitch tüm sonraki ekranları bu sisteme göre üretecek.

---

## Stitch Promptu

```
Create the foundational design system for a web application called "Splitnorder" — an AI music stem separator (vocals / drums / bass / other) built as a university course project. Future screens will reuse these tokens; do not deviate.

────────────────────────
BRAND IDENTITY
────────────────────────
- Product name: Splitnorder
- Tagline: "Müziğini katmanlarına ayır" (TR — "Split your music into layers")
- Logo: stylized treble clef fused with an eighth-note, painted in a warm gradient (red #E53935 → orange #FB8C00 → amber #FFC107). Playful, hand-drawn musical mark on a transparent background.
- Personality: studio-grade but student-friendly; calm, focused, slightly playful. Think "Figma meets Ableton, but lighter."

────────────────────────
COLOR TOKENS
────────────────────────
Background base:   #EAF2FB   (very pale icy blue — NOT pure white)
Surface / card:    #FFFFFF   shadow: 0 4px 24px rgba(30,50,90,0.06)
Surface elevated:  #FFFFFF   shadow: 0 8px 32px rgba(30,50,90,0.10)
Grid pattern:      #D6E0EE dots, 16px spacing, very faint

Primary (action):  #4A90E2   hover #3A7BC8
Primary deep:      #1E3A5F   (headings, body emphasis)
Text primary:      #1E3A5F
Text secondary:    #6B7C93
Text muted:        #A4B0C0
Divider:           #E1E8F2

STEM ACCENT COLORS — ALWAYS map this way, never swap:
  VOCALS → #E53935  (warm red)
  DRUMS  → #FB8C00  (orange)
  BASS   → #5E35B1  (deep purple)
  OTHER  → #00897B  (teal)

State:
  Success #43A047 · Warning #FB8C00 · Error #E53935 · Info #4A90E2

────────────────────────
TYPOGRAPHY
────────────────────────
- Headings: "Plus Jakarta Sans" 700, tight tracking (-0.02em)
- Body: "Inter" 400/500
- Numeric (timers, waveform labels, durations): "JetBrains Mono" 500, tabular-nums

Scale:
  Display 48/56 · H1 36/44 · H2 28/36 · H3 22/30 · Body 16/24 · Small 14/20 · Caption 12/16

────────────────────────
SHAPE & ELEVATION
────────────────────────
- Radius: card 16px · button 12px · pill 999px · input 10px · sheet 24px
- Cards: "cloud-like" pillowy feel — subtle 1px inner highlight on top edge (rgba 255,255,255,0.7) plus the soft drop shadow.
- Master player bar: glass effect, backdrop-filter blur(20px), bg rgba(255,255,255,0.75).
- Buttons: hover lifts -2px, shadow deepens; active scales 0.98.

────────────────────────
ICONOGRAPHY
────────────────────────
- Lucide / Phosphor line icons, 1.5px stroke, 20px default.
- Stem icons (per accent color):
    vocals → microphone
    drums  → drum / cat-face stylized
    bass   → bass-clef
    other  → music-note

────────────────────────
COMPONENTS
────────────────────────
1. Button (primary): bg #4A90E2, text white, 12px radius, 14px/24px padding, weight 600.
2. Button (ghost):   transparent, 1px border #E1E8F2, text #1E3A5F.
3. Button (stem):    colored ring matching stem accent, white fill, accent text.
4. Input field: 1px #E1E8F2 border, focus ring #4A90E2 2px offset, 12px padding.
5. Stem card: white surface, top-left rounded chip with stem icon + label, waveform area (50px tall), bottom row [S | M | volume-slider | download-icon]. Per-stem accent color on chip, waveform, slider track, and a 2px left border.
6. Waveform visual: rounded vertical bars, mirrored top/bottom, accent-colored.
7. Master player bar (sticky bottom, full width): timer left, central play/pause circular button, mini multi-stem stacked waveform, master-volume slider right.
8. Navigation: minimal top bar — logo left, "Splitnorder" wordmark, right side: nav links / user avatar.
9. Toast: pill shape, soft shadow, accent dot on left.
10. Empty state: centered illustration of a paused musical staff, muted text, primary CTA below.

────────────────────────
MOTION
────────────────────────
- Default ease: cubic-bezier(0.22, 0.61, 0.36, 1)
- Card enter: fade + 8px translate-up, 240ms
- Stem connection lines (on result screen): SVG bezier curves animating draw-on at 600ms
- Waveform play state: subtle scale-y pulsing on bars (1.0 → 1.05) on the currently-playing stem only

────────────────────────
LAYOUT GRID
────────────────────────
- 12-column, 1280px max content width
- Page side gutter: 32px desktop / 16px mobile
- Section vertical rhythm: 80px desktop / 48px mobile

────────────────────────
ACCESSIBILITY
────────────────────────
- Min contrast 4.5:1 for text on backgrounds
- Focus visible: 2px solid #4A90E2 outline + 2px offset
- All stem colors paired with icon + label (never color-only signaling)

Output: a single style guide screen showing the palette swatches, type scale, components grid (button variants, input, stem card example for VOCALS, master player bar), and the logo mark. Use the dotted-grid pale-blue background.
```

---

## Notlar (kendin için)

- Stitch bazen tek seferde tüm system'i yakalayamaz — gerekirse "yalnızca renk/font tablosu" ve "yalnızca bileşen örnekleri" diye iki parçaya böl.
- Çıktıyı ekran kartı olarak değil **style-guide referansı** olarak sakla; ekranları üretirken aynı session'da kalmaya dikkat et.
