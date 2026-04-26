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

## 2. Java Backend → Tomcat (Oracle Compute VM — **FREE Tier**)

> Bu kısım Oracle Always Free tier'a sığar — para harcanmaz.

### Compute Instance Gereksinimleri
- **Shape**: `VM.Standard.A1.Flex` (ARM Ampere) — **Always Free**: 4 OCPU + 24 GB RAM bedava
  - Alternatif: `VM.Standard.E2.1.Micro` (AMD x86, 1/8 OCPU) — daha kısıtlı ama AMD uyumluluğu için
- **OS**: **Oracle Linux 8** (önerilen, RPM tabanlı, dnf kolay) veya Ubuntu 22.04
- **Subnet**: `stemsep-vcn` içinde, MySQL HeatWave ile **aynı VCN** (private → MySQL'e direkt erişim)
- **Public IP**: Yes (web traffic için 8080)
- **SSH key**: Mevcut `~/.ssh/oracle_key.pub` (zaten yüklü bastion'a, aynı key)
- **Disk**: 50 GB yeterli (Always Free 200 GB block storage hakkı)

### Security List Kuralları (`stemsep-vcn` default Security List'ine ekle)
| Yön | Port | Kaynak | Amaç |
|-----|------|--------|------|
| Ingress | 22 | Kendi public IP/32 | SSH |
| Ingress | 8080 | 0.0.0.0/0 | HTTP web (test için; production'da nginx + 443) |
| Ingress | 3306 | 10.0.0.0/16 | MySQL (zaten açık) |

### Security List Kuralları
| Yön | Port | Kaynak | Amaç |
|-----|------|--------|------|
| Ingress | 22 | Sadece kendi IP'in | SSH |
| Ingress | 8080 (veya 80/443) | 0.0.0.0/0 | HTTP web |
| Egress | 3306 | 10.0.0.0/16 (VCN) | MySQL |

### VM Kurulum (Tek Seferlik)

`deploy/README.md` dosyasında **adım adım komutlar** var. Özet:

1. **VM oluştur** (Oracle Console → Compute → Create instance)
2. **SSH** ile bağlan: `ssh -i ~/.ssh/oracle_key opc@<VM_PUBLIC_IP>`
3. **Java 17 + Tomcat 10** kur (Oracle Linux için `sudo dnf install -y java-17-openjdk` + Tomcat tar.gz)
4. **`tomcat` user** + `/opt/tomcat10` dizini
5. `deploy/secrets.env.example` → `/etc/stemsep/secrets.env` (chmod 600, şifre gir)
6. `deploy/tomcat-stemsep.service` → `/etc/systemd/system/`
7. `sudo systemctl enable --now tomcat-stemsep`
8. Firewall: `sudo firewall-cmd --add-port=8080/tcp --permanent && sudo firewall-cmd --reload`

### Yeni Sürüm Yayınlama (Her Defasında)

Lokal Mac'ten:
```bash
mvn clean package -DskipTests
./deploy/deploy.sh <VM_PUBLIC_IP> opc
```

Bu script: WAR'ı scp ile yollar → systemd ile durdurup başlatır → log'u gösterir.

URL: `http://<VM_PUBLIC_IP>:8080/stemsep/`

### URL Override Mantığı

Repo'da `hibernate.properties` lokal için `127.0.0.1` (bastion tunnel ile dev). Sunucuda systemd unit'in `CATALINA_OPTS`'unda:
```
-Dmysql.url=jdbc:mysql://10.0.1.212:3306/stemsep_db?...
```
Spring Environment system property'leri @PropertySource'tan **yüksek öncelikte** okur — yani repo dosyası dokunulmuyor, sadece JVM startup arg'ı override ediyor. Aynı yöntemle `colab.api.url` de Colab/Modal/Oracle GPU URL'leri arasında değiştirilir.

## 3. Demucs Flask Service — GPU Seçenekleri

### Neden Önce Ücretsiz?

Mevcut `ColabInferenceService` zaten **swap-friendly** tasarlandı: `colab.api.url` property'si ile GPU servisinin URL'i değişir. Yani önce Colab/Kaggle ile başlayıp sonra Oracle GPU veya Modal'a geçmek **tek satır config değişikliği**.

### Karşılaştırma (Demucs için)

| Platform | Ücretsiz GPU | Limit | Kurulum | Üretim Uygunluğu |
|----------|--------------|-------|---------|------------------|
| **Google Colab Free** | T4 16GB | ~12h/gün, kesintili | 5 dk (notebook + ngrok) | Demo/test |
| **Kaggle Notebooks** | T4×2 / P100 | 30 saat/hafta | 10 dk | Demo/sunum |
| **Modal.com** | A10 (1$ free credit/ay) | ~$0.60/h | Python decorator | ✅ Üretim |
| **Lightning AI** | T4 | 22 saat/ay | Studio interface | Sunum |
| **Oracle GPU** (`VM.GPU.A10.1`) | ❌ Ücretsiz YOK | $300 trial credit | systemd setup (§3 altı) | ✅ Üretim |

### Önerilen Yol

1. **İlk demo / proje sunumu**: Google Colab notebook + ngrok tunnel (FREE)
2. **Ödev gönderim öncesi gerçek test**: Oracle GPU $300 trial (~100 saat A10)
3. **Hocaya canlı demo**: Colab veya Modal — her zaman ayakta

### Colab Notebook Şablonu (5 Dakikalık Setup)

Colab'da yeni notebook → şu hücreyi çalıştır:

```python
!pip install -q demucs flask pyngrok
!ngrok authtoken YOUR_NGROK_TOKEN  # ngrok.com'dan ücretsiz

# demucs-server/app.py'i upload et veya içeri yapıştır
%%writefile app.py
# ... (mevcut app.py içeriği)

from threading import Thread
from pyngrok import ngrok

def run_flask():
    import app
    app.app.run(host='0.0.0.0', port=5000, debug=False, use_reloader=False)

Thread(target=run_flask, daemon=True).start()
public_url = ngrok.connect(5000).public_url
print(f"🌐 Public URL: {public_url}")
```

Sonra Java backend tarafında (lokal veya sunucu):
```bash
# Çalışırken JVM args ile override
-Dcolab.api.url=https://abc-123-456.ngrok-free.app
```

Veya sunucuda systemd unit'in `CATALINA_OPTS` satırında değiştir + `sudo systemctl restart tomcat-stemsep`.

### Oracle Cloud GPU (Üretim İçin — $300 Trial Sonrası Karar)

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
