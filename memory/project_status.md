---
name: Proje Durumu
description: splitnorder güncel durumu ve sıradaki işler
type: project
updated: 2026-05-08
---

## Proje: splitnorder (BM470 İleri Java Programlama ödevi)

Müzik kaynak ayrımı (Demucs) — Java Spring backend + Python Flask audio servisi + JSP frontend.
Düzce Üniv. MF BM, Doç. Dr. Talha KABAKUŞ.

### Mevcut Durum (2026-05-20)
- **Aktif dal:** `feature/studio-redesign`
- **Build:** `mvn test` → **93/93 PASS** (Java 21)
- **Yeni özellikler:** 5 GB kota + manuel silme (Faz 1) + karma stem mix Kaggle Flask üzerinden (Faz 2). ADR-13. result.jsp'de mix paneli aktif. NotebookLM `bm470` ile slayt-uyumu doğrulandı (SUM, cascade, @Transactional).
- **Kaggle deploy:** `docs/kaggle_demucs_server.py`'a `/api/mix` route eklendi — Yusuf'un Kaggle notebook'una yeni versiyonu push etmesi gerekiyor (numpy + soundfile pip install).
- **DB:** `mixed_tracks` tablosu `hbm2ddl.auto=update` ile otomatik oluşur; mevcut `jobs` tablosuna `original_file_size BIGINT` kolonu otomatik eklenir.

### Eski Durum (2026-05-08)
- **Aktif dal:** `yusuf2` — `origin/main` merge edildi, lokalde 8 commit ileride (push edilmedi)
- **GitHub:** https://github.com/importanttobecalm/splitnorder/tree/yusuf2
- **Build:** `mvn test -Dtest='!OracleMySQLConnectionTest'` → **20/20 PASS**
- **Ders kurallarına uyum:** %100 (13/13 ADR ✅) — bkz. `docs/decisions/`
- **Canlı demo:** ✅ `https://splitnorder.space/` — vespay Oracle VM'de docker compose (geçici, ödev sonrası silinecek). Detay: `memory/user_splitnorder_demo_deploy.md`

### Mimari Özet
- Spring 6.0.4 (saf, Boot yok) + `WebApplicationInitializer`
- Hibernate 6.1.7 + c3p0 + Oracle Cloud MySQL (bastion tunnel)
- DAO'lar **CriteriaBuilder** (jakarta.persistence.criteria) — JPQL kalmadı
- JUnit 5 (Jupiter 6.0.3) + Mockito 5.21.0 + byte-buddy 1.15.11 override
- slf4j 2.0.17 + slf4j-reload4j (log4j 1.x stili `log4j.properties`)
- JSP + JSTL + i18n (TR + EN)

### Komutlar
```bash
# Test (Maven JDK 21 ile çalışmalı)
JAVA_HOME=$(/usr/libexec/java_home -v 21) PATH=$JAVA_HOME/bin:$PATH mvn test -Dtest='!OracleMySQLConnectionTest'

# Oracle MySQL entegrasyon testi (bastion tunnel açıkken)
JAVA_HOME=$(/usr/libexec/java_home -v 21) PATH=$JAVA_HOME/bin:$PATH mvn test
```

### ✅ Bu Oturumda Tamamlanan (2026-05-01)
- 12 ADR (`docs/decisions/`) + CLAUDE.md "Karar Özetleri" router tablosu
- Eski "borç" listesi NotebookLM ile çapraz doğrulandı, çoğu **iptal** (paket adı, Hibernate sürümü)
- JUnit 4 → 5 migrasyonu (6 test sınıfı, hepsi yeşil)
- log4j 1.2.14 → slf4j-reload4j 2.0.17 (slayt birebir)
- 6 JPQL sorgusu → CriteriaBuilder (3 DAO)
- Maven 3.9.15 + OpenJDK 21 pipeline kurulu
- Memory sistemi repo'ya alındı (`memory/user_*.md` hariç)

### 🟢 Açık İş Yok
Ders teslim kriterleri açısından geliştirme tamamlanmış sayılır. Eksiklik yok.

### Olası Sıradaki Adımlar (kullanıcı isterse)
- Proje raporu PDF'i (rapor şablonuna göre): tablo, ER diyagramı, request URI tablosu, log örnekleri, açıklamalı testler
- `yusuf2` → `main` PR açılması
- Oracle Compute'a fresh deploy + canlı log örneği toplama
- Frontend UX iyileştirme (i18n switcher, hata mesajları)

### Son Oturum Notu (2026-05-01)
**Yapıldı:** ADR sistemi + JUnit 5 + log4j 2.x stack + CriteriaBuilder. Tüm değişiklikler `yusuf2` dalında commit + push.
**Test durumu:** 20/20 yeşil.

### Son Oturum Notu (2026-05-08)
**Yapıldı:**
- `yusuf2` dalına `origin/main` merge edildi (Kaggle GPU + dev.sh + setup-mac.sh + JSP refresh getirdi). Memory dosyaları korundu. **Henüz push edilmedi**, lokalde 8 commit ileride.
- Berkan'ın Oracle VM'i için SSH key oluşturuldu, public key Berkan'a verildi (authorized_keys'e ekleyince `ssh oracle` çalışacak). Detay: `memory/user_ssh_access.md`.
- Yusuf'un kendi Oracle VM'i (`vespay`, 130.61.66.0) sağlığı kontrol edildi: çok rahat, image hijyeni dışında temiz.
- **splitnorder canlı demoya alındı:** `https://splitnorder.space/` — Let's Encrypt TLS, Caddy reverse proxy, docker compose. ROOT.war + MySQL 8.0 + Tomcat 10.1-jdk17. Detay + cleanup: `memory/user_splitnorder_demo_deploy.md`.
- DNS: GoDaddy'de `@` ve `www` A kayıtları → 130.61.66.0 (Yusuf ekledi).
- Frontend smoke test: `/`, `/upload`, `/history` 200 ✓. Türkçe i18n çalışıyor.

**Kaggle Flask endpoint'leri çözüldü (sync + her zaman 200 yaklaşımı):**
- `POST /api/separate` — Demucs blocking, 200 dön (~8 sn)
- `GET /api/job/{id}/status` — her zaman `{"status":"completed"}` (Java polling ilk denemede biter)
- `GET /api/stem/{id}/{stemType}` — stem WAV `send_file`
Gerekçe: Demucs 8 sn sürüyor, async/polling overhead'i değmiyor. Detay: `memory/gotchas.md` ve `user_splitnorder_demo_deploy.md`.

**Aktif Kaggle ngrok URL:** `https://approval-licking-thread.ngrok-free.dev` (notebook session düşerse ölür → yeni URL al → `set-gpu-url.sh` ile inject et).

**Sıradaki:** Yusuf Kaggle Flask endpoint'lerini ekleyince upload akışı çalışacak. Sonrası: ödev sunumu + cleanup.
