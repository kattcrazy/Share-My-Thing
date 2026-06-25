#Requires -Version 5.1
<#
.SYNOPSIS
  Build signed release APKs with the Play Console-registered upload keystore.

.DESCRIPTION
  Uses Android Studio JBR and verifies APK SHA-256 matches upload-keystore.jks.
  Does not create or modify upload-keystore.jks or keystore.properties.
#>
$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ExpectedSha256 = "FF05C11D4FEEB2A7529F48481D41D16C2571EBDC1798F3DBBD9BF8E43C1F86C7"

function Find-AndroidStudioJbr {
    foreach ($path in @(
            "$env:LOCALAPPDATA\Programs\Android Studio\jbr",
            "$env:ProgramFiles\Android\Android Studio\jbr",
            "$env:ProgramFiles\Android\Android Studio1\jbr"
        )) {
        if (Test-Path (Join-Path $path "bin\java.exe")) { return $path }
    }
    throw "Android Studio JBR not found. Set JAVA_HOME to JDK 17 or 21."
}

function Find-Apksigner {
    $root = Join-Path $env:LOCALAPPDATA "Android\Sdk\build-tools"
    if (-not (Test-Path $root)) { throw "Android SDK build-tools not found at $root" }
    $latest = Get-ChildItem $root -Directory | Sort-Object Name -Descending | Select-Object -First 1
    $bat = Join-Path $latest.FullName "apksigner.bat"
    if (-not (Test-Path $bat)) { throw "apksigner.bat not found under $root" }
    return $bat
}

function Get-ApkSha256 {
    param([string] $Apksigner, [string] $ApkPath)
    $certLine = & $Apksigner verify --print-certs $ApkPath 2>&1 | Select-String "SHA-256 digest:" | Select-Object -First 1
    if (-not $certLine) { throw "Could not read APK certificate from apksigner output." }
    return ($certLine -replace ".*SHA-256 digest:\s*", "").Trim().Replace(":", "").ToUpper()
}

$keystore = Join-Path $ProjectRoot "upload-keystore.jks"
$props = Join-Path $ProjectRoot "keystore.properties"
if (-not (Test-Path $keystore)) { throw "upload-keystore.jks not found. Do not regenerate - restore your backed-up keystore." }
if (-not (Test-Path $props)) { throw "keystore.properties not found. Copy keystore.properties.example and fill in your credentials." }

$env:JAVA_HOME = Find-AndroidStudioJbr
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Push-Location $ProjectRoot
try {
    & .\gradlew.bat ":mobile:assembleRelease" ":wear:assembleRelease" --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE" }

    $apks = @(
        @{ Label = "mobile"; Path = Join-Path $ProjectRoot "mobile\build\outputs\apk\release\mobile-release.apk" },
        @{ Label = "wear"; Path = Join-Path $ProjectRoot "wear\build\outputs\apk\release\wear-release.apk" }
    )
    $apksigner = Find-Apksigner

    foreach ($entry in $apks) {
        if (-not (Test-Path $entry.Path)) { throw "Release APK not found at $($entry.Path)" }
        $actual = Get-ApkSha256 -Apksigner $apksigner -ApkPath $entry.Path
        if ($actual -ne $ExpectedSha256) {
            throw "$($entry.Label) APK SHA-256 ($actual) does not match Play Console upload key ($ExpectedSha256)."
        }
        Write-Host "  $($entry.Label): signed with upload key ($actual)" -ForegroundColor Green
    }

    Write-Host ""
    Write-Host "Release APKs ready:" -ForegroundColor Green
    foreach ($entry in $apks) { Write-Host "  $($entry.Path)" }
} finally {
    Pop-Location
}
