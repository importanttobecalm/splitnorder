---
name: Tasarımı / kod yapısını ASLA kafana göre sadeleştirme
description: Kullanıcının verdiği zengin tasarım veya mevcut kod yapısını "MVP" / "ilk turda basit tutalım" diye kırpmak yasak — tasarım/yapı olduğu gibi korunur, eksik olan davranışlar arka tarafta tamamlanır
type: feedback
---

Kullanıcı bir tasarım veya kod yapısı verdiğinde, onu **olduğu gibi koru** — "MVP'de basitleştirelim, ileride genişletiriz" demek YASAK.

**Somut olay (2026-05-13):** Studio'ya gerçek audio bağlanırken, app.jsx'in alt master bar tasarımını (renkli stacked stem waveform + master play + per-stem kontroller + ZIP) gizledim ve yerine kendi yazdığım sadeleştirilmiş `audeng-bar` koydum. Kullanıcı "kompleks tasarımı niye kırptın?!" diye haklı olarak kızdı. Düzeltme: orijinal alt bar'ı geri açıp app.jsx'e patch'le gerçek state'i bağlamak.

**Why:** Kullanıcı tasarım/kod tercihini somut göstermişse, o seçimi *senin yorumladığın* basitleştirme yetkisi senin değildir. "Bu Faz 4'te yaparım, şimdi basit tutayım" düşüncesi → tasarımı bozarak deploy → kullanıcı için *gerileme* hissi.

**How to apply:**
- Bir tasarımı entegre ederken görsel hiçbir şeyi gizleme / yeniden çizme — sadece arka tarafta davranış bağla
- Mock state'i değiştirmek gerekirse, prop-driven controlled mode + mock fallback ile yap, **tasarımın DOM'una dokunma**
- "İlk turda basit, sonra genişletirim" → bu uyarı: planın yanlış, gerçek planı yap
- `display: none` ile orijinal görsel öğeleri öldürmek özellikle riskli — yapacaksan önce kullanıcıya sor
- Refactoring yapılırken de aynı: çalışan UI/davranışı "temizleme" adına kırpma
