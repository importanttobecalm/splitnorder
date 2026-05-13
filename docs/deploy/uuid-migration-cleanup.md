# UUID Migration — Sunucu Temizlik & Deploy Adımları

> Job URL'leri `/job/19` → `/job/<uuid>` formatına geçti. Mevcut DB'deki
> jobların `public_id` kolonu olmadığı için NOT NULL constraint patlar.
> **Tüm jobs/stems/users verilerini silip yeniden test edeceğiz.**
>
> Komutları sırayla, **bir önceki başarılı olduktan sonra** çalıştır.

---

## 1. Projeyi sunucuda güncelle

```bash
cd /path/to/splitnorder
git pull origin feature/studio-redesign
```

## 2. WAR'ı derle

```bash
mvn clean package -DskipTests
```

## 3. Tomcat'i durdur (DB temizliği öncesi)

```bash
docker stop splitnorder-tomcat
```

## 4. MySQL'e bağlan

```bash
docker exec -it splitnorder-mysql mysql -uroot -p stemsep_db
```

> Şifre sorulunca root şifresini gir (compose dosyasında tanımlı olan).

## 5. Tabloları temizle (MySQL prompt içinde)

```sql
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE stems;
TRUNCATE TABLE jobs;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;
EXIT;
```

> Eğer `public_id` kolonu yeni schema'da otomatik eklenmezse (Hibernate
> `update` modu bazen ALTER yapmaz), aşağıdaki fallback'i çalıştır:
>
> ```sql
> DROP TABLE stems;
> DROP TABLE jobs;
> DROP TABLE users;
> EXIT;
> ```
>
> Tomcat startup'ta tablolar sıfırdan oluşur.

## 6. Yüklenen audio dosyalarını sil

```bash
docker run --rm -v splitnorder_uploads:/data alpine sh -c 'rm -rf /data/*'
docker run --rm -v splitnorder_stems:/data alpine sh -c 'rm -rf /data/*'
```

> Eğer volume isimleri farklıysa (compose'da kontrol et), şunu kullan:
>
> ```bash
> docker exec splitnorder-tomcat sh -c 'rm -rf /usr/local/tomcat/uploads/* /usr/local/tomcat/stems/*'
> ```
> (Bu komutu Tomcat **çalışırken** çalıştırman gerekir, 3. adımı geçici skip et.)

## 7. WAR'ı Tomcat'e kopyala

```bash
docker cp target/splitnorder.war splitnorder-tomcat:/usr/local/tomcat/webapps/ROOT.war
```

> WAR adı / hedef path projende farklıysa mevcut deploy script'ini kullan.

## 8. Tomcat'i başlat

```bash
docker start splitnorder-tomcat
```

## 9. Tomcat loglarını izle (schema kurulumu için)

```bash
docker logs -f splitnorder-tomcat
```

> Şu satırı görmelisin:
> ```
> Hibernate: alter table jobs add column public_id varchar(36) not null
> ```
> veya tablolar drop edildiyse:
> ```
> Hibernate: create table jobs (id bigint not null auto_increment, public_id varchar(36) not null, ...)
> ```
>
> Hata olmadan startup tamamlanınca `Ctrl+C` ile logdan çık.

## 10. Şema kontrolü (opsiyonel)

```bash
docker exec -it splitnorder-mysql mysql -uroot -p stemsep_db -e "DESCRIBE jobs;"
```

> Çıktıda `public_id | varchar(36) | NO | UNI` satırını görmelisin.

---

## 11. Smoke Test (tarayıcıdan)

1. `https://splitnorder.space/auth/register` — yeni test kullanıcısı oluştur
2. Gelen doğrulama mailini aç, linke tıkla → login sayfasına yönlendirir
3. Login ol
4. Bir audio dosyası yükle
5. **URL'ye bak**: `https://splitnorder.space/?jobId=<36 karakterli uuid>` olmalı
6. Studio'da job tamamlandığında `/job/<uuid>` linkleri çalışmalı
7. History sayfasında listeleme + tıklama → UUID URL ile açılmalı
8. Stems klasörünü kontrol et: `docker exec splitnorder-tomcat ls /usr/local/tomcat/stems/`
   → klasörler `<uuid>` formatında olmalı (eski `19`, `20` yok)

---

## Sorun çıkarsa

- **Schema migration patladı**: 5. adımdaki fallback (`DROP TABLE`) yolunu kullan
- **Tomcat startup hata veriyor**: `docker logs splitnorder-tomcat | tail -50` ile hatayı paylaş
- **Eski URL'ler 404**: bu beklenen, eski Long ID linkleri artık geçersiz (DB'de zaten yok)
- **Public_id null hatası**: PrePersist tetiklenmiyor demektir, Job entity'sinin doğru deploy edildiğini kontrol et (`docker exec splitnorder-tomcat jar tf /usr/local/tomcat/webapps/ROOT.war | grep Job.class`)
