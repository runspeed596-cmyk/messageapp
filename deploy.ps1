<#
.SYNOPSIS
    Build, install, and launch the Android app on all USB-connected devices.
.DESCRIPTION
    This script replaces the need to open Android Studio and click RUN.
    It uses Gradle to build the debug APK, then installs and launches it
    on every device connected via USB Debugging.
.PARAMETER BuildType
    The build variant to use: "debug" (default) or "release".
.PARAMETER SkipBuild
    If set, skips the Gradle build step and uses the existing APK.
.PARAMETER Clean
    If set, runs a clean build.
.EXAMPLE
    .\deploy.ps1
    .\deploy.ps1 -BuildType release
    .\deploy.ps1 -SkipBuild
    .\deploy.ps1 -Clean
#>

param(
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",
    [switch]$SkipBuild,
    [switch]$Clean
)

# -- Configuration -------------------------------------------------------
$PROJECT_ROOT    = $PSScriptRoot
$APPLICATION_ID  = "com.Kelasor.app"
$LAUNCHER_ACTIVITY = "$APPLICATION_ID.MainActivity"
$GRADLE_WRAPPER  = Join-Path $PROJECT_ROOT "gradlew.bat"

# Resolve APK path based on build type
if ($BuildType -eq "debug") {
    $APK_PATH = Join-Path $PROJECT_ROOT "app\build\outputs\apk\debug\app-debug.apk"
    $GRADLE_TASK = "assembleDebug"
} else {
    $APK_PATH = Join-Path $PROJECT_ROOT "app\build\outputs\apk\release\app-release.apk"
    $GRADLE_TASK = "assembleRelease"
}

# Resolve ADB path from local.properties or PATH
$localProps = Join-Path $PROJECT_ROOT "local.properties"
$ADB = $null

if (Test-Path $localProps) {
    $sdkLine = Get-Content $localProps | Where-Object { $_ -match "^sdk\.dir=" }
    if ($sdkLine) {
        $sdkDir = ($sdkLine -replace "^sdk\.dir=", "").Replace("\:", ":").Replace("\\", "\")
        $adbCandidate = Join-Path $sdkDir "platform-tools\adb.exe"
        if (Test-Path $adbCandidate) {
            $ADB = $adbCandidate
        }
    }
}

if (-not $ADB) {
    $ADB = Get-Command "adb" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source
}

if (-not $ADB) {
    Write-Host ""
    Write-Host "  [ERROR] adb not found!" -ForegroundColor Red
    Write-Host "  Make sure Android SDK platform-tools is installed and adb is in your PATH." -ForegroundColor Yellow
    Write-Host ""
    Read-Host "  Press Enter to exit"
    exit 1
}

# -- Helper Functions -----------------------------------------------------
function Write-Banner {
    param([string]$Text)
    $line = "=" * 60
    Write-Host ""
    Write-Host "  $line" -ForegroundColor Cyan
    Write-Host "  $Text" -ForegroundColor Cyan
    Write-Host "  $line" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Step {
    param([string]$Icon, [string]$Text)
    Write-Host "  [$Icon] $Text" -ForegroundColor White
}

function Write-Success {
    param([string]$Text)
    Write-Host "  [OK] $Text" -ForegroundColor Green
}

function Write-Err {
    param([string]$Text)
    Write-Host "  [FAIL] $Text" -ForegroundColor Red
}

# -- Step 1: Detect Connected Devices ------------------------------------
Write-Banner "Kelasor Deploy Tool"
Write-Step "SCAN" "Detecting connected devices..."

$adbOutput = & $ADB devices 2>&1
$devices = @()

foreach ($line in $adbOutput) {
    if ($line -match "^(\S+)\s+device$") {
        $devices += $Matches[1]
    }
}

if ($devices.Count -eq 0) {
    Write-Err "No devices found! Make sure:"
    Write-Host "    1. USB Debugging is enabled on your device" -ForegroundColor Yellow
    Write-Host "    2. The device is connected via USB" -ForegroundColor Yellow
    Write-Host "    3. You have authorized the computer on the device" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "  Press Enter to exit"
    exit 1
}

Write-Success "Found $($devices.Count) device(s):"
foreach ($device in $devices) {
    $model = (& $ADB -s $device shell getprop ro.product.model 2>$null).Trim()
    $androidVer = (& $ADB -s $device shell getprop ro.build.version.release 2>$null).Trim()
    Write-Host "    -> $device  ($model - Android $androidVer)" -ForegroundColor Gray
}
Write-Host ""

# -- Step 2: Build APK ---------------------------------------------------
if (-not $SkipBuild) {
    if ($Clean) {
        Write-Step "BUILD" "Running clean build ($GRADLE_TASK)..."
        $gradleArgs = @("clean", $GRADLE_TASK)
    } else {
        Write-Step "BUILD" "Building APK ($GRADLE_TASK)..."
        $gradleArgs = @($GRADLE_TASK)
    }

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

    # Collect all output so we can show full errors on failure
    $buildOutput = @()
    & $GRADLE_WRAPPER @gradleArgs --stacktrace 2>&1 | ForEach-Object {
        $line = $_
        $buildOutput += $line
        if ($line -match "BUILD SUCCESSFUL") {
            Write-Host "    $line" -ForegroundColor Green
        } elseif ($line -match "BUILD FAILED|FAILURE") {
            Write-Host "    $line" -ForegroundColor Red
        } elseif ($line -match "^e:|^w:") {
            if ($line -match "^e:") {
                Write-Host "    $line" -ForegroundColor Red
            } else {
                Write-Host "    $line" -ForegroundColor Yellow
            }
        } elseif ($line -match "> Task") {
            Write-Host "    $line" -ForegroundColor DarkGray
        }
    }

    $stopwatch.Stop()
    $buildTime = $stopwatch.Elapsed.ToString("mm\:ss")

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Err "Build FAILED! Full error output:"
        Write-Host ""
        Write-Host "  ==================== ERROR LOG ====================" -ForegroundColor Red
        foreach ($line in $buildOutput) {
            if ($line -match "^e:|^w:|ERROR|Exception|FAILURE|BUILD FAILED|Caused by") {
                Write-Host "    $line" -ForegroundColor Red
            } elseif ($line -notmatch "^> Task") {
                Write-Host "    $line" -ForegroundColor DarkGray
            }
        }
        Write-Host "  ==================================================" -ForegroundColor Red
        Write-Host ""
        Read-Host "  Press Enter to exit"
        exit 1
    }

    Write-Success "Build completed in $buildTime"
    Write-Host ""
} else {
    Write-Step "SKIP" "Skipping build (using existing APK)..."
    Write-Host ""
}

# Verify APK exists
if (-not (Test-Path $APK_PATH)) {
    Write-Err "APK not found at: $APK_PATH"
    Write-Host "    Run without -SkipBuild to generate the APK first." -ForegroundColor Yellow
    Read-Host "  Press Enter to exit"
    exit 1
}

$apkSize = [math]::Round((Get-Item $APK_PATH).Length / 1MB, 1)
Write-Step "APK" "APK size: ${apkSize} MB"
Write-Host ""

# -- Step 3: Install & Launch on Each Device ------------------------------
$successCount = 0
$failCount = 0

foreach ($device in $devices) {
    $model = (& $ADB -s $device shell getprop ro.product.model 2>$null).Trim()
    Write-Step "DEPLOY" "Deploying to $model ($device)..."

    # Install APK (using -r to update without removing data)
    Write-Host "    Installing..." -ForegroundColor DarkGray
    $installOutput = & $ADB -s $device install -r -t $APK_PATH 2>&1
    $installResult = $installOutput | Select-Object -Last 1

    if ($installResult -match "Success") {
        Write-Host "    [OK] Installed successfully" -ForegroundColor Green

        # Launch the app
        Write-Host "    Launching app..." -ForegroundColor DarkGray
        & $ADB -s $device shell am start -n "$APPLICATION_ID/$LAUNCHER_ACTIVITY" 2>&1 | Out-Null

        if ($LASTEXITCODE -eq 0) {
            Write-Host "    [OK] App launched!" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "    [WARN] Installed but failed to launch" -ForegroundColor Yellow
            $failCount++
        }
    } else {
        Write-Host "    [FAIL] Install failed: $installResult" -ForegroundColor Red
        $failCount++
    }
    Write-Host ""
}

# -- Summary --------------------------------------------------------------
Write-Banner "Deployment Summary"

if ($successCount -gt 0) {
    Write-Success "$successCount device(s) deployed successfully"
}
if ($failCount -gt 0) {
    Write-Err "$failCount device(s) failed"
}

Write-Host ""
Read-Host "  Press Enter to exit"
