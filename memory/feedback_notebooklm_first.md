---
name: NotebookLM-First Geliştirme Kuralı
description: Her kod/geliştirme önerisinden ÖNCE NotebookLM MCP (bm470) ile ders notlarını sorgula
type: feedback
updated: 2026-05-01
---

## Kural
Bu projede herhangi bir kod yazmadan veya teknik öneri sunmadan ÖNCE, ilgili konu için NotebookLM MCP (`bm470` alias, Notebook ID `0e624500-c0de-4aa2-b2f4-a3b290c04257`) sorgulanacak.

### Akış
1. **Sorgula:** İlgili konuyu NotebookLM'den ara (ör. "DAO pattern", "Criteria Query API", "Interceptor yapısı", "i18n setup").
2. **Varsa:** Ders notlarında geçtiği şekliyle birebir kullan. Modern best-practice ile çakışırsa ders kazanır.
3. **Yoksa:** Ya o yapıyı kullanma, ya da kullanıcıya sor — "bu konu ders notlarında yok, nasıl ilerleyeyim?"
4. **Asla:** Ders notlarını sorgulamadan "kendi bildiğim şekilde" kod yazma.

## Why
Bu BM470 (Düzce Üniv. İleri Java) ödev projesi. Hocanın değerlendirmesi ders bağlayıcı kuralları üzerinden. Modern/popüler yaklaşım (Spring Boot, JpaRepository, constructor injection vb.) ders teslim kriterlerini bozar → puan kaybı. Kullanıcının amacı: ders notlarındaki yapı ve prensipleri birebir uygulamak.

## How to apply
- Yeni feature, refactor, bug fix, mimari karar — hepsinde geçerli.
- Tek satırlık typo / rename gibi trivial değişikliklerde sorgu gerekmez.
- Sorgu sonucunu kullanıcıya kısaca özetle ki ne bulduğumu görsün.
- Sorgu yapılamıyorsa (MCP down) → kullanıcıya bildir, varsayım yapma.
- `INTEGRATION_GUIDELINES.md` ve `courses/` dosyaları yardımcı, ama **NotebookLM birincil kaynak** (ham PDF'ler orada).
