# Kelasor Deployment Guide (راهنمای دیپلوی کلاسور)

Deploy API + Admin Panel on **185.116.162.68** (Iranian VPS)

> [!IMPORTANT]
> SSH Port: **3031** | Server IP: **185.116.162.68**
> چون سرور در ایران است، تمام مخازن از **میرور داخلی ایران** استفاده می‌شوند.

---

## Architecture (معماری)

```
Internet / App → 185.116.162.68
                        │
                  ┌─────▼──────┐
                  │   Nginx    │ :80 (HTTP)
                  │            │
                  │  /api/* ───▶ Spring Boot :8080
                  │  /ws/*  ───▶ WebSocket (STOMP)
                  │  /ca978112ca/ ──▶ Admin Panel
                  │  /uploads/* ──▶ Files
                  │  / ────────▶ Placeholder
                  └─────┬──────┘
                        │
                  ┌─────▼──────┐
                  │ PostgreSQL │ (internal)
                  │ Redis      │ (internal)
                  │ MinIO      │ (internal)
                  └────────────┘
```

---

## Quick Deploy (دیپلوی سریع)

### From Windows (PowerShell):

```powershell
cd E:\Learn\programming\ponisha\MessageApp2
.\deploy-server.ps1
```

This single command will:
1. Build the Spring Boot JAR locally
2. Build the Admin Panel (npm run build) locally
3. Create a minimal ZIP (~50-80MB)
4. Upload via SCP (port 3031) to server
5. Extract and rebuild Docker containers on server

### Options:

```powershell
.\deploy-server.ps1 -SkipBuild    # Deploy without rebuilding JAR/Admin
.\deploy-server.ps1 -SkipAdmin    # Skip admin panel rebuild only
```

---

## First-Time Server Setup (راه‌اندازی اولیه)

### 0. Connect to Server

```bash
ssh -p 3031 root@185.116.162.68
# Password: (enter the server password)
```

### 1. Upload & Run Setup Script

From Windows PowerShell:
```powershell
scp -P 3031 "E:\Learn\programming\ponisha\MessageApp2\scripts\server-setup.sh" root@185.116.162.68:/opt/
ssh -p 3031 root@185.116.162.68 "chmod +x /opt/server-setup.sh && bash /opt/server-setup.sh"
```

This script will automatically:
- Configure Iranian APT mirrors (ArvanCloud)
- Install Docker + Docker Compose
- Configure Docker to use Iranian registry mirrors
- Setup UFW Firewall (ports 3031, 80, 443, 9090)
- Configure Fail2Ban (SSH protection on port 3031)
- Create project directory `/opt/messageapp`
- Generate initial `.env` file

### 2. Configure Environment

```bash
nano /opt/messageapp/.env
```

Update these values:
```env
APP_BASE_URL=http://185.116.162.68
POSTGRES_PASSWORD=YOUR_STRONG_PASSWORD
JWT_SECRET=YOUR_JWT_SECRET
MINIO_ROOT_PASSWORD=YOUR_MINIO_PASSWORD
NAJVA_SMS_API_KEY=YOUR_REAL_API_KEY
```

Generate secure secrets:
```bash
openssl rand -base64 32   # for passwords
openssl rand -base64 64   # for JWT secret
```

### 3. Deploy the Application

From Windows PowerShell:
```powershell
cd E:\Learn\programming\ponisha\MessageApp2
.\deploy-server.ps1
```

### 4. Verify Deployment

```bash
# From server:
docker compose ps                     # Check container status
curl http://localhost/api/health       # Test API
curl http://localhost/ca978112ca/      # Test Admin Panel

# From anywhere:
curl http://185.116.162.68/api/health
```

---

## Access Points (نقاط دسترسی)

| Service       | URL                                          |
|---------------|----------------------------------------------|
| Admin Panel   | http://185.116.162.68/ca978112ca/            |
| API           | http://185.116.162.68/api/                   |
| WebSocket     | ws://185.116.162.68/ws/                      |
| Swagger       | http://185.116.162.68/swagger-ui.html        |
| Health Check  | http://185.116.162.68/health                 |

---

## Android App Configuration

The following files have been updated for production:

| File | Value |
|------|-------|
| `Constants.kt` | `BASE_URL = "http://185.116.162.68/"` |
| `WebSocketManager.kt` | `WS_BASE_URL = "ws://185.116.162.68/ws"` |
| `adminApi.ts` | `BASE_URL = "http://185.116.162.68/api"` |

---

## Update Deployment (بروزرسانی)

### Auto-Deploy (Recommended):

```powershell
cd E:\Learn\programming\ponisha\MessageApp2
.\deploy-server.ps1
```

### Manual Deploy:

```powershell
# Build JAR
cd SpringBoot && .\gradlew.bat bootJar --no-daemon -q && cd ..

# Build Admin Panel
cd admin-panel && $env:VITE_API_URL="http://185.116.162.68/api"; npm run build && cd ..

# ZIP deploy
$TEMP = "$env:TEMP\messageapp-deploy"
# ... (use deploy-server.ps1 instead)

# Upload
scp -P 3031 deploy.zip root@185.116.162.68:/opt/
```

### On Server:

```bash
cd /opt/messageapp
docker compose down
# Update files...
docker compose up -d --build
```

---

## Management Commands (دستورات مدیریت)

```bash
# SSH to server
ssh -p 3031 root@185.116.162.68

# Container management
cd /opt/messageapp
docker compose logs -f              # All logs
docker compose logs -f api          # API logs only
docker compose logs -f nginx        # Nginx logs
docker compose restart              # Restart all
docker compose restart api          # Restart API only
docker compose ps                   # Status
docker compose down                 # Stop all
docker compose up -d --build        # Rebuild & start

# Database management
docker compose exec postgres pg_dump -U postgres messageapp > backup_$(date +%Y%m%d).sql
cat backup.sql | docker compose exec -T postgres psql -U postgres -d messageapp

# Docker cleanup
docker system prune -af --volumes   # ⚠️ Removes unused images/volumes
```

---

## Troubleshooting (عیب‌یابی)

### API Not Starting

```bash
docker compose logs -f api
# Common issues:
# - PostgreSQL not ready yet → wait & retry
# - Port 8080 already in use → check with: netstat -tlnp | grep 8080
```

### Can't Pull Docker Images

```bash
# Verify Iranian mirror is configured
cat /etc/docker/daemon.json
# Should contain: "https://docker.arvancloud.ir"

# Restart Docker
systemctl restart docker
```

### Admin Panel Shows Blank Page

```bash
# Check if admin-panel dist was built
docker compose exec admin ls /usr/share/nginx/html/ca978112ca/
# Should show: index.html, assets/, etc.

# Rebuild admin
docker compose build admin
docker compose up -d admin
```

### WebSocket Connection Fails

```bash
# Check if port 9090 is open
ufw status | grep 9090

# Check API WebSocket logs
docker compose logs api | grep -i websocket
```

---

## SSL Setup (When Adding Domain)

When you configure `kelasorapp.ir` to point to this server:

1. Update DNS (A record → 185.116.162.68)
2. Update `.env`: `APP_BASE_URL=https://kelasorapp.ir`
3. Swap nginx config to include SSL
4. Redeploy

---

## File Structure (ساختار فایل‌ها)

```
/opt/messageapp/                  ← Server directory
├── .env                          ← Environment variables (NOT in Git)
├── docker-compose.yml            ← Docker orchestration
├── SpringBoot/                   ← Backend (pre-built JAR)
│   ├── Dockerfile
│   └── build/libs/*.jar
├── admin-panel/                  ← Frontend (pre-built dist/)
│   ├── Dockerfile
│   ├── nginx.conf
│   └── dist/
├── nginx/                        ← Reverse proxy configs
│   ├── nginx.conf
│   └── nginx-http-only.conf
├── scripts/                      ← Server scripts
│   ├── server-setup.sh
│   ├── server-deploy.sh
│   └── daemon.json
└── ssl/                          ← SSL certificates (future)
```
