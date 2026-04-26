# Deployment Rehberi — splitnorder

> Lokal geliştirme → Tomcat lokal → Oracle Cloud (Compute + GPU) sıralı deployment.

## 0. Pre-flight Checklist

- [ ] `mvn clean package` başarılı (WAR üretildi)
- [ ] `OracleMySQLConnectionTest` 3/3 PASS
- [ ] `hibernate.properties` lokal kopyada şifre dolu (gitignored — sunucuya elle taşınır)
- [ ] CHANGELOG güncel
- [ ] Git temiz (`git status` clean veya bilinçli WIP)

## 1. Lokal Tomcat Test (`mvn cargo:run`)

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home
# Bastion tunnel açık olmalı (localhost:3306)
mvn cargo:run
```

- Tomcat 10 embedded ayağa kalkar, `target/stemsep.war` deploy eder
- URL: `http://localhost:8080/stemsep/`
- Durdur: `Ctrl+C`

**Beklenen log satırları:**
```
INFO  HHH000412: Hibernate ORM core version 5.6.15.Final
INFO  Initializing Spring DispatcherServlet 'DispatcherServlet'
INFO  Tomcat started on port(s): 8080
```

## 2. Java Backend → Tomcat (Oracle Compute VM)

### Compute Instance Gereksinimleri
- **Shape**: VM.Standard.E4.Flex (2 OCPU, 8 GB RAM yeterli — Free Tier'a sığar)
- **OS**: Oracle Linux 8 veya Ubuntu 22.04
- **Subnet**: VCN içinde, MySQL HeatWave ile aynı (10.0.x.0/24)
- **Public IP**: Yes (web traffic için)
- **Disk**: En az 50 GB

### Security List Kuralları
| Yön | Port | Kaynak | Amaç |
|-----|------|--------|------|
| Ingress | 22 | Sadece kendi IP'in | SSH |
| Ingress | 8080 (veya 80/443) | 0.0.0.0/0 | HTTP web |
| Egress | 3306 | 10.0.0.0/16 (VCN) | MySQL |

### VM Kurulum
```bash
# Bağlan
ssh -i ~/.ssh/oracle_key opc@<VM_PUBLIC_IP>

# Java 17 + Tomcat 10
sudo dnf install -y java-17-openjdk
wget https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.36/bin/apache-tomcat-10.1.36.tar.gz
tar xzf apache-tomcat-10.1.36.tar.gz
mv apache-tomcat-10.1.36 ~/tomcat10

# WAR'ı upload et (Mac'ten)
scp -i ~/.ssh/oracle_key target/stemsep.war opc@<VM_PUBLIC_IP>:~/tomcat10/webapps/

# hibernate.properties'i upload et (gitignored, ayrı taşınmalı)
scp -i ~/.ssh/oracle_key src/main/resources/hibernate.properties \
    opc@<VM_PUBLIC_IP>:~/hibernate.properties.prod

# Sunucuda: mysql.url'i private IP'ye çevir
sed -i 's|jdbc:mysql://127.0.0.1|jdbc:mysql://10.0.1.212|' ~/hibernate.properties.prod
mv ~/hibernate.properties.prod ~/tomcat10/lib/hibernate.properties

# Başlat
~/tomcat10/bin/catalina.sh start
tail -f ~/tomcat10/logs/catalina.out
```

URL: `http://<VM_PUBLIC_IP>:8080/stemsep/`

## 3. Demucs Flask Service → Oracle Cloud GPU

### GPU Instance Gereksinimleri
- **Shape**: `VM.GPU.A10.1` (1× A10, 24GB GPU) — istek sonrası onay
- **OS**: Ubuntu 22.04 (NVIDIA driver desteği daha kolay)
- **Disk**: En az 100 GB (model dosyaları + audio cache)
- **Subnet**: Public veya bastion arkası (Java backend'den 5000 portuna erişim lazım)

### Security List
| Yön | Port | Kaynak | Amaç |
|-----|------|--------|------|
| Ingress | 22 | Sadece kendi IP'in | SSH |
| Ingress | 5000 | Backend VM CIDR (örn. 10.0.1.0/24) | Flask API |

### Kurulum (Ubuntu 22.04 üzerinde)
```bash
# NVIDIA driver + CUDA
sudo apt update && sudo apt install -y ubuntu-drivers-common
sudo ubuntu-drivers install
sudo reboot

# Reboot sonrası kontrol
nvidia-smi  # GPU görünmeli

# Python 3.10+ ortam
sudo apt install -y python3-pip python3-venv ffmpeg
python3 -m venv ~/demucs-venv
source ~/demucs-venv/bin/activate

# Demucs + dependencies
pip install --upgrade pip
pip install -r requirements.txt  # demucs-server/requirements.txt
pip install torch torchaudio --index-url https://download.pytorch.org/whl/cu121

# Modeli önceden indir (ilk istekte indirme yavaş olmasın)
python3 -c "import demucs.api; demucs.api.Separator(model='htdemucs')"

# Flask service başlat (systemd ile kalıcı)
sudo tee /etc/systemd/system/demucs.service > /dev/null <<EOF
[Unit]
Description=SplitNOrder Demucs Flask API
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/splitnorder/demucs-server
Environment="PATH=/home/ubuntu/demucs-venv/bin"
ExecStart=/home/ubuntu/demucs-venv/bin/python app.py
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now demucs
sudo systemctl status demucs
```

### Java Backend Tarafında URL Güncelle
`hibernate.properties` (sunucudaki):
```properties
colab.api.url=http://<GPU_VM_PRIVATE_IP>:5000
```

## 4. Bastion vs Public Endpoint Tradeoff

| Konfig | Avantaj | Dezavantaj |
|--------|---------|-----------|
| **Şu an: MySQL private + Bastion** | Güvenli, kimlik doğrulama 2 katmanlı | 3 saatte session expire, lokal dev yorucu |
| **MySQL public endpoint açmak** | Lokal dev'de tunnel yok | DB internete açık, sıkı NSG/IP allowlist gerekir |
| **Java backend'i bastion'la birlikte VCN içinde** | Production için doğal — backend MySQL'e direkt bağlanır | Lokal dev hâlâ tunnel ister |

**Öneri**: Production'da Java backend VCN içinde → MySQL'e direkt private IP ile bağlanır. Lokal dev'de bastion tunnel kullanmaya devam.

## 5. Rollback Prosedürü

WAR yeni sürüm bozarsa:
```bash
# Tomcat'i durdur
~/tomcat10/bin/catalina.sh stop

# Önceki WAR'a dön
cp ~/backups/stemsep-v0.3.0.war ~/tomcat10/webapps/stemsep.war

# Tekrar başlat
~/tomcat10/bin/catalina.sh start
```

DB schema migration geri almak için: Oracle MySQL HeatWave **Backup** sekmesinden snapshot restore.

## 6. Monitoring

- **Tomcat logları**: `~/tomcat10/logs/catalina.out` (`tail -f`)
- **Application logs**: `~/tomcat10/logs/bm470.log` (log4j 1.2.14 RollingFileAppender)
- **MySQL slow query**: HeatWave Console → Performance Hub
- **GPU kullanımı**: `nvidia-smi -l 1` (GPU VM'de)

## Pending Decisions

- [ ] Domain adı satın alıp HTTPS (Let's Encrypt) — şimdilik IP+port yeterli
- [ ] Object Storage stem dosyaları için (lokal disk yerine — büyüklerde scale)
- [ ] CDN frontend statik içerik için
