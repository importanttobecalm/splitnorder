#!/usr/bin/env bash
# splitnorder — Tek komutla VM üzerinde geliştirme/Tomcat ayağa kalkar.
# Kullanım:
#   ./dev.sh https://xxx.ngrok-free.dev    # Colab URL ile başlat (URL'i kaydeder)
#   ./dev.sh                                # son kaydedilen URL'i kullanır
set -euo pipefail
cd "$(dirname "$0")"

URL_FILE="$HOME/.ngrok_url"
if [ "${1-}" != "" ]; then
  echo "$1" > "$URL_FILE"
fi
if [ ! -f "$URL_FILE" ]; then
  echo "HATA: Önce ngrok URL ver: ./dev.sh https://xxx.ngrok-free.dev"
  exit 1
fi
COLAB_URL=$(cat "$URL_FILE")
echo "🌐 Colab URL : $COLAB_URL"
echo "🩺 Health    : $COLAB_URL/api/health"
echo "▶️  Tomcat    : http://89.168.74.197:8090/stemsep/"
echo "──────────────────────────────────────────────────────────"
exec mvn cargo:run "-Dcolab.api.url=$COLAB_URL"
