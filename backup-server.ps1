<#
.SYNOPSIS
    Backup Kelasor server and download to local machine.
.EXAMPLE
    .\backup-server.ps1              # Run backup + download
    .\backup-server.ps1 -DownloadOnly "kelasor-full-backup-20260516.tar.gz"  # Just download existing backup
#>

param(
    [string]$Server = "root@185.116.162.68",
    [int]$SshPort = 3031,
    [string]$DownloadOnly = ""
)

$ErrorActionPreference = "Stop"
$SSH_OPTS = @("-p", "$SshPort", "-o", "ConnectTimeout=10", "-o", "StrictHostKeyChecking=no", "-o", "BatchMode=yes")
$SCP_OPTS = @("-P", "$SshPort", "-o", "ConnectTimeout=10", "-o", "StrictHostKeyChecking=no", "-o", "BatchMode=yes")

Write-Host ""
Write-Host "  ========================================" -ForegroundColor Cyan
Write-Host "  [BACKUP] Kelasor Server Backup" -ForegroundColor Cyan
Write-Host "  Server: 185.116.162.68:3031" -ForegroundColor Cyan
Write-Host "  ========================================" -ForegroundColor Cyan

# Test SSH connection
Write-Host ""
Write-Host "  [SSH] Testing connection..." -ForegroundColor Yellow
$result = & ssh @SSH_OPTS $Server "echo OK" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "  [FAIL] Cannot connect! Run: .\deploy-server.ps1 -CopyKey" -ForegroundColor Red
    exit 1
}
Write-Host "  [SSH] Connected." -ForegroundColor Green

if ($DownloadOnly) {
    # Just download an existing backup
    $remoteFile = "/opt/$DownloadOnly"
    $localFile = Join-Path $PSScriptRoot "backups\$DownloadOnly"
} else {
    # Step 1: Upload backup script
    Write-Host ""
    Write-Host "  [1/3] Uploading backup script..." -ForegroundColor Yellow
    
    $backupScript = Join-Path $PSScriptRoot "scripts\server-backup.sh"
    & scp @SCP_OPTS $backupScript "${Server}:/opt/server-backup.sh"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [FAIL] Upload failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "  [OK] Script uploaded" -ForegroundColor Green

    # Step 2: Run backup on server
    Write-Host ""
    Write-Host "  [2/3] Running backup on server (this may take a few minutes)..." -ForegroundColor Yellow
    Write-Host ""
    
    & ssh @SSH_OPTS $Server "chmod +x /opt/server-backup.sh && bash /opt/server-backup.sh"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [FAIL] Backup failed on server!" -ForegroundColor Red
        exit 1
    }

    # Get the backup filename
    $backupFilename = & ssh @SSH_OPTS $Server "ls -t /opt/kelasor-full-backup-*.tar.gz 2>/dev/null | head -1"
    if (-not $backupFilename -or $LASTEXITCODE -ne 0) {
        Write-Host "  [FAIL] Backup file not found on server!" -ForegroundColor Red
        exit 1
    }
    
    $remoteFile = $backupFilename.Trim()
    $localFilename = Split-Path $remoteFile -Leaf
    $localFile = Join-Path $PSScriptRoot "backups\$localFilename"
}

# Step 3: Download backup
Write-Host ""
Write-Host "  [3/3] Downloading backup to local machine..." -ForegroundColor Yellow

$backupsDir = Join-Path $PSScriptRoot "backups"
if (-not (Test-Path $backupsDir)) {
    New-Item $backupsDir -ItemType Directory -Force | Out-Null
}

& scp @SCP_OPTS "${Server}:${remoteFile}" $localFile

if ($LASTEXITCODE -eq 0) {
    $sizeMB = [math]::Round((Get-Item $localFile).Length / 1MB, 1)
    Write-Host ""
    Write-Host "  ========================================" -ForegroundColor Green
    Write-Host "  [OK] BACKUP DOWNLOADED!" -ForegroundColor Green
    Write-Host "  ========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "  File: $localFile" -ForegroundColor White
    Write-Host "  Size: ${sizeMB} MB" -ForegroundColor White
    Write-Host ""
    Write-Host "  [!] Keep this file SAFE! It contains ALL your data." -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host "  [FAIL] Download failed!" -ForegroundColor Red
    Write-Host "  Try manually: scp -P $SshPort ${Server}:${remoteFile} ." -ForegroundColor Gray
    exit 1
}
