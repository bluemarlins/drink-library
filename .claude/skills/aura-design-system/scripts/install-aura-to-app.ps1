<#
.SYNOPSIS
    Builds the latest Aura Design System release AAR (optional) and installs the AAR
    plus the consumer `aura-design-system` agent skill kit into a target Android app repo.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File scripts/install-aura-to-app.ps1 -TargetAppDir C:\path\to\my-app
    powershell -ExecutionPolicy Bypass -File scripts/install-aura-to-app.ps1 -TargetAppDir C:\path\to\my-app -SkipBuild
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$TargetAppDir,

    [string]$AppModule = "app",

    [string]$AarFileName = "aura-design-system-2.0.0.aar",

    [switch]$SkipBuild,

    [switch]$SkipAar,

    [switch]$SkipSkill,

    [ValidateSet("both", "agents", "claude")]
    [string]$SkillTargetMode = "both"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$AuraRepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
$ResolvedTarget = (Resolve-Path $TargetAppDir).Path

if (-not (Test-Path $ResolvedTarget)) {
    throw "Target app directory does not exist: $TargetAppDir"
}

if (-not $SkipAar -and -not (Test-Path (Join-Path $AuraRepoRoot "design-system"))) {
    throw "This script builds the AAR from a clone of the aura-design-system repository, but '$AuraRepoRoot' is not one. Run it from <aura-repo>\skills\aura-design-system\scripts\, or add the JitPack dependency instead (see SKILL.md, Consumer Setup)."
}

Write-Host "Aura repo root : $AuraRepoRoot"
Write-Host "Target app dir : $ResolvedTarget"

# 1. Build & Sync AAR
if (-not $SkipAar) {
    if (-not $SkipBuild) {
        Write-Host "`n[1/2] Building :design-system:assembleRelease..."
        $gradlew = Join-Path $AuraRepoRoot "gradlew.bat"
        if (-not (Test-Path $gradlew)) {
            throw "gradlew.bat not found at $gradlew"
        }
        & $gradlew -p $AuraRepoRoot ":design-system:assembleRelease"
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle :design-system:assembleRelease failed with exit code $LASTEXITCODE"
        }
    } else {
        Write-Host "`n[1/2] Skipping Gradle build (-SkipBuild)."
    }

    $SourceAar = Join-Path $AuraRepoRoot "design-system\build\outputs\aar\design-system-release.aar"
    if (-not (Test-Path $SourceAar)) {
        throw "Built AAR not found at $SourceAar. Run without -SkipBuild first."
    }

    $LibsDir = Join-Path $ResolvedTarget "$AppModule\libs"
    if (-not (Test-Path $LibsDir)) {
        New-Item -ItemType Directory -Path $LibsDir -Force | Out-Null
    }

    $DestAar = Join-Path $LibsDir $AarFileName
    Copy-Item -Path $SourceAar -Destination $DestAar -Force
    $AarInfo = Get-Item $DestAar
    Write-Host "Synced AAR     : $DestAar ($($AarInfo.Length) bytes)"

    $AppGradle = Join-Path $ResolvedTarget "$AppModule\build.gradle.kts"
    if (Test-Path $AppGradle) {
        $gradleContent = Get-Content $AppGradle -Raw
        if ($gradleContent -notmatch [regex]::Escape($AarFileName)) {
            Write-Warning "Note: $AppGradle does not mention '$AarFileName'. Add: implementation(files(""libs/$AarFileName""))"
        }
    }
}

# 2. Install Consumer Skill Kit
if (-not $SkipSkill) {
    $SkillSource = (Resolve-Path (Join-Path $AuraRepoRoot "skills\aura-design-system")).Path
    $TargetDirs = @()
    if ($SkillTargetMode -eq "both" -or $SkillTargetMode -eq "agents") {
        $TargetDirs += (Join-Path $ResolvedTarget ".agents\skills\aura-design-system")
    }
    if ($SkillTargetMode -eq "both" -or $SkillTargetMode -eq "claude") {
        $TargetDirs += (Join-Path $ResolvedTarget ".claude\skills\aura-design-system")
    }

    Write-Host "`n[2/2] Installing consumer skill kit..."
    foreach ($DestSkill in $TargetDirs) {
        $DestParent = Split-Path -Parent $DestSkill
        if (-not (Test-Path $DestParent)) {
            New-Item -ItemType Directory -Path $DestParent -Force | Out-Null
        }
        if (Test-Path $DestSkill) {
            if ((Resolve-Path $DestSkill).Path -eq $SkillSource) {
                continue
            }
            Remove-Item -Path $DestSkill -Recurse -Force
        }
        Copy-Item -Path $SkillSource -Destination $DestSkill -Recurse -Force
        Write-Host "Installed skill: $DestSkill"
    }
}

Write-Host "`nDone. Run verify-aura-app.ps1 against $ResolvedTarget to audit Compose compliance."
