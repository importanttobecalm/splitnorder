#!/usr/bin/env bash
# splitnorder — Mac üzerinde lokal geliştirme ortamını sıfırdan kurar.
# Kullanım:
#   curl -fsSL https://raw.githubusercontent.com/importanttobecalm/splitnorder/main/setup/setup-mac.sh | bash
# veya repo klonlanmışsa:
#   ./setup/setup-mac.sh
#
# Bu script:
#   1. SSH key kontrolü (yoksa oluşturur, public key'i gösterir)
#   2. ~/.ssh/config'a oracle + oracle-db host'larını ekler
#   3. SSH erişimini test eder (Berkan'ın authorized_keys'e eklemesi gerek)
#   4. Repo'yu ~/Desktop/splitnorder'a klonlar
#   5. hibernate.properties'i şablondan oluşturur
#   6. mvn package ile derlemeyi doğrular

set -euo pipefail

REPO_URL="https://github.com/importanttobecalm/splitnorder"
REPO_DIR="${HOME}/Desktop/splitnorder"
SSH_KEY="${HOME}/.ssh/id_ed25519"
ORACLE_IP="89.168.74.197"
ORACLE_USER="opc"
MYSQL_PRIVATE_IP="10.0.1.212"
MYSQL_USER="admin"
MYSQL_PASSWORD='Yusuf%505!#'

say()  { printf "\033[1;36m▶ %s\033[0m\n" "$*"; }
ok()   { printf "\033[1;32m✓ %s\033[0m\n" "$*"; }
warn() { printf "\033[1;33m⚠ %s\033[0m\n" "$*"; }
err()  { printf "\033[1;31m✗ %s\033[0m\n" "$*" >&2; }

# ── 0. Önkoşullar ──────────────────────────────────────────────────────
say "Önkoşullar kontrol ediliyor..."
need=()
command -v git  >/dev/null || need+=("git")
command -v mvn  >/dev/null || need+=("maven")
command -v java >/dev/null || need+=("openjdk@17")
command -v nc   >/dev/null || need+=("netcat")
if [ ${#need[@]} -gt 0 ]; then
  err "Eksik araçlar: ${need[*]}"
  echo "Kurulum (Homebrew):"
  echo "  brew install ${need[*]}"
  exit 1
fi
JAVA_MAJOR=$(java -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\).*/\1/')
[ "$JAVA_MAJOR" = "17" ] || warn "Java $JAVA_MAJOR — proje 17 ile derleniyor, sürüm sapması olabilir."
ok "git, mvn, java $JAVA_MAJOR, nc hazır"

# ── 1. SSH key ─────────────────────────────────────────────────────────
if [ ! -f "$SSH_KEY" ]; then
  say "SSH key bulunamadı, oluşturuluyor: $SSH_KEY"
  ssh-keygen -t ed25519 -f "$SSH_KEY" -N "" -C "$(whoami)@$(hostname -s)"
  ok "Yeni SSH key oluşturuldu"
fi

PUBKEY=$(cat "${SSH_KEY}.pub")

# ── 2. ~/.ssh/config ──────────────────────────────────────────────────
mkdir -p ~/.ssh && chmod 700 ~/.ssh
touch ~/.ssh/config && chmod 600 ~/.ssh/config

if ! grep -qE "^Host oracle$" ~/.ssh/config; then
  say "~/.ssh/config'a oracle + oracle-db host'ları ekleniyor..."
  cat >> ~/.ssh/config <<EOF

# splitnorder VM (Berkan)
Host oracle
    HostName ${ORACLE_IP}
    User ${ORACLE_USER}
    IdentityFile ${SSH_KEY}
    ServerAliveInterval 60
    LogLevel ERROR

# MySQL tunnel için ayrı host (sadece dev.sh kullanır)
Host oracle-db
    HostName ${ORACLE_IP}
    User ${ORACLE_USER}
    IdentityFile ${SSH_KEY}
    ServerAliveInterval 60
    LogLevel ERROR
    LocalForward 3306 ${MYSQL_PRIVATE_IP}:3306
    ExitOnForwardFailure yes
EOF
  ok "SSH config güncellendi"
else
  ok "SSH config zaten ayarlı"
fi

# ── 3. SSH testi (authorized_keys'e eklendi mi?) ──────────────────────
say "SSH testi..."
if ! ssh -o ConnectTimeout=8 -o BatchMode=yes oracle 'true' 2>/dev/null; then
  err "SSH ile oracle'a bağlanılamadı."
  echo
  echo "Bu public key'i Berkan'a gönder (WhatsApp/Slack):"
  echo "─────────────────────────────────────────────"
  echo "$PUBKEY"
  echo "─────────────────────────────────────────────"
  echo
  echo "Berkan VM'in authorized_keys'ine ekleyecek, sonra bu script'i tekrar çalıştır:"
  echo "  $0"
  exit 2
fi
ok "SSH erişimi çalışıyor"

# ── 4. Repo clone ──────────────────────────────────────────────────────
if [ ! -d "$REPO_DIR" ]; then
  say "Repo klonlanıyor: $REPO_DIR"
  git clone "$REPO_URL" "$REPO_DIR"
fi
cd "$REPO_DIR"
git fetch origin >/dev/null 2>&1 || true
ok "Repo: $(git rev-parse --abbrev-ref HEAD) @ $(git log -1 --format=%h)"

# ── 5. hibernate.properties ────────────────────────────────────────────
HP="src/main/resources/hibernate.properties"
if [ ! -f "$HP" ]; then
  say "hibernate.properties oluşturuluyor (lokal dev için 127.0.0.1, tunnel ile)..."
  cat > "$HP" <<EOF
mysql.driver=com.mysql.cj.jdbc.Driver
mysql.url=jdbc:mysql://127.0.0.1:3306/stemsep_db?characterEncoding=utf-8&useSSL=true&serverTimezone=UTC
mysql.user=${MYSQL_USER}
mysql.password=${MYSQL_PASSWORD}

hibernate.show_sql=true
hibernate.format_sql=true
hibernate.hbm2ddl.auto=update
hibernate.dialect=org.hibernate.dialect.MySQLDialect
hibernate.default_schema=stemsep_db

hibernate.c3p0.min_size=5
hibernate.c3p0.max_size=20
hibernate.c3p0.initialPoolSize=5
hibernate.c3p0.acquire_increment=1
hibernate.c3p0.timeout=1800
hibernate.c3p0.max_statements=150
hibernate.c3p0.idle_test_period=60
hibernate.c3p0.acquireRetryAttempts=1
hibernate.c3p0.acquireRetryDelay=250

upload.directory=uploads
stems.directory=stems
colab.api.url=http://localhost:5000
EOF
  ok "hibernate.properties yazıldı (gitignored)"
else
  ok "hibernate.properties zaten var"
fi

# ── 6. Build doğrulama ────────────────────────────────────────────────
say "İlk build (mvn package, 1-3 dk)..."
if mvn -q -DskipTests package 2>&1 | tail -3; then
  ok "Build başarılı: target/stemsep.war"
else
  err "Build başarısız. mvn -DskipTests package çıktısına bak."
  exit 3
fi

# ── 7. Bitiş ─────────────────────────────────────────────────────────
cat <<EOF

╔═══════════════════════════════════════════════════════════╗
║  ✅ Hazır! Geliştirmeye başlamak için:                    ║
║                                                           ║
║    cd $REPO_DIR                                           ║
║    ./dev.sh https://NGROK_URL.ngrok-free.dev              ║
║                                                           ║
║  Tarayıcı:  http://localhost:8090                         ║
║                                                           ║
║  ngrok URL'i Berkan'dan al (Kaggle notebook restart       ║
║  ettiğinde yeni URL gönderir). Sonraki çalıştırmalarda    ║
║  argümansız './dev.sh' yeterli (URL cache'lenir).         ║
╚═══════════════════════════════════════════════════════════╝
EOF
