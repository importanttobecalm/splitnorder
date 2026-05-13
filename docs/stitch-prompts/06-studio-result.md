# 06 — Studio / Result (ANA EKRAN — Stem Ayrım Sonucu)

> **Bu projenin imza ekranı.** Kullanıcının paylaştığı referans tasarım birebir bu ekran. Aşağıdaki prompt birebir o görselden türetildi.

## Stitch Promptu

```
Design the "Studio" result screen for Splitnorder — the signature screen of the entire app. After AI separation finishes, the user lands here to listen, control, and download each stem. Use the Splitnorder design system (pale icy-blue #EAF2FB dotted-grid background, white "cloud-like" pillowy cards with soft shadow 0 4px 24px rgba(30,50,90,0.06), Plus Jakarta Sans headings, Inter body, JetBrains Mono for numerics, primary #4A90E2, stem accents: vocals=#E53935, drums=#FB8C00, bass=#5E35B1, other=#00897B). Desktop 1920×1080 (cinematic wide).

────────────────────────
TOP BAR (72px tall, full width, transparent over the dotted grid)
────────────────────────
- Left: Splitnorder logo mark (40px) + wordmark "Splitnorder" 28px Plus Jakarta Sans 700, color #1E3A5F.
- Right (cluster):
  • Pill button "Download All (ZIP)" — white bg, 1px border #E1E8F2, 14px 600 #1E3A5F, download icon left, radius 999px, padding 12×20.
  • Circular icon button (44px) "Share" — white bg, 1px border #E1E8F2, share icon centered.

────────────────────────
CANVAS (the studio graph — fills the middle of the screen)
────────────────────────
This is a NODE-GRAPH layout. Five cards floating on the dotted grid, connected by colored bezier curves.

CENTER — "input node" card (the source song):
- White rounded card, ~520×140px, radius 16px, soft shadow.
- Inside: left = 96×96px square album-art thumbnail (gradient placeholder showing musical note), right = text "input node" 22px 700 #1E3A5F + a 24px tall horizontal waveform (compact, multicolored bars in muted blues/grays). Far right edge: circular play button (44px, light blue #DDEBF8 bg, blue #4A90E2 play triangle).

FOUR STEM CARDS — placed at the four corners around the input node, each one a "cloud-like" white card ~360×220px, radius 16px, soft shadow:

  TOP-LEFT — OTHER (teal):
    • Top: small chip — teal-tinted rounded square with music-note icon + label "OTHER" 14px 700 letter-spacing 0.04em, teal color.
    • Middle: waveform area ~80px tall — mirrored vertical bars in teal #00897B.
    • Bottom row (left → right): circular "S" (Solo) button + circular "M" (Mute) button + horizontal volume slider (teal track) + download icon.

  TOP-RIGHT — VOCALS (red):
    • Same structure, chip uses microphone icon + "VOCALS", red waveform, red slider, red download.

  BOTTOM-LEFT — BASS (purple):
    • Bass-clef icon, "BASS", purple waveform, purple slider.

  BOTTOM-RIGHT — DRUMS (orange):
    • Stylized drum (or playful cat-face) icon, "DRUMS", orange waveform, orange slider.

CONNECTION CURVES:
Four smooth bezier curves go from the input node to each stem card. Each curve uses its STEM ACCENT COLOR with a slight gradient (lighter near input, saturated near stem). Stroke width 4–6px, rounded caps. Curves arc gracefully — they cross visually but never overlap awkwardly. Slight glow effect under each curve (8px blur, same hue, 20% opacity).

────────────────────────
MASTER PLAYER BAR (sticky bottom, full width, 96px tall)
────────────────────────
Glass effect: backdrop-filter blur(20px), bg rgba(255,255,255,0.85), top border 1px #E1E8F2, soft top shadow.

Layout left → right:
1. Time readout: "01:28 / 03:45" — 18px JetBrains Mono 500, #1E3A5F.
2. Wide multi-stem stacked waveform (the heart of the bar) — four mini waveforms layered: drums orange, vocals red, bass purple, other teal — overlaid with subtle transparency, scrolling waveform of the full song. Roughly 50% of bar width.
3. Center: large circular play/pause button (64px, soft light-blue #DDEBF8 fill, blue pause icon).
4. Right cluster: label "Master volume" 14px #6B7C93 + speaker icon + slider track (160px wide, light blue #4A90E2 thumb).

────────────────────────
SUBTLE EXTRAS
────────────────────────
- Whole canvas has the faint dotted grid #D6E0EE at 16px spacing.
- Each stem card has a 2px left-edge accent line in its stem color (acts as identity bar).
- Solo (S) / Mute (M) buttons: 32px circles, transparent bg, 1px border #E1E8F2, accent text color when active.
- Volume sliders: track 4px tall, thumb 16px circle with soft shadow, accent color fill on left of thumb.
- Download icon: 20px line icon, accent color, button hover → fills accent tint.

────────────────────────
INTERACTIONS / STATES (show as variants beside the main frame)
────────────────────────
- Card hover: lifts -3px, shadow deepens.
- Playing state: that stem's waveform bars subtly pulse vertically; its accent line glows.
- Solo active: other three stems dim to 30% opacity; this card gets a glowing ring.
- Mute active: this card grayscales + small "M" badge highlights red.

────────────────────────
EMPTY / EDGE STATES
────────────────────────
- If a stem failed: replace its waveform with a small ⚠️ "Bu stem oluşturulamadı" message + "Yeniden dene" mini button.

MOOD: cinematic, calm, premium. Should feel like a peek into a professional DAW but with playful musical warmth. The signature image of the entire product — use this screen as the brand's hero.
```

---

## Referans

Kullanıcının yapıştırdığı görsel birebir bu tasarım — Stitch çıktısını ona göre kıyasla.
