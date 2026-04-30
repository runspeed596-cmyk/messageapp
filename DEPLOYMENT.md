# MessageApp Deployment Guide

Deploy API + Admin Panel on **194.5.175.30** with domain **kelasorapp.ir**

---

## 0. DNS Setup (Domain Provider)

Set these DNS records at your domain registrar (nic.ir → Cloudflare):

| Type | Name | Value          |
|------|------|----------------|
| A    | @    | 194.5.175.30   |
| A    | www  | 194.5.175.30   |

---

## 1. Server Setup

### 1.1 Connect & Update

```bash
ssh root@194.5.175.30
apt update && apt upgrade -y
```

### 1.2 Install Docker

```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
systemctl enable docker && systemctl start docker
docker --version && docker compose version
```

### 1.3 Install Utilities

```bash
apt install unzip ufw fail2ban -y
```

---

## 2. Security

### 2.1 Firewall

```bash
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
ufw status
```

### 2.2 Fail2Ban

```bash
cat > /etc/fail2ban/jail.local << 'EOF'
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 3

[sshd]
enabled = true
port = 22
logpath = /var/log/auth.log
maxretry = 3
EOF

systemctl enable fail2ban && systemctl restart fail2ban
```

---

## 3. Deploy Code (ZIP + SCP)

> GitHub is blocked on Iranian servers. Use ZIP archive method.

### 3.1 Archive (Windows PowerShell)

```powershell
cd E:\Learn\programming\ponisha\MessageApp2
Compress-Archive -Path "SpringBoot","admin-panel","nginx","docker-compose.yml",".env.example",".gitignore" -DestinationPath "$env:USERPROFILE\Desktop\messageapp.zip" -Force
```

### 3.2 Upload to Server (Windows PowerShell)

```powershell
scp "$env:USERPROFILE\Desktop\messageapp.zip" root@194.5.175.30:/opt/
```

### 3.3 Extract on Server

```bash
cd /opt
mkdir -p messageapp
unzip messageapp.zip -d messageapp
cd messageapp
```

### 3.4 Configure Environment

```bash
cp .env.example .env
nano .env
```

Set these values:
```env
APP_BASE_URL=https://kelasorapp.ir
POSTGRES_PASSWORD=YOUR_STRONG_PASSWORD
JWT_SECRET=YOUR_JWT_SECRET
```

Generate secrets:
```bash
openssl rand -base64 32   # for password
openssl rand -base64 64   # for JWT secret
```

---

## 4. First Deploy (HTTP Only)

Use HTTP-only nginx config first to get SSL certificate:

```bash
cd /opt/messageapp
cp nginx/nginx-http-only.conf nginx/nginx.conf
docker compose up -d --build
docker compose ps
```

### Verify

```bash
curl http://194.5.175.30/api/health
curl http://194.5.175.30/ca978112ca/
```

---

## 5. SSL Certificate

### Option A: Cloudflare Proxy (Recommended)

If using Cloudflare with **Proxied** (orange cloud ☁️):
- SSL is handled by Cloudflare automatically
- Keep `nginx-http-only.conf` — no need for Certbot
- In Cloudflare: SSL/TLS → **Full (Strict)**

### Option B: Let's Encrypt (without Cloudflare proxy)

```bash
docker compose stop nginx

docker run --rm -p 80:80 -p 443:443 \
  -v messageapp_certbot_etc:/etc/letsencrypt \
  -v messageapp_certbot_var:/var/lib/letsencrypt \
  certbot/certbot certonly \
  --standalone \
  -d kelasorapp.ir \
  -d www.kelasorapp.ir \
  --email your@email.com \
  --agree-tos --no-eff-email

# Switch to SSL nginx config
cp nginx/nginx-ssl.conf nginx/nginx.conf
docker compose up -d
```

Auto-renewal cron:
```bash
crontab -e
# Add:
0 3 * * * cd /opt/messageapp && docker compose run --rm certbot renew && docker compose exec nginx nginx -s reload
```

---

## 6. Update Deployment

### On Windows:

```powershell
cd E:\Learn\programming\ponisha\MessageApp2
Remove-Item "$env:USERPROFILE\Desktop\messageapp.zip" -ErrorAction SilentlyContinue
Compress-Archive -Path "SpringBoot","admin-panel","nginx","docker-compose.yml",".env.example",".gitignore" -DestinationPath "$env:USERPROFILE\Desktop\messageapp.zip" -Force
scp "$env:USERPROFILE\Desktop\messageapp.zip" root@194.5.175.30:/opt/
```

### On Server:

```bash
cd /opt
rm -rf messageapp/SpringBoot messageapp/admin-panel messageapp/nginx messageapp/docker-compose.yml
unzip -o messageapp.zip -d messageapp
cd messageapp
docker compose up -d --build
```

> `.env` is preserved since it's not in the zip.

---

## 7. Access Points

| Service       | URL                                     |
|---------------|-----------------------------------------|
| Admin Panel   | https://kelasorapp.ir/ca978112ca/       |
| API           | https://kelasorapp.ir/api/              |
| WebSocket     | wss://kelasorapp.ir/ws/                 |
| Swagger       | https://kelasorapp.ir/swagger-ui.html   |

---

## 8. Android App URLs

Update these files before building APK:

| File | Value |
|------|-------|
| `Constants.kt` | `BASE_URL = "https://kelasorapp.ir/"` |
| `NetworkModule.kt` | `BASE_URL = "https://kelasorapp.ir/"` |
| `WebSocketManager.kt` | `WS_BASE_URL = "wss://kelasorapp.ir/ws"` |
| `StoryMappers.kt` | `BASE_URL = "https://kelasorapp.ir"` |

---

## 9. Management Commands

```bash
docker compose logs -f              # All logs
docker compose logs -f api           # API logs
docker compose restart               # Restart all
docker compose ps                    # Status

# Database backup
docker compose exec postgres pg_dump -U postgres messageapp > backup_$(date +%Y%m%d).sql

# Database restore
cat backup.sql | docker compose exec -T postgres psql -U postgres -d messageapp
```

---

## Architecture

```
Internet → Cloudflare (SSL) → 194.5.175.30
                                    │
                              ┌─────▼──────┐
                              │   Nginx    │ :80/:443
                              │            │
                              │  /api/* ───▶ Spring Boot :8080
                              │  /ws/*  ───▶ WebSocket
                              │  /ca978112ca/ ──▶ Admin Panel
                              │  /uploads/* ──▶ Files
                              │  / ────────▶ Other Website
                              └─────┬──────┘
                                    │
                              ┌─────▼──────┐
                              │ PostgreSQL │ (internal)
                              └────────────┘
```
