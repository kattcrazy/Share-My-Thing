#Requires -Version 5.1
<#
.SYNOPSIS
  Build signed release APKs/AABs and optionally publish to GitHub Releases.

.DESCRIPTION
  1. Runs build-local.ps1 -Release (copies off Google Drive, builds release)
  2. Runs bundleRelease for Play-style AABs
  3. Signs APKs if keystore.properties exists (else debug keystore for local testing)
  4. Writes dist/release/v{version}/ with checksums
  5. With -GitHub: creates/updates GitHub Release via gh CLI

  Play Store is NOT auto-uploaded from this script.

.EXAMPLE
  .\scripts\release.ps1 -VersionTag v1.0.0
  .\scripts\release.ps1 -VersionTag v1.0.0 -GitHub
  .\scripts\release.ps1 -VersionTag v1.0.0 -SkipBuild -GitHub
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $VersionTag,

    [switch] $SkipBuild,
    [switch] $GitHub,
    [switch] $DryRun,

    [string] $BuildDir = $(Join-Path $env:LOCALAPPDATA "ShareMyThing-build"),
    [string] $ConfigPath = ""
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
if (-not $ConfigPath) {
    $ConfigPath = Join-Path $PSScriptRoot "release.config.json"
}

function Write-Step([string] $Message) {
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Read-GradleVersion {
    param([string] $GradleFile)
    $text = Get-Content $GradleFile -Raw
    $code = if ($text -match 'versionCode\s*=\s*(\d+)') { [int]$Matches[1] } else { throw "versionCode not found in $GradleFile" }
    $name = if ($text -match 'versionName\s*=\s*"([^"]+)"') { $Matches[1] } else { throw "versionName not found in $GradleFile" }
    return @{ Code = $code; Name = $name }
}

function Find-AndroidStudioJbr {
    foreach ($path in @(
            "$env:LOCALAPPDATA\Programs\Android Studio\jbr",
            "$env:ProgramFiles\Android\Android Studio\jbr",
            "$env:ProgramFiles\Android\Android Studio1\jbr"
        )) {
        if (Test-Path (Join-Path $path "bin\java.exe")) { return $path }
    }
    return $null
}

function Find-Apksigner {
    $root = Join-Path $env:LOCALAPPDATA "Android\Sdk\build-tools"
    if (-not (Test-Path $root)) { throw "Android SDK build-tools not found at $root" }
    $latest = Get-ChildItem $root -Directory | Sort-Object Name -Descending | Select-Object -First 1
    $bat = Join-Path $latest.FullName "apksigner.bat"
    if (-not (Test-Path $bat)) { throw "apksigner.bat not found under $root" }
    return $bat
}

function Sign-Apk {
    param(
        [string] $Apksigner,
        [string] $InputApk,
        [string] $OutputApk,
        [string] $Keystore,
        [string] $StorePass,
        [string] $KeyPass,
        [string] $KeyAlias
    )
    if (Test-Path $OutputApk) { Remove-Item $OutputApk -Force }
    & $Apksigner sign --ks $Keystore --ks-pass "pass:$StorePass" --key-pass "pass:$KeyPass" --ks-key-alias $KeyAlias --out $OutputApk $InputApk
    if ($LASTEXITCODE -ne 0) { throw "apksigner failed for $InputApk" }
    & $Apksigner verify --verbose $OutputApk | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "apksigner verify failed for $OutputApk" }
}

function Find-Artifact {
    param(
        [string] $BaseDir,
        [string] $Module,
        [string] $Kind,
        [string] $Variant = "release"
    )
    if ($Kind -eq "apk") {
        $dir = Join-Path $BaseDir "$Module\build\outputs\apk\$Variant"
        $file = Get-ChildItem $dir -Filter "*.apk" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    } elseif ($Kind -eq "aab") {
        $dir = Join-Path $BaseDir "$Module\build\outputs\bundle\$Variant"
        $file = Get-ChildItem $dir -Filter "*.aab" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    } elseif ($Kind -eq "native-debug-symbols") {
        $dir = Join-Path $BaseDir "$Module\build\outputs\native-debug-symbols\$Variant"
        $file = Get-ChildItem $dir -Filter "native-debug-symbols.zip" -ErrorAction SilentlyContinue | Select-Object -First 1
    } else {
        throw "Unknown artifact kind: $Kind"
    }
    if (-not $file) { throw "Could not find $Kind for $Module under $dir" }
    return $file.FullName
}

$mobileVer = Read-GradleVersion (Join-Path $ProjectRoot "mobile\build.gradle.kts")
$wearVer = Read-GradleVersion (Join-Path $ProjectRoot "wear\build.gradle.kts")
if ($mobileVer.Code -ne $wearVer.Code -or $mobileVer.Name -ne $wearVer.Name) {
    throw "mobile and wear versionCode/versionName must match. mobile=$($mobileVer.Name)/$($mobileVer.Code) wear=$($wearVer.Name)/$($wearVer.Code)"
}
$expectedTag = "v$($mobileVer.Name)"
if ($VersionTag -ne $expectedTag) {
    Write-Warning "VersionTag '$VersionTag' does not match Gradle versionName '$($mobileVer.Name)' (expected tag $expectedTag)."
}

$ReleaseDir = Join-Path $ProjectRoot "dist\release\v$($mobileVer.Name)"
$BuildLocal = Join-Path $ProjectRoot "build-local.ps1"

if (-not $SkipBuild) {
    if (-not (Test-Path $BuildLocal)) {
        throw "build-local.ps1 not found at $BuildLocal. Copy it from your local setup or build manually."
    }
    Write-Step "Running build-local.ps1 -Release"
    if ($DryRun) {
        Write-Host "  (dry run - skipped)"
    } else {
        & $BuildLocal -Release -BuildDir $BuildDir
    }

    Write-Step "Building release AABs (bundleRelease)"
    $JavaHome = Find-AndroidStudioJbr
    if (-not $JavaHome) { throw "Android Studio JBR not found." }
    $env:JAVA_HOME = $JavaHome
    if ($DryRun) {
        Write-Host "  (dry run - skipped gradlew bundleRelease)"
    } else {
        Push-Location $BuildDir
        try {
            & .\gradlew.bat ":mobile:bundleRelease" ":wear:bundleRelease" --no-daemon
            if ($LASTEXITCODE -ne 0) { throw "bundleRelease failed with exit code $LASTEXITCODE" }
        } finally {
            Pop-Location
        }
    }
} else {
    Write-Step "Skipping build (-SkipBuild)"
}

if ($DryRun) {
    Write-Host "Dry run complete." -ForegroundColor Yellow
    return
}

Write-Step "Packaging release artifacts"
New-Item -ItemType Directory -Force -Path $ReleaseDir | Out-Null

$unsignedMobileApk = Find-Artifact -BaseDir $BuildDir -Module "mobile" -Kind "apk"
$unsignedWearApk = Find-Artifact -BaseDir $BuildDir -Module "wear" -Kind "apk"
$mobileAab = Find-Artifact -BaseDir $BuildDir -Module "mobile" -Kind "aab"
$wearAab = Find-Artifact -BaseDir $BuildDir -Module "wear" -Kind "aab"

$outMobileApk = Join-Path $ReleaseDir "ShareMyThing-mobile-release.apk"
$outWearApk = Join-Path $ReleaseDir "ShareMyThing-wear-release.apk"
$outMobileAab = Join-Path $ReleaseDir "ShareMyThing-mobile-release.aab"
$outWearAab = Join-Path $ReleaseDir "ShareMyThing-wear-release.aab"

$keystoreProps = Join-Path $ProjectRoot "keystore.properties"
$apksigner = Find-Apksigner

if (Test-Path $keystoreProps) {
    Write-Host "  Signing with keystore.properties (upload/release key)"
    $props = @{}
    Get-Content $keystoreProps | Where-Object { $_ -match '=' -and $_ -notmatch '^\s*#' } | ForEach-Object {
        $k, $v = $_ -split '=', 2
        $props[$k.Trim()] = $v.Trim()
    }
    $ks = Join-Path $ProjectRoot $props.storeFile
    Sign-Apk -Apksigner $apksigner -InputApk $unsignedMobileApk -OutputApk $outMobileApk -Keystore $ks `
        -StorePass $props.storePassword -KeyPass $props.keyPassword -KeyAlias $props.keyAlias
    Sign-Apk -Apksigner $apksigner -InputApk $unsignedWearApk -OutputApk $outWearApk -Keystore $ks `
        -StorePass $props.storePassword -KeyPass $props.keyPassword -KeyAlias $props.keyAlias
} else {
    Write-Warning "keystore.properties not found - signing with debug keystore (Play Store will reject; OK for GitHub/sideload testing)."
    $debugKs = Join-Path $env:USERPROFILE ".android\debug.keystore"
    Sign-Apk -Apksigner $apksigner -InputApk $unsignedMobileApk -OutputApk $outMobileApk -Keystore $debugKs `
        -StorePass "android" -KeyPass "android" -KeyAlias "androiddebugkey"
    Sign-Apk -Apksigner $apksigner -InputApk $unsignedWearApk -OutputApk $outWearApk -Keystore $debugKs `
        -StorePass "android" -KeyPass "android" -KeyAlias "androiddebugkey"
}

Copy-Item $mobileAab $outMobileAab -Force
Copy-Item $wearAab $outWearAab -Force

$outMobileNativeSymbols = Join-Path $ReleaseDir "ShareMyThing-mobile-native-debug-symbols.zip"
$outWearNativeSymbols = Join-Path $ReleaseDir "ShareMyThing-wear-native-debug-symbols.zip"
try {
    Copy-Item (Find-Artifact -BaseDir $BuildDir -Module "mobile" -Kind "native-debug-symbols") $outMobileNativeSymbols -Force
    Write-Host "  Native debug symbols: ShareMyThing-mobile-native-debug-symbols.zip"
} catch {
    Write-Warning "Mobile native debug symbols not found (rebuild after adding ndk.debugSymbolLevel)."
}
try {
    Copy-Item (Find-Artifact -BaseDir $BuildDir -Module "wear" -Kind "native-debug-symbols") $outWearNativeSymbols -Force
    Write-Host "  Native debug symbols: ShareMyThing-wear-native-debug-symbols.zip"
} catch {
    Write-Warning "Wear native debug symbols not found (rebuild after adding ndk.debugSymbolLevel)."
}

# AABs are signed by Gradle when keystore.properties exists; otherwise unsigned
if (-not (Test-Path $keystoreProps)) {
    Write-Warning "AAB files may be unsigned - configure keystore.properties before Play upload."
}

$checksumFile = Join-Path $ReleaseDir "SHA256SUMS.txt"
Get-ChildItem $ReleaseDir -File | Where-Object { $_.Name -ne "SHA256SUMS.txt" } | ForEach-Object {
    $hash = (Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLower()
    "$hash  $($_.Name)"
} | Set-Content $checksumFile -Encoding ASCII

Write-Host ""
Write-Host "Release packaged:" -ForegroundColor Green
Write-Host "  $ReleaseDir"
Get-ChildItem $ReleaseDir | Format-Table Name, @{ N = 'MB'; E = { [math]::Round($_.Length / 1MB, 2) } }

if ($GitHub) {
    Write-Step "Publishing GitHub Release $VersionTag"
    $gh = Get-Command gh -ErrorAction SilentlyContinue
    if (-not $gh) { throw "GitHub CLI (gh) not found. Install from https://cli.github.com/" }

    Push-Location $ProjectRoot
    try {
        $tagExists = git rev-parse "$VersionTag^{commit}" 2>$null
        if (-not $tagExists) {
            throw "Git tag $VersionTag not found locally. Create and push it first: git tag $VersionTag && git push origin $VersionTag"
        }

        $assets = Get-ChildItem $ReleaseDir -File | ForEach-Object { $_.FullName }
        $notesArg = @("--generate-notes")

        $existing = gh release view $VersionTag 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  Release exists - uploading assets"
            gh release upload $VersionTag @assets --clobber
        } else {
            gh release create $VersionTag @assets @notesArg --title "Share My Thing $($mobileVer.Name)"
        }
        if ($LASTEXITCODE -ne 0) { throw "gh release failed" }
        $url = gh release view $VersionTag --json url -q .url
        Write-Host "GitHub Release: $url" -ForegroundColor Green
    } finally {
        Pop-Location
    }
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  Play Store (general): upload ShareMyThing-mobile-release.aab"
Write-Host "  Play Store (general): upload ShareMyThing-mobile-native-debug-symbols.zip (App bundle explorer > Downloads)"
Write-Host "  Play Store (Wear OS): upload ShareMyThing-wear-release.apk"
