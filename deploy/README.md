# Production Deployment — Oracle Cloud

## Dosyalar

| Dosya | Görev | Konum (sunucuda) |
|-------|-------|------------------|
| `tomcat-stemsep.service` | systemd unit | `/etc/systemd/system/` |
| `secrets.env.example` | Şifre template (kopyala, düzenle) | `/etc/stemsep/secrets.env` (chmod 600) |
| `deploy.sh` | Lokal Mac'ten WAR yükleme + restart | (lokal'de çalışır) |

> `secrets.env` ve gerçek `hibernate.properties` git'e gitmez.

## İlk Kurulum (VM'de bir kez)

```bash
# Java 17
sudo dnf install -y java-17-openjdk      # Oracle Linux
# veya
sudo apt install -y openjdk-17-jre        # Ubuntu

# Tomcat 10
TOMCAT_VER=10.1.36
sudo mkdir -p /opt
cd /tmp
wget https://dlcdn.apache.org/tomcat/tomcat-10/v$TOMCAT_VER/bin/apache-tomcat-$TOMCAT_VER.tar.gz
sudo tar xzf apache-tomcat-$TOMCAT_VER.tar.gz -C /opt
sudo mv /opt/apache-tomcat-$TOMCAT_VER /opt/tomcat10

# tomcat user
sudo useradd -r -d /opt/tomcat10 -s /bin/false tomcat
sudo chown -R tomcat:tomcat /opt/tomcat10
sudo chmod +x /opt/tomcat10/bin/*.sh

# Secrets
sudo mkdir -p /etc/stemsep
sudo cp /tmp/secrets.env /etc/stemsep/secrets.env   # önce scp ile yükle
sudo chmod 600 /etc/stemsep/secrets.env
sudo chown tomcat:tomcat /etc/stemsep/secrets.env

# systemd
sudo cp /tmp/tomcat-stemsep.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable tomcat-stemsep

# Firewall (Oracle Linux firewalld)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
# Ubuntu: sudo ufw allow 8080
```

## Yayın Çıkarma (Her Yeni Sürüm)

Lokal Mac'ten:

```bash
mvn clean package -DskipTests
./deploy/deploy.sh <VM_PUBLIC_IP> opc
```

Script: WAR'ı `scp` ile yollar → Tomcat durdurur → `webapps/`'e kopyalar → başlatır → log gösterir.

## URL Override Mantığı

`hibernate.properties` repo'da `127.0.0.1` (lokal dev için). Sunucuda systemd unit `CATALINA_OPTS`'ta `-Dmysql.url=jdbc:mysql://10.0.1.212:...` ile override eder. Spring Environment system property'leri @PropertySource'tan yüksek öncelikte okur.

Aynı pattern: `-Dcolab.api.url=http://...` ile GPU servisinin URL'i de değiştirilir (Colab/Kaggle/Modal swap için).

## Logları İzle

```bash
sudo journalctl -u tomcat-stemsep -f          # systemd logları
sudo tail -f /opt/tomcat10/logs/catalina.out  # Tomcat stdout
sudo tail -f /opt/tomcat10/logs/bm470.log     # Spring app log4j
```

## Troubleshooting

| Belirti | Neden | Çözüm |
|---------|-------|-------|
| `503 Service Unavailable` | Tomcat ayakta ama context deploy etmedi | `catalina.out`'ta exception ara |
| `404 /stemsep/` | WAR adı yanlış veya context boot fail | `webapps/`'i kontrol et, `META-INF/context.xml` |
| `Connection refused` MySQL | VCN içi private IP yanlış / NSG kapalı | `10.0.1.212`'ye bastion'sız bağlanmayı dene |
| systemd `failed (exit 1)` | secrets.env yanlış chmod / yol | `sudo systemctl status` ve journalctl |
