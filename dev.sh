#!/usr/bin/env bash
# splitnorder — Lokal Mac geliştirme workflow'u.
# • MySQL'e Compute VM (oracle) üzerinden SSH tunnel'i açar (yoksa).
# • Tomcat'i mvn cargo ile başlatır, Kaggle/Colab ngrok URL'ine yönlendirir.
#
# Kullanım:
#   ./dev.sh https://xxx.ngrok-free.dev    # GPU URL ver (kaydedilir)
#   ./dev.sh                                # son kaydedilen URL'i kullanır
#
# Önkoşul: ~/.ssh/config'da `Host oracle` tanımlı + key authorized_keys'te.
set -euo pipefail
cd "$(dirname "$0")"

URL_FILE="$HOME/.ngrok_url"
if [ "${1-}" != "" ]; then
  echo "$1" > "$URL_FILE"
fi
if [ ! -f "$URL_FILE" ]; then
  echo "❌  Önce GPU API URL'i ver: ./dev.sh https://xxx.ngrok-free.dev"
  exit 1
fi
GPU_URL=$(cat "$URL_FILE")

# ── MySQL tunnel kontrol/aç ────────────────────────────────────────────
if ! nc -z localhost 3306 2>/dev/null; then
  echo "🔐  MySQL tunnel açılıyor (oracle-db → 10.0.1.212:3306)..."
  ssh -fN oracle-db
  sleep 1
  if ! nc -z localhost 3306 2>/dev/null; then
    echo "❌  Tunnel açılamadı. ssh oracle-db çalışıyor mu? ~/.ssh/config kontrol et."
    exit 1
  fi
  echo "✅  Tunnel hazır."
else
  echo "✅  MySQL tunnel zaten açık."
fi

echo "🌐  GPU URL  : $GPU_URL"
echo "🩺  Health   : $GPU_URL/api/health"
echo "▶️   Tomcat  : http://localhost:8090/"
echo "──────────────────────────────────────────────────────────"
# package + cargo:run → her seferinde kod değişikliklerini recompile eder
exec mvn -DskipTests package cargo:run "-Dcolab.api.url=$GPU_URL"
