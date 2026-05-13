# UUID Migration — vespay Sunucu Deploy

> Job URL'leri `/job/19` → `/job/<uuid>` formatına geçti. Mevcut DB'deki
> jobların `public_id` kolonu olmadığı için NOT NULL constraint patlar.
> **Tüm jobs/stems/users verilerini silip yeniden test edeceğiz.**
>
> **Hedef sunucu:** vespay (`130.61.66.0`), Docker Compose stack
> **Komutlar:** Mac terminalinden tek satır SSH ile çalıştırılır (yapıştır → Enter).

---

## 1. WAR'ı lokalde derle (Mac)

```bash
cd /Users/yusufbulut/Documents/Projelerim/JAVAodev/splitnorder && JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean package -DskipTests
```

> Çıktıda `BUILD SUCCESS` görmelisin. `target/stemsep.war` ~43 MB.

---

## 2. WAR'ı vespay'e yükle

```bash
scp -i ~/Desktop/ssh-key-2026-02-24.key /Users/yusufbulut/Documents/Projelerim/JAVAodev/splitnorder/target/stemsep.war ubuntu@130.61.66.0:/home/ubuntu/splitnorder-demo/stemsep.war
```

---

## 3. DB temizliği + dosya temizliği + Tomcat restart + schema doğrulama (TEK SATIR)

> Yapıştır → Enter. Şifre sorulmaz (key ile auth). 8 saniye bekleyip `jobs`
> tablosunun yeni schema'sını gösterir.

```bash
ssh -i ~/Desktop/ssh-key-2026-02-24.key ubuntu@130.61.66.0 "docker exec splitnorder-mysql mysql -uroot -pO8UbXqKgLqtSTSgYdjPVBd94skCT stemsep_db -e 'SET FOREIGN_KEY_CHECKS=0; DROP TABLE stems; DROP TABLE jobs; DROP TABLE users; SET FOREIGN_KEY_CHECKS=1;' && docker exec splitnorder-tomcat sh -c 'rm -rf /usr/local/tomcat/uploads/* /usr/local/tomcat/stems/*' ; docker restart splitnorder-tomcat && sleep 8 && docker exec splitnorder-mysql mysql -uroot -pO8UbXqKgLqtSTSgYdjPVBd94skCT stemsep_db -e 'DESCRIBE jobs;'"
```

**Beklenen çıktı:** `DESCRIBE jobs` tablosunda **`public_id | varchar(36) | NO | UNI`** satırı görünmeli.

---

## 4. Tomcat startup loglarını izle (opsiyonel)

```bash
ssh -i ~/Desktop/ssh-key-2026-02-24.key ubuntu@130.61.66.0 "docker logs --tail 50 splitnorder-tomcat"
```

> Hata aramak için: `... 2>&1 | grep -iE 'error|exception|fail'`

---

## 5. Canlı sayfayı kontrol et (tarayıcı)

1. `https://splitnorder.space/auth/register` → yeni test kullanıcısı
2. Doğrulama maili gelir → linke tıkla
3. Login ol → bir audio dosyası yükle
4. URL'ye bak: `/?jobId=<36 karakterli uuid>` formatında olmalı
5. History sayfasında joblar açılsın, hepsi UUID URL

### Stems klasörü kontrolü

```bash
ssh -i ~/Desktop/ssh-key-2026-02-24.key ubuntu@130.61.66.0 "docker exec splitnorder-tomcat ls /usr/local/tomcat/stems/"
```

> Klasör adları UUID formatında olmalı (eski `19`, `20` görünmez — DB temizlendi).

---

## Sorun çıkarsa

### Tomcat başlamadı / 502 dönüyor
```bash
ssh -i ~/Desktop/ssh-key-2026-02-24.key ubuntu@130.61.66.0 "docker logs --tail 100 splitnorder-tomcat 2>&1 | tail -40"
```

### Schema'da `public_id` yok
Hibernate `ddl-auto=update` bazen kolon eklemez. 3. adımdaki DROP TABLE zaten tabloyu sıfırdan kurduğu için bu durumda olmamalı. Yine de:
```bash
ssh -i ~/Desktop/ssh-key-2026-02-24.key ubuntu@130.61.66.0 "docker exec splitnorder-mysql mysql -uroot -pO8UbXqKgLqtSTSgYdjPVBd94skCT stemsep_db -e 'ALTER TABLE jobs ADD COLUMN public_id VARCHAR(36) NOT NULL UNIQUE;'"
```

### Mail gelmiyor
SMTP config gitignored `hibernate.properties`'te. Tomcat loglarında `EmailService` hatası var mı bak:
```bash
ssh -i ~/Desktop/ssh-key-2026-02-24.key ubuntu@130.61.66.0 "docker logs splitnorder-tomcat 2>&1 | grep -i email | tail -20"
```
