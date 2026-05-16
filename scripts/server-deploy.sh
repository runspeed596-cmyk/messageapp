#!/usr/bin/env bash
# ============================================================
# Kelasor — Server Deploy / Update Script
# ============================================================
# This script runs ON THE SERVER after the ZIP is uploaded.
# Usage:
#   bash /opt/messageapp/scripts/server-deploy.sh
# ============================================================
set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

PROJECT_DIR="/opt/messageapp"
ZIP_PATH="/opt/messageapp-deploy.zip"
BACKUP_DIR="/opt/messageapp-backup-$(date +%Y%m%d_%H%M%S)"

print_step()  { echo -e "\n${CYAN}═══════════════════════════════════════${NC}"; echo -e "${CYAN}  $1${NC}"; echo -e "${CYAN}═══════════════════════════════════════${NC}"; }
print_ok()    { echo -e "  ${GREEN}[OK]${NC} $1"; }
print_warn()  { echo -e "  ${YELLOW}[WARN]${NC} $1"; }
print_fail()  { echo -e "  ${RED}[FAIL]${NC} $1"; }

# ── Pre-flight checks ─────────────────────────────────────
if [[ ! -f "$ZIP_PATH" ]]; then
    print_fail "ZIP file not found: $ZIP_PATH"
    echo "  Upload the deploy ZIP first using deploy-server.ps1"
    exit 1
fi

# ── Step 1: Backup current state ──────────────────────────
print_step "1/5  Backing up current deployment"

if [[ -d "$PROJECT_DIR/SpringBoot" ]] || [[ -d "$PROJECT_DIR/admin-panel" ]]; then
    mkdir -p "$BACKUP_DIR"
    cp -r "$PROJECT_DIR/docker-compose.yml" "$BACKUP_DIR/" 2>/dev/null || true
    cp -r "$PROJECT_DIR/.env" "$BACKUP_DIR/" 2>/dev/null || true
    print_ok "Backup created: $BACKUP_DIR"
else
    print_warn "No existing deployment to backup (first deploy)"
fi

# ── Step 2: Extract new code ─────────────────────────────
print_step "2/5  Extracting new deployment"

# Remove old code (preserve .env and ssl certs)
rm -rf "$PROJECT_DIR/SpringBoot" \
       "$PROJECT_DIR/admin-panel" \
       "$PROJECT_DIR/kelasor-online" \
       "$PROJECT_DIR/jitsi-config" \
       "$PROJECT_DIR/nginx" \
       "$PROJECT_DIR/scripts"
rm -f  "$PROJECT_DIR/docker-compose.yml" \
       "$PROJECT_DIR/.env.example" \
       "$PROJECT_DIR/.gitignore"

# Extract ZIP
unzip -o "$ZIP_PATH" -d "$PROJECT_DIR"
rm -f "$ZIP_PATH"

# Make scripts executable
chmod +x "$PROJECT_DIR/scripts/"*.sh 2>/dev/null || true

print_ok "Code extracted to $PROJECT_DIR"

# ── Step 3: Ensure .env exists ────────────────────────────
print_step "3/5  Checking environment configuration"

if [[ ! -f "$PROJECT_DIR/.env" ]]; then
    if [[ -f "$PROJECT_DIR/.env.example" ]]; then
        cp "$PROJECT_DIR/.env.example" "$PROJECT_DIR/.env"
        print_warn ".env created from .env.example — EDIT IT: nano $PROJECT_DIR/.env"
    else
        print_fail "No .env file found! Create one manually."
        exit 1
    fi
else
    print_ok ".env file preserved from previous deployment"
fi

# Ensure Jitsi JWT variables exist in .env
if ! grep -q "JITSI_JWT_SECRET" "$PROJECT_DIR/.env"; then
    echo "" >> "$PROJECT_DIR/.env"
    echo "# Jitsi Meet (Kelasor Online)" >> "$PROJECT_DIR/.env"
    echo "JITSI_JWT_APP_ID=kelasor-online" >> "$PROJECT_DIR/.env"
    echo "JITSI_JWT_SECRET=K3las0r_J1ts1_S3cr3t_2026!" >> "$PROJECT_DIR/.env"
    echo "JICOFO_AUTH_PASSWORD=focuspass" >> "$PROJECT_DIR/.env"
    echo "JVB_AUTH_PASSWORD=jvbpass" >> "$PROJECT_DIR/.env"
    echo "JIBRI_XMPP_PASSWORD=jibripass" >> "$PROJECT_DIR/.env"
    echo "JIGASI_XMPP_PASSWORD=jigasipass" >> "$PROJECT_DIR/.env"
    print_ok "Jitsi environment variables added to .env"
fi

# Open JVB port for WebRTC media
ufw allow 10000/udp 2>/dev/null || true

# ── Step 4: Build and start containers ────────────────────
print_step "4/5  Building and starting Docker containers"

cd "$PROJECT_DIR"

# Pull base images (uses Iranian mirrors from daemon.json)
echo "  Pulling base images..."
docker compose pull 2>/dev/null || print_warn "Some images couldn't be pulled (will build from cache)"

# Build and start
echo "  Building and starting services..."
docker compose up -d --build --remove-orphans

# Wait for containers to start
echo "  Waiting for services to start..."
sleep 10

print_ok "Docker containers started"

# ── Step 5: Health checks ─────────────────────────────────
print_step "5/5  Running health checks"

echo "  Checking container status..."
docker compose ps

echo ""
echo "  Testing API health endpoint..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/health 2>/dev/null || echo "000")

if [[ "$HTTP_CODE" == "200" ]]; then
    print_ok "API is healthy (HTTP $HTTP_CODE)"
else
    print_warn "API returned HTTP $HTTP_CODE (may still be starting)"
    echo "  Check logs: docker compose logs -f api"
fi

echo ""
echo "  Testing Admin Panel..."
ADMIN_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/ca978112ca/ 2>/dev/null || echo "000")

if [[ "$ADMIN_CODE" == "200" ]]; then
    print_ok "Admin Panel is accessible (HTTP $ADMIN_CODE)"
else
    print_warn "Admin Panel returned HTTP $ADMIN_CODE"
fi

echo ""
echo "  Testing Kelasor Online Portal..."
ONLINE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "Host: online.kelasorapp.ir" http://localhost/ 2>/dev/null || echo "000")

if [[ "$ONLINE_CODE" == "200" ]]; then
    print_ok "Kelasor Online is accessible (HTTP $ONLINE_CODE)"
else
    print_warn "Kelasor Online returned HTTP $ONLINE_CODE (DNS may not be set up yet)"
fi

# ── Done ──────────────────────────────────────────────────
echo ""
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}  DEPLOYMENT COMPLETE!${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo ""
echo -e "  Access Points:"
echo -e "    API:          ${CYAN}http://185.116.162.68/api/${NC}"
echo -e "    Admin Panel:  ${CYAN}http://185.116.162.68/ca978112ca/${NC}"
echo -e "    Kelasor Online: ${CYAN}http://online.kelasorapp.ir/${NC}"
echo -e "    WebSocket:    ${CYAN}ws://185.116.162.68/ws/${NC}"
echo -e "    Swagger:      ${CYAN}http://185.116.162.68/swagger-ui.html${NC}"
echo ""
echo -e "  Useful commands:"
echo -e "    Logs:         ${CYAN}docker compose logs -f${NC}"
echo -e "    API logs:     ${CYAN}docker compose logs -f api${NC}"
echo -e "    Restart:      ${CYAN}docker compose restart${NC}"
echo -e "    Status:       ${CYAN}docker compose ps${NC}"
echo -e "    DB Backup:    ${CYAN}docker compose exec postgres pg_dump -U postgres messageapp > backup.sql${NC}"
echo ""
