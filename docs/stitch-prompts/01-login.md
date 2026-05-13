# 01 — Login (Giriş)

## Stitch Promptu

```
Design a "Login" screen for the Splitnorder web app using the existing Splitnorder design system (pale icy-blue #EAF2FB dotted-grid background, white cards with soft cloud-like shadows, Plus Jakarta Sans headings, Inter body, primary action color #4A90E2, stem accent palette for decoration only). Desktop-first, 1440×900.

LAYOUT — two-column split:

LEFT COLUMN (60% width, full height) — BRAND PANEL:
- Background: same pale-blue #EAF2FB with faint dotted grid.
- Centered vertically:
  • Splitnorder logo mark (treble clef + eighth-note, red→orange→amber gradient), 96×96px.
  • Wordmark "Splitnorder" 48px Plus Jakarta Sans 700, color #1E3A5F.
  • Tagline beneath: "Müziğini katmanlarına ayır." 18px Inter 400, color #6B7C93.
- Decorative element behind the logo: four softly-blurred floating cards (one per stem) at low opacity (~12%), each showing a tiny waveform in its stem color (vocals red, drums orange, bass purple, other teal). They drift in different positions, creating a calm "music studio" ambient feel — they must NOT compete with the form.

RIGHT COLUMN (40% width, full height) — FORM PANEL:
- White surface card centered vertically, max-width 400px, padding 40px, radius 16px, soft shadow.
- Inside the card, top to bottom:
  1. H2 heading "Tekrar hoş geldin" — 28px, weight 700, color #1E3A5F.
  2. Subtitle "Hesabına giriş yap" — 15px, color #6B7C93, 8px below heading.
  3. 32px gap.
  4. Input field — Label "E-posta" above, placeholder "ornek@mail.com", left icon (mail), full width, 10px radius.
  5. 20px gap.
  6. Input field — Label "Parola" above, placeholder "••••••••", left icon (lock), right icon (eye, toggle visibility), full width.
  7. 12px gap.
  8. Row: left = checkbox "Beni hatırla" (14px), right = link "Şifremi unuttum" (14px, color #4A90E2).
  9. 28px gap.
  10. Primary button "Giriş Yap" — full width, 48px tall, bg #4A90E2, white text 16px 600, 12px radius.
  11. 24px gap.
  12. Divider with centered "veya" label (line color #E1E8F2).
  13. 20px gap.
  14. Ghost button "Google ile devam et" — full width, 48px, white bg, 1px #E1E8F2 border, Google "G" icon left of label.
  15. 28px gap.
  16. Footer line centered: "Hesabın yok mu? " + link "Kayıt ol" (color #4A90E2, weight 600).

INTERACTIONS / STATES:
- Input focus: 2px #4A90E2 outline ring with 2px offset.
- Button hover: lifts -2px, shadow deepens.
- Show error chip beneath an input on invalid (red #E53935 dot + "Geçersiz e-posta" caption).

ACCESSIBILITY:
- All labels visible (no placeholder-only).
- Tab order: email → password → remember → forgot → submit → google → register link.

MOOD: calm, focused, studio-like. The form must feel light, the brand panel must feel like the inside of a peaceful music workspace.
```
