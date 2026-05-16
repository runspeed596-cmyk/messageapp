<#
.SYNOPSIS
    Ultra-minimal deploy - builds JAR + Admin locally, creates tiny tar.gz, uploads SINGLE file via SCP.
    Uses SSH key authentication — no passwords needed after initial key setup.
.EXAMPLE
    .\deploy-server.ps1              # Full build + deploy
    .\deploy-server.ps1 -SkipBuild   # Deploy without rebuilding
    .\deploy-server.ps1 -SkipAdmin   # Skip admin panel build
    .\deploy-server.ps1 -SetupServer # First-time server setup
    .\deploy-server.ps1 -CopyKey     # Copy SSH key to server (one-time, never need password again)
#>

param(
    [switch]$SkipBuild,
    [switch]$SkipAdmin,
    [switch]$SetupServer,
    [switch]$CopyKey,
    [string]$Server = "root@185.116.162.68",
    [int]$SshPort = 3031,
    [string]$RemotePath = "/opt/messageapp"
)

$ErrorActionPreference = "Stop"
$PROJECT_ROOT = $PSScriptRoot
$DEPLOY_DIR = Join-Path $PROJECT_ROOT "deploy_tmp"
$ARCHIVE_NAME = "kelasor-deploy.tar.gz"
$ARCHIVE_PATH = Join-Path $PROJECT_ROOT $ARCHIVE_NAME

# ── Common SSH/SCP options ──────────────────────────────────
$SSH_OPTS = @("-p", "$SshPort", "-o", "ConnectTimeout=10", "-o", "ServerAliveInterval=30", "-o", "ServerAliveCountMax=3", "-o", "StrictHostKeyChecking=no", "-o", "BatchMode=yes")
$SCP_OPTS = @("-P", "$SshPort", "-o", "ConnectTimeout=10", "-o", "StrictHostKeyChecking=no", "-o", "BatchMode=yes")

function Invoke-Ssh {
    param([string]$Command)
    & ssh @SSH_OPTS $Server $Command
    return $LASTEXITCODE
}

function Invoke-Scp {
    param([string]$LocalFile, [string]$RemoteFile)
    & scp @SCP_OPTS $LocalFile "${Server}:${RemoteFile}"
    return $LASTEXITCODE
}

function Test-SshConnection {
    <# Quick test: can we connect without a password? #>
    $oldEAP = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $result = & ssh @SSH_OPTS $Server "echo OK" 2>&1
    $code = $LASTEXITCODE
    $ErrorActionPreference = $oldEAP
    if ($code -eq 0 -and ($result -join "") -match "OK") {
        return $true
    }
    return $false
}

Write-Host ""
Write-Host "  ========================================" -ForegroundColor Cyan
Write-Host "  Kelasor Ultra-Minimal Deploy" -ForegroundColor Cyan
Write-Host "  Server: kelasorapp.ir (185.116.162.68:3031)" -ForegroundColor Cyan
Write-Host "  ========================================" -ForegroundColor Cyan

# == Copy SSH Key (one-time setup) ==
if ($CopyKey) {
    Write-Host ""
    Write-Host "  [KEY] Copying SSH key to server..." -ForegroundColor Yellow
    Write-Host "  [KEY] Enter your password ONE LAST TIME. After this, no more passwords!" -ForegroundColor Gray
    Write-Host ""

    $keyFile = "$env:USERPROFILE\.ssh\id_ed25519.pub"
    if (-not (Test-Path $keyFile)) {
        $keyFile = "$env:USERPROFILE\.ssh\id_rsa.pub"
    }
    if (-not (Test-Path $keyFile)) {
        Write-Host "  [FAIL] No SSH key found! Run: ssh-keygen -t ed25519" -ForegroundColor Red
        exit 1
    }

    $pubKey = (Get-Content $keyFile -Raw).Trim()

    # Use a single SSH command to add the key (without BatchMode so it asks for password)
    $addKeyCmd = "mkdir -p ~/.ssh; chmod 700 ~/.ssh; echo '$pubKey' >> ~/.ssh/authorized_keys; sort -u ~/.ssh/authorized_keys -o ~/.ssh/authorized_keys; chmod 600 ~/.ssh/authorized_keys; echo KEY_INSTALLED_OK"
    & ssh -p $SshPort -o ConnectTimeout=10 -o StrictHostKeyChecking=no $Server $addKeyCmd

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "  [OK] SSH key installed! You will NEVER need a password again." -ForegroundColor Green
        Write-Host ""
        # Verify it works
        Write-Host "  [TEST] Verifying key-based login..." -ForegroundColor Yellow
        if (Test-SshConnection) {
            Write-Host "  [OK] Password-free login works!" -ForegroundColor Green
        } else {
            Write-Host "  [WARN] Key installed but test failed. Try manually:" -ForegroundColor Yellow
            Write-Host "  ssh -p $SshPort $Server 'echo hello'" -ForegroundColor Gray
        }
    } else {
        Write-Host "  [FAIL] Key installation failed." -ForegroundColor Red
    }
    exit 0
}

# == Verify SSH key connection works ==
Write-Host ""
Write-Host "  [SSH] Testing connection..." -ForegroundColor Yellow
if (Test-SshConnection) {
    Write-Host "  [SSH] Connected (key-based, no password)." -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Cannot connect without password!" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Run this first to set up password-free SSH:" -ForegroundColor Yellow
    Write-Host "    .\deploy-server.ps1 -CopyKey" -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

# == First-time server setup ==
if ($SetupServer) {
    Write-Host ""
    Write-Host "  [SETUP] Uploading server-setup.sh..." -ForegroundColor Yellow
    $setupScript = Join-Path $PROJECT_ROOT "scripts\server-setup.sh"
    Invoke-Scp -LocalFile $setupScript -RemoteFile "/opt/server-setup.sh"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [FAIL] Upload failed!" -ForegroundColor Red
        exit 1
    }

    Write-Host "  [SETUP] Running setup on server..." -ForegroundColor Yellow
    Write-Host "  [SETUP] This runs in ONE SSH session with remote polling. Just wait..." -ForegroundColor Gray
    Write-Host ""

    # Run setup via nohup, then poll in the SAME SSH session using a remote loop.
    # This is ONE SSH call instead of 60!
    $remoteSetupScript = @'
chmod +x /opt/server-setup.sh
nohup bash /opt/server-setup.sh > /opt/setup.log 2>&1 &
SETUP_PID=$!
echo "Setup started (PID: $SETUP_PID)"
echo "Polling log every 10 seconds..."
attempt=0
max_attempts=60
while [ $attempt -lt $max_attempts ]; do
    attempt=$((attempt + 1))
    sleep 10
    if tail -5 /opt/setup.log 2>/dev/null | grep -q "SERVER SETUP COMPLETE\|Next steps"; then
        echo ""
        echo "========================================="
        echo "  SERVER SETUP COMPLETE!"
        echo "========================================="
        echo "  Now run: .\deploy-server.ps1"
        exit 0
    fi
    LAST_LINE=$(tail -1 /opt/setup.log 2>/dev/null || echo "waiting...")
    echo "  [SETUP] Still running... (attempt $attempt/$max_attempts) - $LAST_LINE"
done
echo ""
echo "[WARN] Setup may still be running. Check: cat /opt/setup.log"
'@

    Invoke-Ssh -Command $remoteSetupScript
    exit 0
}

# == Step 1: Build Spring Boot JAR ==
if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "  [1/5] Building Spring Boot JAR..." -ForegroundColor Yellow
    Push-Location (Join-Path $PROJECT_ROOT "SpringBoot")
    & .\gradlew.bat bootJar --no-daemon -q
    if ($LASTEXITCODE -ne 0) {
        Pop-Location
        Write-Host "  [FAIL] JAR build failed!" -ForegroundColor Red
        exit 1
    }
    Pop-Location

    $jarFile = Get-ChildItem (Join-Path $PROJECT_ROOT "SpringBoot\build\libs\*.jar") | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    $jarSizeMB = [math]::Round($jarFile.Length / 1MB, 1)
    Write-Host ("  [OK] JAR: {0} ({1} MB)" -f $jarFile.Name, $jarSizeMB) -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "  [1/5] Skipping JAR build" -ForegroundColor DarkGray
}

# == Step 2: Build Admin Panel ==
if (-not $SkipBuild -and -not $SkipAdmin) {
    Write-Host ""
    Write-Host "  [2/5] Building Admin Panel..." -ForegroundColor Yellow
    Push-Location (Join-Path $PROJECT_ROOT "admin-panel")
    # Temporarily allow stderr from node.exe (Vite/tsc writes warnings to stderr)
    $oldEAP = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    if (-not (Test-Path "node_modules")) {
        Write-Host "  Installing npm dependencies..." -ForegroundColor Gray
        & cmd /c "npm install --prefer-offline 2>&1" | Out-Null
    }
    $env:VITE_API_URL = "/api"
    $buildOutput = & cmd /c "npm run build 2>&1"
    $buildExitCode = $LASTEXITCODE
    $ErrorActionPreference = $oldEAP
    if ($buildExitCode -ne 0) {
        Pop-Location
        Write-Host "  [FAIL] Admin build failed! Errors:" -ForegroundColor Red
        $buildOutput | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkRed }
        exit 1
    }
    Remove-Item Env:\VITE_API_URL -ErrorAction SilentlyContinue
    Pop-Location

    $distBytes = (Get-ChildItem (Join-Path $PROJECT_ROOT "admin-panel\dist") -Recurse -File | Measure-Object Length -Sum).Sum
    $distSizeMB = [math]::Round($distBytes / 1MB, 1)
    Write-Host ("  [OK] Admin dist: {0} MB" -f $distSizeMB) -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "  [2/5] Skipping Admin build" -ForegroundColor DarkGray
}

# == Step 3: Create ultra-minimal deploy package ==
Write-Host ""
Write-Host "  [3/5] Creating minimal deploy package..." -ForegroundColor Yellow

if (Test-Path $DEPLOY_DIR) { Remove-Item $DEPLOY_DIR -Recurse -Force }
New-Item $DEPLOY_DIR -ItemType Directory -Force | Out-Null

# SpringBoot: ONLY Dockerfile + JAR (no source code!)
$sbLibs = Join-Path $DEPLOY_DIR "SpringBoot\build\libs"
New-Item $sbLibs -ItemType Directory -Force | Out-Null
Copy-Item (Join-Path $PROJECT_ROOT "SpringBoot\Dockerfile") (Join-Path $DEPLOY_DIR "SpringBoot\")
Copy-Item (Join-Path $PROJECT_ROOT "SpringBoot\build\libs\*.jar") $sbLibs

# Admin Panel: ONLY Dockerfile + nginx.conf + dist/ (no source!)
$apDir = Join-Path $DEPLOY_DIR "admin-panel"
New-Item $apDir -ItemType Directory -Force | Out-Null
Copy-Item (Join-Path $PROJECT_ROOT "admin-panel\Dockerfile") $apDir
Copy-Item (Join-Path $PROJECT_ROOT "admin-panel\nginx.conf") $apDir
Copy-Item (Join-Path $PROJECT_ROOT "admin-panel\dist") (Join-Path $apDir "dist") -Recurse

# Kelasor Online Portal: static files + Dockerfile
$koDir = Join-Path $DEPLOY_DIR "kelasor-online"
New-Item $koDir -ItemType Directory -Force | Out-Null
Copy-Item (Join-Path $PROJECT_ROOT "kelasor-online\Dockerfile") $koDir
Copy-Item (Join-Path $PROJECT_ROOT "kelasor-online\index.html") $koDir
Copy-Item (Join-Path $PROJECT_ROOT "kelasor-online\style.css") $koDir
Copy-Item (Join-Path $PROJECT_ROOT "kelasor-online\app.js") $koDir

# Jitsi Meet custom config files
$jcDir = Join-Path $DEPLOY_DIR "jitsi-config"
New-Item $jcDir -ItemType Directory -Force | Out-Null
Copy-Item (Join-Path $PROJECT_ROOT "jitsi-config\custom.css") $jcDir
Copy-Item (Join-Path $PROJECT_ROOT "jitsi-config\custom-config.js") $jcDir
Copy-Item (Join-Path $PROJECT_ROOT "jitsi-config\custom-interface_config.js") $jcDir
Copy-Item (Join-Path $PROJECT_ROOT "jitsi-config\fonts") $jcDir -Recurse

# Nginx: config files only
$ngDir = Join-Path $DEPLOY_DIR "nginx"
New-Item $ngDir -ItemType Directory -Force | Out-Null
Copy-Item (Join-Path $PROJECT_ROOT "nginx\nginx.conf") $ngDir
Copy-Item (Join-Path $PROJECT_ROOT "nginx\nginx-http-only.conf") $ngDir
Copy-Item (Join-Path $PROJECT_ROOT "admin-panel\intro.html") $ngDir

# Root config files
Copy-Item (Join-Path $PROJECT_ROOT "docker-compose.yml") $DEPLOY_DIR
Copy-Item (Join-Path $PROJECT_ROOT ".env.example") $DEPLOY_DIR

# Scripts (small)
$scDir = Join-Path $DEPLOY_DIR "scripts"
New-Item $scDir -ItemType Directory -Force | Out-Null
Copy-Item (Join-Path $PROJECT_ROOT "scripts\server-deploy.sh") $scDir
Copy-Item (Join-Path $PROJECT_ROOT "scripts\daemon.json") $scDir

$folderBytes = (Get-ChildItem $DEPLOY_DIR -Recurse -File | Measure-Object Length -Sum).Sum
$folderSizeMB = [math]::Round($folderBytes / 1MB, 1)
Write-Host ("  Uncompressed: {0} MB" -f $folderSizeMB) -ForegroundColor Gray

# Create tar.gz (much smaller than ZIP!)
if (Test-Path $ARCHIVE_PATH) { Remove-Item $ARCHIVE_PATH -Force }

$tarExe = Get-Command "tar" -ErrorAction SilentlyContinue
if ($tarExe) {
    & tar -czf $ARCHIVE_PATH -C $DEPLOY_DIR .
}

if (-not $tarExe -or $LASTEXITCODE -ne 0) {
    # Fallback to ZIP if tar not available
    Write-Host "  tar not available, using ZIP..." -ForegroundColor Yellow
    $ARCHIVE_NAME = "kelasor-deploy.zip"
    $ARCHIVE_PATH = Join-Path $PROJECT_ROOT $ARCHIVE_NAME
    if (Test-Path $ARCHIVE_PATH) { Remove-Item $ARCHIVE_PATH -Force }
    Compress-Archive -Path (Join-Path $DEPLOY_DIR "*") -DestinationPath $ARCHIVE_PATH -Force
}

$archiveSizeMB = [math]::Round((Get-Item $ARCHIVE_PATH).Length / 1MB, 1)
Write-Host ("  [OK] Archive: {0} ({1} MB)" -f $ARCHIVE_NAME, $archiveSizeMB) -ForegroundColor Green

# Cleanup temp dir
Remove-Item $DEPLOY_DIR -Recurse -Force

# == Step 4: Upload single file via SCP ==
Write-Host ""
Write-Host ("  [4/5] Uploading {0} to server..." -f $ARCHIVE_NAME) -ForegroundColor Yellow

Invoke-Scp -LocalFile $ARCHIVE_PATH -RemoteFile "/opt/${ARCHIVE_NAME}"
if ($LASTEXITCODE -ne 0) {
    Write-Host "  [FAIL] Upload failed!" -ForegroundColor Red
    exit 1
}
Write-Host ("  [OK] Uploaded ({0} MB)" -f $archiveSizeMB) -ForegroundColor Green

# == Step 5: Extract and rebuild on server ==
Write-Host ""
Write-Host "  [5/5] Deploying on server..." -ForegroundColor Yellow

$remoteScript = "cd /opt; mkdir -p messageapp; rm -rf messageapp/SpringBoot messageapp/admin-panel messageapp/kelasor-online messageapp/nginx messageapp/scripts; rm -f messageapp/docker-compose.yml messageapp/.env.example; tar -xzf $ARCHIVE_NAME -C messageapp 2>/dev/null || unzip -o $ARCHIVE_NAME -d messageapp; rm -f /opt/$ARCHIVE_NAME; chmod +x messageapp/scripts/*.sh 2>/dev/null; cd messageapp; docker compose up -d --build --remove-orphans 2>&1 | tail -20"

Invoke-Ssh -Command $remoteScript

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "  ========================================" -ForegroundColor Green
    Write-Host "  DEPLOY COMPLETE!" -ForegroundColor Green
    Write-Host "  ========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "  API:    http://kelasorapp.ir/api/" -ForegroundColor White
    Write-Host "  Admin:  http://kelasorapp.ir/ca978112ca/" -ForegroundColor White
    Write-Host "  Online: http://online.kelasorapp.ir/" -ForegroundColor White
    Write-Host "  WS:     ws://kelasorapp.ir/ws/" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "  [WARN] Check server logs manually:" -ForegroundColor Yellow
    Write-Host ("  ssh -p {0} {1}" -f $SshPort, $Server) -ForegroundColor Gray
    Write-Host "  cd /opt/messageapp; docker compose logs --tail=50" -ForegroundColor Gray
}

# Cleanup local archive
Remove-Item $ARCHIVE_PATH -Force -ErrorAction SilentlyContinue

Write-Host ""
