@echo off
chcp 65001 >nul 2>&1
title Kelasor Deploy Tool
echo.
echo  ══════════════════════════════════════════════
echo   Kelasor Deploy Tool - Build ^& Run on Device
echo  ══════════════════════════════════════════════
echo.
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0deploy.ps1" %*
echo.
echo  Done.
pause
