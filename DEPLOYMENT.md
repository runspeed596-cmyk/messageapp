# MessageApp Deployment Guide

Complete guide to deploy the MessageApp API and Admin Panel on Ubuntu 22.04 with Docker.

## Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [Server Setup](#2-server-setup)
3. [Project Deployment](#3-project-deployment)
4. [Android App Configuration](#4-android-app-configuration)
5. [Verification](#5-verification)
6. [Management Commands](#6-management-commands)
7. [Troubleshooting](#7-troubleshooting)

---

## 0. Prepare and Push Code to Git

Before you can clone the project on your server, you need to have your code in a Git repository (GitHub/GitLab/Gitea).

### 0.1 Create Repository
Create a new **Private** repository on GitHub or your preferred Git provider.

### 0.2 Initialize and Push (on Local Machine)

Run these commands in your project root folder on your **Local Development Machine**:

```bash
# Initialize git if not already done
git init

# Add all files
git add .

# Commit changes
git commit -m "Initial commit for deployment"

# Rename branch to main (if needed)
git branch -M main

# Add your remote repository URL
git remote add origin YOUR_REPOSITORY_URL

# Push to the server
git push -u origin main
```

---

## 1. Prerequisites

### Server Requirements
- Ubuntu 22.04 LTS (64-bit)
- Minimum 2GB RAM
- Minimum 20GB disk space
- Open ports: 80 (HTTP), optionally 443 (HTTPS)

### Required Software
- Docker Engine 24+
- Docker Compose v2+
- Git

---

## 2. Server Setup

### 2.1 Connect to Server

```bash
ssh root@YOUR_SERVER_IP
```

### 2.2 Update System

```bash
apt update && apt upgrade -y
```

### 2.3 Install Docker

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Start Docker
systemctl enable docker
systemctl start docker

# Verify installation
docker --version
docker compose version
```

### 2.4 Install Git

```bash
apt install git -y
```

---

## 3. Project Deployment

### 3.1 Clone Repository

```bash
cd /opt
git clone YOUR_REPOSITORY_URL messageapp
cd messageapp
```

### 3.2 Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit configuration
nano .env
```

**Update these values in `.env`:**

```env
# Replace with your server's public IP or domain
APP_BASE_URL=http://YOUR_SERVER_IP

# Set a secure database password
POSTGRES_PASSWORD=YOUR_SECURE_PASSWORD

# Generate a secure JWT secret (optional, default works for testing)
# JWT_SECRET=your_generated_secret
```

### 3.3 Build and Start Services

```bash
# Build all images and start containers
docker compose up -d --build
```

This will:
1. Build the Spring Boot API image
2. Build the Admin Panel image
3. Start PostgreSQL database
4. Start all services with Nginx reverse proxy

### 3.4 Check Status

```bash
# View running containers
docker compose ps

# View logs
docker compose logs -f
```

**Expected output:**
```
NAME                COMMAND                  SERVICE   STATUS
messageapp-db       "docker-entrypoint.s…"   postgres  Up (healthy)
messageapp-api      "java -jar app.jar"      api       Up (healthy)
messageapp-admin    "/docker-entrypoint.…"   admin     Up (healthy)
messageapp-nginx    "/docker-entrypoint.…"   nginx     Up (healthy)
```

---

## 4. Android App Configuration

Before building the Android APK, update the server URLs in these files:

### 4.1 Constants.kt
**File:** `app/src/main/java/com/Kelasor/app/util/Constants.kt`

```kotlin
object Constants {
    const val BASE_URL = "http://YOUR_SERVER_IP/"
}
```

### 4.2 NetworkModule.kt
**File:** `app/src/main/java/com/Kelasor/app/di/NetworkModule.kt`

```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP/"
```

### 4.3 WebSocketManager.kt
**File:** `app/src/main/java/com/Kelasor/app/data/websocket/WebSocketManager.kt`

```kotlin
private const val WS_BASE_URL = "ws://YOUR_SERVER_IP/ws"
```

### 4.4 StoryMappers.kt
**File:** `app/src/main/java/com/Kelasor/app/data/mapper/StoryMappers.kt`

```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP"
```

### 4.5 Build APK

After updating all URLs, build the release APK:

```bash
# On your development machine
./gradlew assembleRelease
```

---

## 5. Verification

### 5.1 Check Services

```bash
# API Health Check
curl http://YOUR_SERVER_IP/api/health

# Admin Panel
curl -I http://YOUR_SERVER_IP/admin/
```

### 5.2 Access Points

| Service | URL |
|---------|-----|
| Admin Panel | http://YOUR_SERVER_IP/admin/ |
| API Base | http://YOUR_SERVER_IP/api/ |
| Swagger UI | http://YOUR_SERVER_IP/swagger-ui.html |
| WebSocket | ws://YOUR_SERVER_IP/ws |

### 5.3 Database Verification

```bash
docker compose exec postgres psql -U postgres -d messageapp -c "SELECT 1;"
```

---

## 6. Management Commands

### Start Services
```bash
docker compose up -d
```

### Stop Services
```bash
docker compose down
```

### Restart Services
```bash
docker compose restart
```

### View Logs
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f api
docker compose logs -f admin
docker compose logs -f postgres
docker compose logs -f nginx
```

### Rebuild After Code Changes
```bash
docker compose up -d --build
```

### Database Backup
```bash
docker compose exec postgres pg_dump -U postgres messageapp > backup_$(date +%Y%m%d).sql
```

### Database Restore
```bash
cat backup.sql | docker compose exec -T postgres psql -U postgres -d messageapp
```

### Clean Up
```bash
# Remove containers and networks
docker compose down

# Remove containers, networks, and volumes (DELETES DATA!)
docker compose down -v

# Remove unused images
docker image prune -a
```

---

## 7. Troubleshooting

### Container Won't Start

```bash
# Check logs
docker compose logs api

# Check if port is in use
netstat -tlnp | grep :80
```

### Database Connection Error

```bash
# Check PostgreSQL is running
docker compose ps postgres

# Test database connection
docker compose exec postgres psql -U postgres -d messageapp
```

### API Returns 502 Bad Gateway

```bash
# Check if API container is healthy
docker compose ps api

# Check API logs for errors
docker compose logs api --tail=100
```

### Admin Panel Not Loading

```bash
# Check admin container
docker compose logs admin

# Verify nginx config
docker compose exec nginx nginx -t
```

### WebSocket Connection Failed

Ensure the WebSocket endpoint is accessible:
```bash
# Install wscat if needed
npm install -g wscat

# Test WebSocket
wscat -c "ws://YOUR_SERVER_IP/ws"
```

### Reset Everything

```bash
# Stop and remove all containers, volumes, networks
docker compose down -v

# Remove all images
docker compose down --rmi all

# Start fresh
docker compose up -d --build
```

---

## Architecture Summary

```
                    ┌─────────────────────────────────────────┐
                    │           Ubuntu 22.04 Server           │
                    │                                         │
┌─────────┐         │  ┌─────────┐      ┌─────────────────┐  │
│ Android │────────────▶│  Nginx  │──────▶│  Spring Boot    │  │
│   App   │         │  │  :80    │      │  API :8080      │  │
└─────────┘         │  │         │      │                 │  │
                    │  │  /api   │──────▶│  - REST API     │  │
┌─────────┐         │  │  /ws    │──────▶│  - WebSocket    │  │
│ Browser │────────────▶│  /admin │      │  - File Upload  │  │
└─────────┘         │  │         │      └────────┬────────┘  │
                    │  │         │               │           │
                    │  │         │      ┌────────▼────────┐  │
                    │  │         │      │   PostgreSQL    │  │
                    │  │         │      │     :5432       │  │
                    │  │         │      └─────────────────┘  │
                    │  │         │                           │
                    │  │         │      ┌─────────────────┐  │
                    │  │  /admin ├──────▶│  Admin Panel    │  │
                    │  │         │      │  (React/Nginx)  │  │
                    │  └─────────┘      └─────────────────┘  │
                    │                                         │
                    └─────────────────────────────────────────┘
```

---

## Quick Start Checklist

- [ ] Server has Docker and Docker Compose installed
- [ ] Repository cloned to `/opt/messageapp`
- [ ] `.env` file configured with server IP
- [ ] `docker compose up -d --build` completed successfully
- [ ] All 4 containers running and healthy
- [ ] Admin panel accessible at `http://SERVER_IP/admin/`
- [ ] API responding at `http://SERVER_IP/api/`
- [ ] Android app URLs updated and APK built
