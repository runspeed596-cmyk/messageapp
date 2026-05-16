#!/usr/bin/env bash
# ============================================================
# Kelasor — FULL SERVER BACKUP (v2 - Iran-safe)
# ============================================================
# Does NOT require internet access. Uses only local containers.
#
# Run ON the server:
#   bash /opt/server-backup.sh
#
# Output: /opt/kelasor-full-backup-TIMESTAMP.tar.gz
# ============================================================
set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/kelasor-backup-${TIMESTAMP}"
ARCHIVE_NAME="kelasor-full-backup-${TIMESTAMP}.tar.gz"
PROJECT_DIR="/opt/messageapp"

print_step()  { echo -e "\n${CYAN}=========================================${NC}"; echo -e "${CYAN}  $1${NC}"; echo -e "${CYAN}=========================================${NC}"; }
print_ok()    { echo -e "  ${GREEN}[OK]${NC} $1"; }
print_warn()  { echo -e "  ${YELLOW}[WARN]${NC} $1"; }
print_fail()  { echo -e "  ${RED}[FAIL]${NC} $1"; }

echo ""
echo -e "${CYAN}=================================================${NC}"
echo -e "${CYAN}  Kelasor Full Server Backup${NC}"
echo -e "${CYAN}  Timestamp: ${TIMESTAMP}${NC}"
echo -e "${CYAN}=================================================${NC}"

# Create backup directory
mkdir -p "$BACKUP_DIR"

# ── Step 1: Backup PostgreSQL Database ────────────────────
print_step "1/6  Backing up PostgreSQL Database"

cd "$PROJECT_DIR"

# Try pg_dump directly - don't bother checking if running, just try it
echo "  Attempting database dump..."
if docker compose exec -T postgres pg_dump -U postgres -Fc --no-owner --no-acl messageapp > "${BACKUP_DIR}/database.dump" 2>/dev/null; then
    DB_SIZE=$(du -sh "${BACKUP_DIR}/database.dump" | cut -f1)
    print_ok "Binary dump created (${DB_SIZE})"
else
    print_warn "Binary dump failed, trying SQL dump..."
    rm -f "${BACKUP_DIR}/database.dump"
fi

# Also try plain SQL dump
if docker compose exec -T postgres pg_dump -U postgres --no-owner --no-acl messageapp > "${BACKUP_DIR}/database.sql" 2>/dev/null; then
    SQL_SIZE=$(du -sh "${BACKUP_DIR}/database.sql" | cut -f1)
    # Check if file is not empty
    if [ -s "${BACKUP_DIR}/database.sql" ]; then
        print_ok "SQL dump created (${SQL_SIZE})"
    else
        rm -f "${BACKUP_DIR}/database.sql"
        print_warn "SQL dump was empty"
    fi
else
    rm -f "${BACKUP_DIR}/database.sql"
    print_warn "SQL dump also failed"
fi

# ── Step 2: Backup Docker Volumes (direct copy) ──────────
print_step "2/6  Backing up Docker Volumes"

mkdir -p "${BACKUP_DIR}/volumes"

# Find actual volume paths on disk (no internet needed!)
echo "  Finding Docker volume paths..."
docker volume ls -q | while read vol; do
    echo "  Found volume: $vol"
done

# Copy volumes using the API container (already running, no pull needed)
# Method: use docker cp from the containers that already mount these volumes

# Postgres data - copy directly from running container
echo "  Backing up postgres data..."
if docker compose cp postgres:/var/lib/postgresql/data "${BACKUP_DIR}/volumes/postgres_data" 2>/dev/null; then
    PG_SIZE=$(du -sh "${BACKUP_DIR}/volumes/postgres_data" 2>/dev/null | cut -f1)
    print_ok "postgres_data (${PG_SIZE})"
else
    # Fallback: copy from Docker volume mount point on host
    PG_VOL_PATH=$(docker volume inspect $(docker volume ls -q | grep postgres_data | head -1) --format '{{.Mountpoint}}' 2>/dev/null || echo "")
    if [ -n "$PG_VOL_PATH" ] && [ -d "$PG_VOL_PATH" ]; then
        cp -r "$PG_VOL_PATH" "${BACKUP_DIR}/volumes/postgres_data"
        PG_SIZE=$(du -sh "${BACKUP_DIR}/volumes/postgres_data" 2>/dev/null | cut -f1)
        print_ok "postgres_data via host path (${PG_SIZE})"
    else
        print_warn "Could not backup postgres_data"
    fi
fi

# Uploads data - copy from API container
echo "  Backing up uploads data..."
if docker compose cp api:/app/uploads "${BACKUP_DIR}/volumes/uploads_data" 2>/dev/null; then
    UP_SIZE=$(du -sh "${BACKUP_DIR}/volumes/uploads_data" 2>/dev/null | cut -f1)
    print_ok "uploads_data (${UP_SIZE})"
else
    UP_VOL_PATH=$(docker volume inspect $(docker volume ls -q | grep uploads_data | head -1) --format '{{.Mountpoint}}' 2>/dev/null || echo "")
    if [ -n "$UP_VOL_PATH" ] && [ -d "$UP_VOL_PATH" ]; then
        cp -r "$UP_VOL_PATH" "${BACKUP_DIR}/volumes/uploads_data"
        UP_SIZE=$(du -sh "${BACKUP_DIR}/volumes/uploads_data" 2>/dev/null | cut -f1)
        print_ok "uploads_data via host path (${UP_SIZE})"
    else
        print_warn "Could not backup uploads_data"
    fi
fi

# MinIO data - copy from host volume path
echo "  Backing up minio data..."
MINIO_VOL_PATH=$(docker volume inspect $(docker volume ls -q | grep minio_data | head -1) --format '{{.Mountpoint}}' 2>/dev/null || echo "")
if [ -n "$MINIO_VOL_PATH" ] && [ -d "$MINIO_VOL_PATH" ]; then
    cp -r "$MINIO_VOL_PATH" "${BACKUP_DIR}/volumes/minio_data"
    MN_SIZE=$(du -sh "${BACKUP_DIR}/volumes/minio_data" 2>/dev/null | cut -f1)
    print_ok "minio_data (${MN_SIZE})"
else
    print_warn "Could not backup minio_data"
fi

# Redis data - copy from host volume path
echo "  Backing up redis data..."
REDIS_VOL_PATH=$(docker volume inspect $(docker volume ls -q | grep redis_data | head -1) --format '{{.Mountpoint}}' 2>/dev/null || echo "")
if [ -n "$REDIS_VOL_PATH" ] && [ -d "$REDIS_VOL_PATH" ]; then
    cp -r "$REDIS_VOL_PATH" "${BACKUP_DIR}/volumes/redis_data"
    RD_SIZE=$(du -sh "${BACKUP_DIR}/volumes/redis_data" 2>/dev/null | cut -f1)
    print_ok "redis_data (${RD_SIZE})"
else
    print_warn "Could not backup redis_data (not critical)"
fi

# ── Step 3: Backup Configuration Files ────────────────────
print_step "3/6  Backing up Configuration Files"

mkdir -p "${BACKUP_DIR}/config"

if [[ -f "${PROJECT_DIR}/.env" ]]; then
    cp "${PROJECT_DIR}/.env" "${BACKUP_DIR}/config/.env"
    print_ok ".env file"
else
    print_warn ".env file not found"
fi

if [[ -f "${PROJECT_DIR}/docker-compose.yml" ]]; then
    cp "${PROJECT_DIR}/docker-compose.yml" "${BACKUP_DIR}/config/docker-compose.yml"
    print_ok "docker-compose.yml"
fi

if [[ -d "${PROJECT_DIR}/nginx" ]]; then
    cp -r "${PROJECT_DIR}/nginx" "${BACKUP_DIR}/config/nginx"
    print_ok "nginx configs"
fi

# ── Step 4: Backup SSL Certificates ──────────────────────
print_step "4/6  Backing up SSL Certificates"

if [[ -d "${PROJECT_DIR}/ssl" ]]; then
    cp -r "${PROJECT_DIR}/ssl" "${BACKUP_DIR}/ssl"
    print_ok "SSL certificates backed up"
else
    print_warn "No SSL directory found at ${PROJECT_DIR}/ssl"
fi

if [[ -d "/etc/ssl/server" ]]; then
    mkdir -p "${BACKUP_DIR}/ssl-system"
    cp -r /etc/ssl/server/* "${BACKUP_DIR}/ssl-system/" 2>/dev/null || true
    print_ok "System SSL certificates backed up"
fi

# ── Step 5: Backup SSH Configuration ─────────────────────
print_step "5/6  Backing up SSH Configuration"

mkdir -p "${BACKUP_DIR}/ssh"
if [[ -f /root/.ssh/authorized_keys ]]; then
    cp /root/.ssh/authorized_keys "${BACKUP_DIR}/ssh/authorized_keys"
    print_ok "SSH authorized_keys"
fi
grep -E "^Port " /etc/ssh/sshd_config > "${BACKUP_DIR}/ssh/sshd_port.txt" 2>/dev/null || echo "Port 3031" > "${BACKUP_DIR}/ssh/sshd_port.txt"
print_ok "SSH port configuration"

# ── Step 6: Create Final Archive ─────────────────────────
print_step "6/6  Creating Final Archive"

# Show what we have
echo "  Contents of backup:"
du -sh "${BACKUP_DIR}"/* 2>/dev/null || true
echo ""

# Create archive
cd /opt
tar czf "/opt/${ARCHIVE_NAME}" -C "$BACKUP_DIR" .

ARCHIVE_SIZE=$(du -sh "/opt/${ARCHIVE_NAME}" | cut -f1)

# Cleanup temp directory
rm -rf "$BACKUP_DIR"

echo ""
echo -e "${GREEN}=================================================${NC}"
echo -e "${GREEN}  BACKUP COMPLETE!${NC}"
echo -e "${GREEN}=================================================${NC}"
echo ""
echo -e "  Archive: ${CYAN}/opt/${ARCHIVE_NAME}${NC}"
echo -e "  Size:    ${CYAN}${ARCHIVE_SIZE}${NC}"
echo ""
echo -e "  ${YELLOW}IMPORTANT: Download this file to your local machine!${NC}"
echo -e "  From Windows PowerShell run:"
echo -e "  ${CYAN}scp -P 3031 root@185.116.162.68:/opt/${ARCHIVE_NAME} .${NC}"
echo ""
