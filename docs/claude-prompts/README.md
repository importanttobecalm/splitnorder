# Paralel Çalışma Rehberi (3 Track)

Üç ekibe iş bölünmüş hâlde. Her track aynı `yusuf2` dalına commit atar, **farklı dosyalarda** çalıştığı için git merge çakışması olmaz.

## Track A — JSP + i18n (mevcut Claude sessionunda devam ediyor)
**Bekleyen:** upload (3 state), processing, processing-error, studio-result, history (4 state), profile + tüm i18n key'leri

## Track B — Backend Slayt-Uyumlu Temizlik
**Yeni terminal aç:** `cd ~/Documents/Projelerim/JAVAodev/splitnorder && claude`

Sonra Claude'a yaz:
```
docs/claude-prompts/track-b-backend-slayt-temizlik.md dosyasını oku ve oradaki talimatları sırasıyla uygula. Memory + ADR'ları önce oku, sonra başla.
```

## Track C — Rapor Markdown Taslağı
**Yeni terminal aç:** `cd ~/Documents/Projelerim/JAVAodev/splitnorder && claude`

Sonra Claude'a yaz:
```
docs/claude-prompts/track-c-rapor-taslagi.md dosyasını oku ve oradaki talimatları uygula. docs/report/rapor.md'i şablon dökülmeye hazır biçimde yaz.
```

## Çakışma Önleme

- **Track A** sadece: `src/main/webapp/WEB-INF/views/**` + `src/main/resources/messages_*.properties`
- **Track B** sadece: `src/main/java/**` + `src/main/resources/log4j.properties` + `pom.xml`
- **Track C** sadece: `docs/report/**`

`HomeController.java` ortak temas noktası — Track B prompt'unda Track A'nın eklediği GET routes hakkında uyarı var, sadece taşıma yapacak.

## Senkronizasyon

Her track her commit'ten önce `git pull --rebase` çalıştırırsa, çakışma sıfıra yakın. Push etmediğimiz için merge sorunu lokal.

## İlerleme Takibi

Üç track'i koordine etmek için periyodik:
```bash
git log --oneline --graph -30
```

Hangi track ne yaptı görmek için:
```bash
git log --oneline --grep="feat(view)"        # Track A
git log --oneline --grep="refactor(auth)"    # Track B
git log --oneline --grep="docs(report)"      # Track C
```
