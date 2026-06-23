#Requires -Version 5.1
<#
.SYNOPSIS
  Bump visible version (versionName) and Play versionCodes for mobile + wear.

.DESCRIPTION
  - versionName: set on both modules (you choose key.features.fixes).
  - mobile versionCode: current + 1.
  - wear versionCode: major*1000 + minor*10 + patch from versionName (must differ from mobile).

  See docs/VERSIONING.md.

.EXAMPLE
  .\scripts\bump-version.ps1 -VersionName 2.1.0
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $VersionName
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

if ($VersionName -notmatch '^(\d+)\.(\d+)\.(\d+)$') {
    throw "VersionName must be key.features.fixes format, e.g. 2.1.0 (got '$VersionName')"
}
$major = [int]$Matches[1]
$minor = [int]$Matches[2]
$patch = [int]$Matches[3]

function Read-GradleVersion {
    param([string] $GradleFile)
    $text = Get-Content $GradleFile -Raw
    $code = if ($text -match 'versionCode\s*=\s*(\d+)') { [int]$Matches[1] } else { throw "versionCode not found in $GradleFile" }
    $name = if ($text -match 'versionName\s*=\s*"([^"]+)"') { $Matches[1] } else { throw "versionName not found in $GradleFile" }
    return @{ Code = $code; Name = $name; Text = $text; Path = $GradleFile }
}

function Set-GradleVersion {
    param(
        [string] $GradleFile,
        [int] $VersionCode,
        [string] $VersionName
    )
    $text = Get-Content $GradleFile -Raw
    $text = [regex]::Replace($text, 'versionCode\s*=\s*\d+', "versionCode = $VersionCode", 1)
    $text = [regex]::Replace($text, 'versionName\s*=\s*"[^"]*"', "versionName = `"$VersionName`"", 1)
    Set-Content -Path $GradleFile -Value $text -NoNewline
}

$mobile = Read-GradleVersion (Join-Path $ProjectRoot "mobile\build.gradle.kts")
$wear = Read-GradleVersion (Join-Path $ProjectRoot "wear\build.gradle.kts")

$newMobileCode = $mobile.Code + 1
$newWearCode = ($major * 1000) + ($minor * 10) + $patch

if ($newWearCode -le $wear.Code) {
    $newWearCode = $wear.Code + 1
}
if ($newMobileCode -eq $newWearCode) {
    $newWearCode++
}

Write-Host "Visible version: $VersionName"
Write-Host "  mobile: $($mobile.Name)/$($mobile.Code) -> $VersionName/$newMobileCode"
Write-Host "  wear:   $($wear.Name)/$($wear.Code) -> $VersionName/$newWearCode"

Set-GradleVersion -GradleFile $mobile.Path -VersionCode $newMobileCode -VersionName $VersionName
Set-GradleVersion -GradleFile $wear.Path -VersionCode $newWearCode -VersionName $VersionName

Write-Host ""
Write-Host "Updated. Next:" -ForegroundColor Cyan
Write-Host "  .\scripts\release.ps1 -VersionTag v$VersionName"
Write-Host "  git commit, push, tag v$VersionName, GitHub Release"
