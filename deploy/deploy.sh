#!/usr/bin/env bash
# splitnorder — Lokal Mac'ten Oracle Compute VM'e deployment scripti
#
# Kullanım:
#   ./deploy/deploy.sh <VM_PUBLIC_IP> [ssh_user]
#
# Örnek:
#   ./deploy/deploy.sh 140.238.123.45 opc

set -euo pipefail

VM_IP="${1:?VM public IP gerekli: ./deploy/deploy.sh <IP> [user]}"
SSH_USER="${2:-opc}"
SSH_KEY="${SSH_KEY:-$HOME/.ssh/oracle_key}"
WAR="target/stemsep.war"

if [[ ! -f "$WAR" ]]; then
    echo "❌ $WAR yok — önce: mvn clean package -DskipTests"
    exit 1
fi

echo "▶ WAR: $(ls -lh $WAR | awk '{print $5, $9}')"
echo "▶ Hedef: $SSH_USER@$VM_IP"

# 1. WAR yükle
echo "▶ WAR yükleniyor..."
scp -i "$SSH_KEY" "$WAR" "$SSH_USER@$VM_IP:/tmp/stemsep.war"

# 2. Sunucuda deploy
ssh -i "$SSH_KEY" "$SSH_USER@$VM_IP" 'bash -s' <<'REMOTE'
set -euo pipefail

CATALINA_HOME=/opt/tomcat10
WEBAPPS=$CATALINA_HOME/webapps

# Tomcat'i durdur (varsa)
sudo systemctl stop tomcat-stemsep || true
sleep 3

# Eski WAR + extracted dir temizle
sudo rm -rf $WEBAPPS/stemsep $WEBAPPS/stemsep.war

# Yeni WAR'ı koy
sudo cp /tmp/stemsep.war $WEBAPPS/stemsep.war
sudo chown tomcat:tomcat $WEBAPPS/stemsep.war
rm /tmp/stemsep.war

# Tomcat başlat
sudo systemctl start tomcat-stemsep
sleep 5

# Status
sudo systemctl status tomcat-stemsep --no-pager | head -10
echo "---son 20 log satırı---"
sudo tail -20 $CATALINA_HOME/logs/catalina.out
REMOTE

echo ""
echo "✅ Deploy tamam. URL: http://$VM_IP:8080/stemsep/"
echo "   Logları izle: ssh -i $SSH_KEY $SSH_USER@$VM_IP 'sudo tail -f /opt/tomcat10/logs/catalina.out'"
