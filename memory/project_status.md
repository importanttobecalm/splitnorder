---
name: Proje Durumu
description: splitnorder güncel durumu ve sıradaki işler
type: project
updated: 2026-05-01
---

## Proje: splitnorder (BM470 İleri Java Programlama ödevi)

Müzik kaynak ayrımı (Demucs) — Java Spring backend + Python Flask audio servisi + JSP frontend.
Düzce Üniv. MF BM, Doç. Dr. Talha KABAKUŞ.

### Mevcut Durum (2026-05-01)
- **Aktif dal:** `yusuf2` — `origin/yusuf2`'ye push edildi (8c3a162)
- **GitHub:** https://github.com/importanttobecalm/splitnorder/tree/yusuf2
- **Build:** `mvn test -Dtest='!OracleMySQLConnectionTest'` → **20/20 PASS**
- **Ders kurallarına uyum:** %100 (13/13 ADR ✅) — bkz. `docs/decisions/`

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
**Sıradaki:** Kullanıcı yönlendirecek (rapor PDF'i, PR, deploy, vb.).
