# 02 — Register (Kayıt)

## Stitch Promptu

```
Design a "Register" screen for Splitnorder using the existing Splitnorder design system (pale icy-blue #EAF2FB dotted-grid background, white cards with cloud-like soft shadows, Plus Jakarta Sans headings, Inter body, primary #4A90E2, stem accents red/orange/purple/teal). Desktop 1440×900, must visually pair with the Login screen.

LAYOUT — same two-column split as Login (brand left 60% / form right 40%), to feel like a continuation of the same flow.

LEFT BRAND PANEL — identical to Login screen (logo + wordmark + tagline + ambient floating stem cards).

RIGHT FORM PANEL (white card, max-width 440px, padding 40px, radius 16px, soft shadow):

  1. H2 "Hesap oluştur" — 28px Plus Jakarta Sans 700, #1E3A5F.
  2. Subtitle "Birkaç saniye sürer, ücretsiz." — 15px Inter, #6B7C93.
  3. 28px gap.
  4. Input — Label "Ad Soyad", placeholder "Yusuf Bulut", left icon (user).
  5. 16px gap.
  6. Input — Label "Kullanıcı Adı", placeholder "yusufb", left icon (at-sign). Tiny availability hint (right of label): green check + "Uygun" or red dot + "Alınmış".
  7. 16px gap.
  8. Input — Label "E-posta", placeholder "ornek@mail.com", left icon (mail).
  9. 16px gap.
  10. Input — Label "Parola", placeholder "En az 8 karakter", left icon (lock), right eye toggle.
      Beneath input: a 4-segment password strength meter (segments fill in order — gray → red → orange → blue → green). Caption "Güçlü" or "Zayıf" right-aligned.
  11. 16px gap.
  12. Input — Label "Parola (Tekrar)", placeholder "Parolayı tekrar gir", left icon (lock).
  13. 20px gap.
  14. Checkbox row — "Kullanım koşulları" ve "Gizlilik Politikası"nı kabul ediyorum. (links in #4A90E2).
  15. 24px gap.
  16. Primary button "Hesabı Oluştur" — full width 48px, bg #4A90E2, weight 600.
  17. 20px gap.
  18. Footer centered: "Zaten hesabın var mı? " + link "Giriş yap".

VALIDATION VISUALS:
- Inline check icon (green #43A047) appearing on the right of valid inputs.
- Inline red dot + small caption beneath invalid inputs.

MICRO-INTERACTIONS:
- Password strength meter animates segment-by-segment as the user types.
- "Hesabı Oluştur" button is disabled (50% opacity) until all required fields valid and checkbox ticked.

MOOD: welcoming, low-friction. Form should feel scannable, not bureaucratic.
```
