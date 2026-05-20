# ADR-13 — Depo Kotası + Karma Stem Mix

**Statü:** ✅ Kabul
**Tarih:** 2026-05-20

## Bağlam

Proje ilk versiyonunda kullanıcı sınırsız upload yapabiliyor; üretilen stem'ler ve orijinal dosyalar diskte birikiyordu. Ayrıca kullanıcı yalnızca 4 sabit stem'i (vocals/drums/bass/other) tek tek veya hepsini toplu ZIP olarak indirebiliyordu — **alt küme seçip yeni bir mix üretme** (örn. yalnız vokal+davul) yoktu.

İki gereksinim eklendi:
1. **Kullanıcı başına 5 GB kota** + manuel silme (geçmiş dosyalar görüntülenip silinebilir).
2. **Karma mix indirme:** stem'leri çapraz birleştirip yeni bir parça olarak indirme + ön-kayıtla yeniden erişim.

## Karar

### A) Depo Kotası

- **Sınır:** kullanıcı başına `5 GB = 5 * 1024^3 bytes` (sabit, `StorageQuotaService.QUOTA_BYTES`). Orijinal yüklenen dosya + üretilen stem'ler + üretilen mix'ler toplamı sayılır.
- **Hesaplama:** `CriteriaBuilder.sum()` — slayt birebir (8.pdf "sum Projeksiyonu"). İki ayrı sorgu (jobs.original_file_size + stems.file_size) toplanır.
- **Uyarı eşiği:** %80 — UI'da renk değişir; %100'de yeni upload reddedilir.
- **Dolu davranışı:** HTTP 507 `StorageQuotaExceededException`. Otomatik silme YOK — kullanıcı manuel siler. (Kullanıcı tercihi.)
- **Silme:** stem klasörü recursive FS + orijinal dosya + DB cascade (`@OneToMany cascade=ALL orphanRemoval=true` Job → Stems üzerinde zaten vardı — slayt 7.pdf birebir). FS önce, DB sonra (rollback → yarım FS + sağlam DB, idempotent retry).
- **UI:** `/history` sayfasına entegre (ayrı `/storage` açılmadı — daha az dosya, tek yerden yönetim).

### B) Karma Stem Mix

- **Birleştirme yeri:** **Kaggle Flask** (mevcut Python audio servisi). Java'da değil. Sebepler:
  1. Kullanıcı talebi: çıktı **depoda kalsın** — client-side olsa dosyayı sunucuya yüklemek gerekirdi (büyük WAV round-trip + karmaşa).
  2. Python tarafı zaten ses işliyor (Demucs/ffmpeg/numpy hazır). Pure Java mixer yerine `numpy.sum + normalize` 10 satır.
  3. Java tarafı Demucs `/api/separate` çağrısı ile **aynı HTTP pattern** kullanır (`ColabInferenceService` HttpURLConnection). Mevcut mimarinin doğal genişlemesi, ders raporunda tutarlı anlatım.
- **Endpoint:** `POST /api/mix` body `{job_id, stems: ["vocals","drums"], fmt: "mp3"|"wav"}` → ham dosya stream'i (`send_file`).
- **Persistence:** **Ayrı `mixed_tracks` tablosu** + `MixedTrack` entity. Sebep: `stems` tablosunun semantiği "4 sabit ayrılmış kaynak". Mix farklı bir kavram (kaynak stem alt kümesinden türemiş, kompozit); aynı tabloya `is_mix` + `source_stems` kolonu eklemek polymorphism kirliliği yaratırdı.
- **Mix dosyaları kotaya sayılır** (`StorageQuotaService.getUsedBytes` mix dosyalarını da toplar — Faz 2.2'de DAO genişletilecek).
- **UI:** React studio sayfasında stem node'larına checkbox + "Seçilenleri Birleştir" toolbar; sonuç graph'a yeni "Custom Mix" node olarak eklenir, indir/sil butonu ile yönetilir. Sayfa açılışında DB'den restore.

## Gerekçe (Slayt Uyumu)

- **CriteriaBuilder.sum:** 8.pdf slayt birebir (öğrenci sınıflarının toplamı örneği) ✅
- **Cascade silme:** 7.pdf "Entity'ler Arası İlişkiler (II)" — `cascade=ALL orphanRemoval=true` (Fakulte → Bolum) ✅
- **@Transactional:** 7.pdf "Örnek Service Sınıfı" — `@Transactional(readOnly=false)` write/delete ✅
- **Slayt-dışı bileşenler (gerekçeli):**
  - `java.io.File` ile fiziksel dosya silme: Slaytta yok ama proje audio backend olarak FS'te dosya tutmak zorunda; alternatif (DB BLOB) hem büyük dosyalar için pratik değil hem de mevcut yapı bozulur.
  - Harici REST servisine HttpURLConnection POST: Slaytta yok ama proje **zaten** `ColabInferenceService` ile bu pattern'i kullanıyor (Demucs entegrasyonu). Yeni endpoint mevcut pattern'in genişlemesi.

## Alternatifler

- **Kota = sadece stem'ler (orijinal hariç):** Reddedildi. Kullanıcı 5 GB orijinal yüklerse stem'ler için yer kalmazdı; kota anlamsız olur.
- **Otomatik en eski silme:** Reddedildi (kullanıcı tercihi). Kullanıcı kontrolünde olmalı.
- **Mix'i Java pure ffmpeg subprocess:** Reddedildi. Java tarafı ses formatları + encoding karmaşası, Python zaten ortamı hazır.
- **Mix client-side Web Audio API:** Reddedildi. "Depoda kalsın" gereksinimi + büyük WAV dosyaları için tarayıcı performansı zayıf + mobil/zayıf cihaz desteği bozulur.
- **`stems` tablosuna `is_mix` flag:** Reddedildi (semantik kirlilik — ayrı tablo daha temiz).

## Etkilenen Dosyalar

**Faz 1:**
- `model/Job.java` (originalFileSize)
- `dao/JobDao.java`, `dao/StemDao.java` (sum metodları)
- `service/StorageQuotaService.java` (yeni)
- `service/JobService.java` (deleteJob)
- `exception/StorageQuotaExceededException.java`, `ErrorCode.java`
- `controller/UploadController.java`, `controller/HistoryController.java`
- `views/history.jsp`
- `messages_tr_TR.properties`, `messages_en_US.properties`

**Faz 2 (planlı):**
- `model/MixedTrack.java`, `dao/MixedTrackDao.java` (yeni)
- `service/MixService.java` (yeni)
- `controller/MixController.java` (yeni)
- `frontend-prototype/src/studio/` (checkbox + mix node)
- Kaggle notebook'unda `/api/mix` endpoint

## Sonuç (Faz 1 itibarıyla)

- 71 → 82 test ✅ (11 yeni)
- 5 GB kota canlı, dolduğunda upload reddediliyor (507)
- Kullanıcı manuel silme yapabiliyor; FS + DB temiz
- ADR-13 Faz 2 ile genişletilecek
