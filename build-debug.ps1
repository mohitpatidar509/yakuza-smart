# Yakuza Smart — Local CLI Build Helper Script
param (
    [switch]$Help
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  YAKUZA SMART — CLI APK BUILDER" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Check for Java
$javaCheck = Get-Command "java" -ErrorAction SilentlyContinue
if (-not $javaCheck) {
    Write-Host "[!] Java (JDK 17) is not detected in your PATH." -ForegroundColor Red
    Write-Host "    To install JDK 17 via Windows Package Manager, run:" -ForegroundColor White
    Write-Host "    winget install EclipseAdoptium.Temurin.17.JDK" -ForegroundColor Yellow
    Write-Host "    Then restart PowerShell and re-run this script." -ForegroundColor White
    Exit 1
} else {
    Write-Host "[✓] Java runtime detected: $($javaCheck.Source)" -ForegroundColor Green
}

# 2. Check for Android SDK
$androidHome = $env:ANDROID_HOME
if (-not $androidHome -and (Test-Path "$env:LOCALAPPDATA\Android\Sdk")) {
    $androidHome = "$env:LOCALAPPDATA\Android\Sdk"
    $env:ANDROID_HOME = $androidHome
}

if (-not $androidHome) {
    Write-Host "[!] ANDROID_HOME environment variable is not set." -ForegroundColor Red
    Write-Host "    If Android Studio or cmdline-tools is installed, set ANDROID_HOME to your SDK path." -ForegroundColor White
    Write-Host "    Default path: $env:LOCALAPPDATA\Android\Sdk" -ForegroundColor Yellow
} else {
    Write-Host "[✓] Android SDK detected at: $androidHome" -ForegroundColor Green
}

# 3. Check for Gradle
$gradleCheck = Get-Command "gradle" -ErrorAction SilentlyContinue
if ($gradleCheck) {
    Write-Host "[*] Executing Gradle assembleDebug..." -ForegroundColor Cyan
    gradle assembleDebug
} else {
    Write-Host "[!] Gradle is not installed globally." -ForegroundColor Yellow
    Write-Host "    You can install Gradle via: winget install Gradle.Gradle" -ForegroundColor Yellow
    Write-Host "    Or open this project in Android Studio and click Build > Build APK." -ForegroundColor White
}
