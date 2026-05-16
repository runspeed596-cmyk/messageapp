#!/usr/bin/env bash
# ============================================================
# Kelasor — FULL SERVER RESTORE
# ============================================================
# This script restores a COMPLETE backup after OS reinstall.
# 
# Prerequisites:
#   1. Fresh Ubuntu 22.04 installed
#   2. SSH configured on port 3031
#   3. Backup file uploaded to /opt/
#
# Usage:
#   bash /opt/server-restore.sh /opt/kelasor-full-backup-XXXXXXXX.tar.gz
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

# Check arguments
BACKUP_FILE="${1:-}"
if [[ -z "$BACKUP_FILE" ]] || [[ ! -f "$BACKUP_FILE" ]]; then
    echo -e "${RED}Usage: bash server-restore.sh /path/to/kelasor-full-backup-XXXXXXXX.tar.gz${NC}"
    exit 1
fi

echo ""
echo -e "${CYAN}═══════════════════════════════════════════════${NC}"
echo -e "${CYAN}  🔄 Kelasor Full Server Restore${NC}"
echo -e "${CYAN}  Backup: ${BACKUP_FILE}${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════${NC}"

RESTORE_DIR="/opt/kelasor-restore-tmp"
PROJECT_DIR="/opt/messageapp"

# ── Step 1: Extract Backup ────────────────────────────────
print_step "1/7  Extracting Backup"

mkdir -p "$RESTORE_DIR"
tar xzf "$BACKUP_FILE" -C "$RESTORE_DIR"
print_ok "Backup extracted"

# Show manifest
if [[ -f "${RESTORE_DIR}/MANIFEST.txt" ]]; then
    echo ""
    cat "${RESTORE_DIR}/MANIFEST.txt"
    echo ""
fi

# ── Step 2: Install Docker (if needed) ────────────────────
print_step "2/7  Ensuring Docker is Installed"

if ! command -v docker &>/dev/null; then
    echo "  Installing Docker..."
    
    # Configure Iranian APT mirrors
    CODENAME=$(lsb_release -cs 2>/dev/null || echo "jammy")
    cat > /etc/apt/sources.list << APTEOF
deb http://mirror.arvancloud.ir/ubuntu ${CODENAME} main restricted universe multiverse
deb http://mirror.arvancloud.ir/ubuntu ${CODENAME}-updates main restricted universe multiverse
deb http://mirror.arvancloud.ir/ubuntu ${CODENAME}-security main restricted universe multiverse
APTEOF
    
    apt update -y
    apt install -y curl wget unzip ca-certificates gnupg lsb-release apt-transport-https
    
    # Install Docker
    curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
    sh /tmp/get-docker.sh
    rm -f /tmp/get-docker.sh
    
    # Configure Docker Iranian mirrors
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
    systemctl enable docker
    systemctl restart docker
    print_ok "Docker installed and configured"
else
    print_ok "Docker already installed: $(docker --version)"
fi

# Verify docker compose
if ! docker compose version &>/dev/null; then
    apt install -y docker-compose-plugin
fi
print_ok "Docker Compose: $(docker compose version --short 2>/dev/null || echo 'installed')"

# ── Step 3: Restore Configuration ─────────────────────────
print_step "3/7  Restoring Configuration Files"

mkdir -p "$PROJECT_DIR"
mkdir -p "$PROJECT_DIR/nginx"
mkdir -p "$PROJECT_DIR/ssl"

# .env file (CRITICAL!)
if [[ -f "${RESTORE_DIR}/config/.env" ]]; then
    cp "${RESTORE_DIR}/config/.env" "${PROJECT_DIR}/.env"
    print_ok ".env restored"
else
    print_fail ".env not found in backup!"
fi

# nginx configs
if [[ -d "${RESTORE_DIR}/config/nginx" ]]; then
    cp -r "${RESTORE_DIR}/config/nginx/"* "${PROJECT_DIR}/nginx/"
    print_ok "nginx configs restored"
fi

# docker-compose.yml
if [[ -f "${RESTORE_DIR}/config/docker-compose.yml" ]]; then
    cp "${RESTORE_DIR}/config/docker-compose.yml" "${PROJECT_DIR}/docker-compose.yml"
    print_ok "docker-compose.yml restored"
fi

# ── Step 4: Restore SSL Certificates ─────────────────────
print_step "4/7  Restoring SSL Certificates"

if [[ -d "${RESTORE_DIR}/ssl" ]]; then
    cp -r "${RESTORE_DIR}/ssl/"* "${PROJECT_DIR}/ssl/" 2>/dev/null || true
    print_ok "SSL certificates restored to project"
fi

if [[ -d "${RESTORE_DIR}/ssl-system" ]]; then
    mkdir -p /etc/ssl/server
    cp -r "${RESTORE_DIR}/ssl-system/"* /etc/ssl/server/ 2>/dev/null || true
    print_ok "System SSL certificates restored"
fi

# ── Step 5: Restore SSH Configuration ─────────────────────
print_step "5/7  Restoring SSH Configuration"

if [[ -f "${RESTORE_DIR}/ssh/authorized_keys" ]]; then
    mkdir -p /root/.ssh
    chmod 700 /root/.ssh
    # Merge keys (don't overwrite existing)
    cat "${RESTORE_DIR}/ssh/authorized_keys" >> /root/.ssh/authorized_keys
    sort -u /root/.ssh/authorized_keys -o /root/.ssh/authorized_keys
    chmod 600 /root/.ssh/authorized_keys
    print_ok "SSH authorized_keys restored"
fi

# Ensure SSH port is 3031
if ! grep -q "^Port 3031" /etc/ssh/sshd_config; then
    echo "Port 3031" >> /etc/ssh/sshd_config
    systemctl restart sshd
    print_ok "SSH port set to 3031"
else
    print_ok "SSH port already 3031"
fi

# ── Step 6: Deploy Application ────────────────────────────
print_step "6/7  Deploying Application"

echo "  You need to deploy the latest code using deploy-server.ps1 from Windows."
echo "  Run from your Windows machine:"
echo -e "  ${CYAN}.\\deploy-server.ps1${NC}"
echo ""
print_warn "Skipping app deployment (run deploy-server.ps1 after restore)"

# ── Step 7: Restore Database ─────────────────────────────
print_step "7/7  Restoring Database"

if [[ -f "${RESTORE_DIR}/database.dump" ]] || [[ -f "${RESTORE_DIR}/database.sql" ]]; then
    echo "  Database backup found. To restore it:"
    echo ""
    echo "  AFTER running deploy-server.ps1 and containers are up:"
    echo ""
    
    if [[ -f "${RESTORE_DIR}/database.dump" ]]; then
        # Copy dump to project dir for easy access
        cp "${RESTORE_DIR}/database.dump" "${PROJECT_DIR}/database.dump"
        echo -e "  ${CYAN}# Option 1: Binary restore (recommended)${NC}"
        echo -e "  ${CYAN}cd /opt/messageapp${NC}"
        echo -e "  ${CYAN}cat database.dump | docker compose exec -T postgres pg_restore -U postgres -d messageapp --no-owner --clean --if-exists${NC}"
    fi
    
    if [[ -f "${RESTORE_DIR}/database.sql" ]]; then
        cp "${RESTORE_DIR}/database.sql" "${PROJECT_DIR}/database.sql"
        echo ""
        echo -e "  ${CYAN}# Option 2: SQL restore (fallback)${NC}"
        echo -e "  ${CYAN}cd /opt/messageapp${NC}"
        echo -e "  ${CYAN}cat database.sql | docker compose exec -T postgres psql -U postgres messageapp${NC}"
    fi
    
    echo ""
    print_ok "Database files copied to ${PROJECT_DIR}/"
else
    print_warn "No database backup found"
fi

# Cleanup
rm -rf "$RESTORE_DIR"

# ── Firewall ──────────────────────────────────────────────
echo ""
echo "  Setting up firewall..."
ufw default deny incoming 2>/dev/null || true
ufw default allow outgoing 2>/dev/null || true
ufw allow 3031/tcp 2>/dev/null || true
ufw allow 80/tcp 2>/dev/null || true
ufw allow 443/tcp 2>/dev/null || true
ufw allow 9090/tcp 2>/dev/null || true
# BBB ports (if installing BBB on this server)
ufw allow 16384:32768/udp 2>/dev/null || true
ufw --force enable 2>/dev/null || true
print_ok "Firewall configured"

# ── Done ──────────────────────────────────────────────────
echo ""
echo -e "${GREEN}═══════════════════════════════════════════════${NC}"
echo -e "${GREEN}  ✅ RESTORE COMPLETE!${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════${NC}"
echo ""
echo -e "  ${YELLOW}⚠️  NEXT STEPS (IN ORDER):${NC}"
echo ""
echo -e "  ${CYAN}1. From Windows, deploy the latest code:${NC}"
echo -e "     .\\deploy-server.ps1"
echo ""
echo -e "  ${CYAN}2. Wait for containers to start, then restore database:${NC}"
echo -e "     cd /opt/messageapp"
echo -e "     cat database.dump | docker compose exec -T postgres pg_restore -U postgres -d messageapp --no-owner --clean --if-exists"
echo ""
echo -e "  ${CYAN}3. Verify everything works:${NC}"
echo -e "     curl http://localhost/api/health"
echo -e "     docker compose ps"
echo ""
echo -e "  ${CYAN}4. (Optional) Install BBB if OS is Ubuntu 22.04:${NC}"
echo -e "     See DEPLOYMENT.md for BBB installation guide"
echo ""
