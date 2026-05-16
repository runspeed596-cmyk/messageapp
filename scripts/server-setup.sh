#!/usr/bin/env bash
# ============================================================
# Kelasor — Server Initial Setup (Iranian VPS)
# ============================================================
# Usage:
#   ssh -p 3031 root@185.116.162.68
#   bash server-setup.sh
# ============================================================
set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

print_step()  { echo -e "\n${CYAN}═══════════════════════════════════════${NC}"; echo -e "${CYAN}  $1${NC}"; echo -e "${CYAN}═══════════════════════════════════════${NC}"; }
print_ok()    { echo -e "  ${GREEN}[OK]${NC} $1"; }
print_warn()  { echo -e "  ${YELLOW}[WARN]${NC} $1"; }
print_fail()  { echo -e "  ${RED}[FAIL]${NC} $1"; }

# ── Step 0: Check root ────────────────────────────────────
if [[ $EUID -ne 0 ]]; then
    print_fail "This script must be run as root"
    exit 1
fi

# ── Step 1: Configure Iranian APT mirrors ─────────────────
print_step "1/7  Configuring Iranian APT mirrors"

# Backup original sources.list
cp /etc/apt/sources.list /etc/apt/sources.list.bak 2>/dev/null || true

# Detect Ubuntu codename
CODENAME=$(lsb_release -cs 2>/dev/null || echo "jammy")

cat > /etc/apt/sources.list << APTEOF
# Iranian mirror for faster downloads inside Iran
deb http://mirror.arvancloud.ir/ubuntu ${CODENAME} main restricted universe multiverse
deb http://mirror.arvancloud.ir/ubuntu ${CODENAME}-updates main restricted universe multiverse
deb http://mirror.arvancloud.ir/ubuntu ${CODENAME}-security main restricted universe multiverse
deb http://mirror.arvancloud.ir/ubuntu ${CODENAME}-backports main restricted universe multiverse
APTEOF

apt update -y && apt upgrade -y
print_ok "APT mirrors configured (ArvanCloud)"

# ── Step 2: Install essential packages ─────────────────────
print_step "2/7  Installing essential packages"

apt install -y \
    curl wget unzip htop nano \
    ufw fail2ban \
    ca-certificates gnupg lsb-release \
    apt-transport-https software-properties-common

print_ok "Essential packages installed"

# ── Step 3: Install Docker ─────────────────────────────────
print_step "3/7  Installing Docker Engine"

if command -v docker &>/dev/null; then
    print_warn "Docker already installed: $(docker --version)"
else
    # Use ArvanCloud Docker mirror for installation
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg 2>/dev/null || {
        # Fallback: direct install script
        curl -fsSL https://get.docker.com -o get-docker.sh
        sh get-docker.sh
        rm -f get-docker.sh
    }

    if ! command -v docker &>/dev/null; then
        # Add repo manually with Iranian mirror fallback
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu ${CODENAME} stable" > /etc/apt/sources.list.d/docker.list
        apt update -y
        apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    fi
fi

systemctl enable docker
systemctl start docker
print_ok "Docker installed: $(docker --version)"

# ── Step 4: Configure Docker to use Iranian mirrors ────────
print_step "4/7  Configuring Docker Iranian Registry Mirrors"

mkdir -p /etc/docker

cat > /etc/docker/daemon.json << 'DAEMONEOF'
{
  "registry-mirrors": [
    "https://docker.arvancloud.ir",
    "https://registry.docker.ir"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
DAEMONEOF

systemctl daemon-reload
systemctl restart docker
print_ok "Docker Iranian mirrors configured"

# Verify Docker compose is available
if docker compose version &>/dev/null; then
    print_ok "Docker Compose: $(docker compose version --short)"
else
    print_warn "Docker Compose plugin not found, installing..."
    apt install -y docker-compose-plugin || {
        # Manual fallback
        COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep '"tag_name"' | head -1 | sed -E 's/.*"([^"]+)".*/\1/' || echo "v2.27.1")
        curl -SL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-$(uname -m)" -o /usr/local/bin/docker-compose
        chmod +x /usr/local/bin/docker-compose
    }
fi

# ── Step 5: Firewall (UFW) ────────────────────────────────
print_step "5/7  Configuring Firewall (UFW)"

# IMPORTANT: Do NOT use 'ufw --force reset' here — it drops
# established SSH connections and kills this script mid-run.
ufw default deny incoming
ufw default allow outgoing

# SSH on custom port 3031 (MUST be first!)
ufw allow 3031/tcp comment 'SSH'

# HTTP & HTTPS
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'

# Netty WebSocket
ufw allow 9090/tcp comment 'WebSocket'

# MinIO Console (optional, restrict to your IP later)
# ufw allow 9001/tcp comment 'MinIO Console'

ufw --force enable
print_ok "Firewall configured"
ufw status verbose

# ── Step 6: Fail2Ban ──────────────────────────────────────
print_step "6/7  Configuring Fail2Ban"

cat > /etc/fail2ban/jail.local << 'F2BEOF'
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 3
backend = auto

[sshd]
enabled = true
port = 3031
logpath = /var/log/auth.log
maxretry = 3
bantime = 7200
F2BEOF

systemctl enable fail2ban
systemctl restart fail2ban
print_ok "Fail2Ban configured (SSH on port 3031)"

# ── Step 7: Prepare project directory ─────────────────────
print_step "7/7  Preparing project directory"

mkdir -p /opt/messageapp
mkdir -p /opt/messageapp/nginx
mkdir -p /opt/messageapp/ssl

# Create initial .env if not exists
if [[ ! -f /opt/messageapp/.env ]]; then
    cat > /opt/messageapp/.env << 'ENVEOF'
# =============================================
# Kelasor Production Environment
# =============================================

# Server Configuration
APP_BASE_URL=http://185.116.162.68

# PostgreSQL Database
POSTGRES_DB=messageapp
POSTGRES_USER=postgres
POSTGRES_PASSWORD=K3las0r_Pr0d_2026!

# JWT Secret (256-bit secure key)
JWT_SECRET=2Qw1Qk5vQ2p6b3J4bGd2d2h5b2J6d3J4bGd2d2h5b2J6d3J4bGd2d2h5b2J6d3J4bGd2d2h5b2J6d3J4bGd2d2h5b2J6d3J4bGd2d2h5b2J6d3J4bGQ=

# MinIO Object Storage
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=Min10_K3las0r_2026!

# Najva SMS (for OTP verification)
NAJVA_SMS_API_KEY=YOUR_NAJVA_API_KEY_HERE
ENVEOF
    print_ok ".env file created at /opt/messageapp/.env"
    print_warn "EDIT the .env file: nano /opt/messageapp/.env"
else
    print_ok ".env already exists (preserved)"
fi

# ── Done ──────────────────────────────────────────────────
echo ""
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}  SERVER SETUP COMPLETE!${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo -e "  Next steps:"
echo -e "    1. Edit .env:  ${CYAN}nano /opt/messageapp/.env${NC}"
echo -e "    2. Upload code from Windows using deploy-server.ps1"
echo -e "    3. Run:        ${CYAN}cd /opt/messageapp && docker compose up -d --build${NC}"
echo ""
