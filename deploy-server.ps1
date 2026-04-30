<#
.SYNOPSIS
    Smart deploy — builds only needed files, creates tiny ZIP, uploads via SCP.
.DESCRIPTION
    Instead of zipping the entire project (1GB+), this script:
    1. Builds the Spring Boot JAR
    2. Creates a minimal ZIP with only server-needed files (~50MB)
    3. Uploads via SCP and rebuilds on server
.EXAMPLE
    .\deploy-server.ps1              # Build JAR + deploy
    .\deploy-server.ps1 -SkipBuild   # Deploy without rebuilding JAR
#>

param(
    [switch]$SkipBuild,
    [string]$Server = "root@194.5.175.30",
    [string]$RemotePath = "/opt/messageapp"
)

$ErrorActionPreference = "Stop"
$PROJECT_ROOT = $PSScriptRoot
$TEMP_DIR = "$env:TEMP\messageapp-deploy"
$ZIP_PATH = "$env:TEMP\messageapp-deploy.zip"

Write-Host ""
Write-Host "  ========================================" -ForegroundColor Cyan
Write-Host "  Kelasor Smart Deploy" -ForegroundColor Cyan
Write-Host "  ========================================" -ForegroundColor Cyan

# ── Step 1: Build JAR ──
if (-not $SkipBuild) {
    Write-Host "`n  [1/4] Building Spring Boot JAR..." -ForegroundColor Yellow
    Push-Location "$PROJECT_ROOT\SpringBoot"
    & .\gradlew.bat bootJar --no-daemon -q
    if ($LASTEXITCODE -ne 0) { Pop-Location; Write-Host "  [FAIL] Build failed!" -ForegroundColor Red; exit 1 }
    Pop-Location
    Write-Host "  [OK] JAR built" -ForegroundColor Green
} else {
    Write-Host "`n  [1/4] Skipping JAR build" -ForegroundColor DarkGray
}

# ── Step 2: Prepare minimal deploy folder ──
Write-Host "`n  [2/4] Preparing minimal deploy package..." -ForegroundColor Yellow

# Clean temp
if (Test-Path $TEMP_DIR) { Remove-Item $TEMP_DIR -Recurse -Force }
New-Item $TEMP_DIR -ItemType Directory -Force | Out-Null

# Copy SpringBoot (only what Docker needs)
$sbDest = "$TEMP_DIR\SpringBoot"
New-Item "$sbDest\build\libs" -ItemType Directory -Force | Out-Null

Copy-Item "$PROJECT_ROOT\SpringBoot\Dockerfile" "$sbDest\"
Copy-Item "$PROJECT_ROOT\SpringBoot\build\libs\*.jar" "$sbDest\build\libs\" -ErrorAction SilentlyContinue
# Copy src tree — use destination WITHOUT \src to avoid src\src nesting
Copy-Item "$PROJECT_ROOT\SpringBoot\src" "$sbDest\" -Recurse

# Copy admin-panel (exclude node_modules only — dist/ is needed by Dockerfile!)
$apDest = "$TEMP_DIR\admin-panel"
Copy-Item "$PROJECT_ROOT\admin-panel" $apDest -Recurse
if (Test-Path "$apDest\node_modules") { Remove-Item "$apDest\node_modules" -Recurse -Force }

# Copy nginx configs
Copy-Item "$PROJECT_ROOT\nginx" "$TEMP_DIR\nginx" -Recurse

# Copy root files
Copy-Item "$PROJECT_ROOT\docker-compose.yml" "$TEMP_DIR\"
Copy-Item "$PROJECT_ROOT\.env.example" "$TEMP_DIR\"
Copy-Item "$PROJECT_ROOT\.gitignore" "$TEMP_DIR\"

# Show size
$folderSize = [math]::Round((Get-ChildItem $TEMP_DIR -Recurse -File | Measure-Object Length -Sum).Sum / 1MB, 1)
Write-Host "  [OK] Deploy package: ${folderSize} MB (instead of 1GB!)" -ForegroundColor Green

# ── Step 3: ZIP and upload ──
Write-Host "`n  [3/4] Zipping and uploading..." -ForegroundColor Yellow

if (Test-Path $ZIP_PATH) { Remove-Item $ZIP_PATH -Force }
Compress-Archive -Path "$TEMP_DIR\*" -DestinationPath $ZIP_PATH -Force

$zipSize = [math]::Round((Get-Item $ZIP_PATH).Length / 1MB, 1)
Write-Host "  ZIP size: ${zipSize} MB" -ForegroundColor Gray

& scp $ZIP_PATH "${Server}:/opt/messageapp-deploy.zip"
if ($LASTEXITCODE -ne 0) { Write-Host "  [FAIL] Upload failed!" -ForegroundColor Red; exit 1 }
Write-Host "  [OK] Uploaded" -ForegroundColor Green

# ── Step 4: Extract and rebuild on server ──
Write-Host "`n  [4/4] Rebuilding on server..." -ForegroundColor Yellow

& ssh $Server "cd /opt && rm -rf messageapp/SpringBoot messageapp/admin-panel messageapp/nginx messageapp/docker-compose.yml messageapp/.env.example messageapp/.gitignore && unzip -o messageapp-deploy.zip -d messageapp && rm messageapp-deploy.zip && cd messageapp && docker-compose up -d --build"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n  ========================================" -ForegroundColor Green
    Write-Host "  DEPLOY COMPLETE!" -ForegroundColor Green
    Write-Host "  ========================================" -ForegroundColor Green
} else {
    Write-Host "`n  [WARN] Check server manually" -ForegroundColor Yellow
}

# Cleanup
Remove-Item $TEMP_DIR -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $ZIP_PATH -Force -ErrorAction SilentlyContinue

Write-Host ""
