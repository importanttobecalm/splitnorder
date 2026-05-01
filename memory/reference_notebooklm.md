---
name: NotebookLM MCP Referansı
description: BM470 ders PDF'lerine erişim için NotebookLM MCP yapılandırması
type: reference
updated: 2026-05-01
---

## Notebook
- **Başlık:** Düzce University Advanced Java Programming Project Requirements
- **ID:** `0e624500-c0de-4aa2-b2f4-a3b290c04257`
- **Alias:** `bm470` (CLI ile `nlm ... bm470` kullanılabilir)
- **Kaynak sayısı:** 12 PDF (10.pdf, 11.pdf, 1_unlocked.pdf … 6_unlocked.pdf vb.)
- **Hesap:** m.yusufzehraa@gmail.com

## Kurulum (yapıldı)
- Paket: `notebooklm-mcp-cli` v0.6.1 (pipx ile, Python 3.12.7)
- Komutlar: `nlm`, `notebooklm-mcp` (`/Users/yusufbulut/.local/bin/`)
- Cookie: `~/.notebooklm-mcp-cli/profiles/default`
- MCP scope: **local** (sadece bu projede, `/Users/yusufbulut/.claude.json`)
- MCP server: `notebooklm-mcp` (✓ Connected — `claude mcp list` ile doğrulanabilir)

## Kullanım
- **Bu projede neden var:** BM470 ders PDF'lerine context'i şişirmeden erişmek. PDF'leri tek tek okumak yerine NotebookLM'e sorgu at, sadece ilgili parçayı al.
- Claude Code içinde MCP araçları MCP server bağlandıktan sonra session yeniden başlatılınca yüklenir.
- CLI alternatifi: `nlm chat start bm470` (interactive), `nlm source list bm470`, `nlm note list bm470`.

## Yararlı CLI Komutları
- `nlm notebook list` — tüm notebook'lar
- `nlm source list bm470` — bu notebook'taki PDF kaynakları
- `nlm alias list` — alias'lar
- `nlm doctor` — kurulum/auth tanılama

## Yeniden Auth
Cookie expire olursa: `nlm login`
