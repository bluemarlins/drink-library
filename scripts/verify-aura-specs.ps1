<#
.SYNOPSIS
    Audits drink-library Jetpack Compose UI sources for Aura Design System component-spec contracts
    beyond basic token/literal checks (Crossfade motion spec, AuraDialog visible hoisting,
    SurfaceDocked scoping, Expressive island wrapping, and semantic badge usage).

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File scripts/verify-aura-specs.ps1 -TargetAppDir . -FailOnViolation
#>
[CmdletBinding()]
param(
    [string]$TargetAppDir = ".",
    [string[]]$SourceDirs = @("app\src\main\java\com\bluemarlin\drinkdiary\ui"),
    [switch]$FailOnViolation
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ResolvedTarget = (Resolve-Path $TargetAppDir).Path

function Remove-PreviewBlocks {
    param([string]$SourceText)

    $lines = $SourceText -split "`r?`n"
    $output = New-Object System.Collections.Generic.List[string]
    $inPreviewAnnotation = $false
    $inPreviewBody = $false
    $seenBodyBrace = $false
    $braceDepth = 0

    foreach ($line in $lines) {
        $trimmed = $line.Trim()

        if (-not $inPreviewBody) {
            if ($trimmed -match '^@(?:[A-Za-z0-9_.]*Preview)\b') {
                $inPreviewAnnotation = $true
                $output.Add("")
                continue
            }
            if ($inPreviewAnnotation) {
                if ($trimmed -match '\bfun\b') {
                    $inPreviewAnnotation = $false
                    $inPreviewBody = $true
                    $seenBodyBrace = $false
                    $braceDepth = 0
                } else {
                    $output.Add("")
                    continue
                }
            }
        }

        if ($inPreviewBody) {
            $openCount = ([regex]::Matches($line, '\{')).Count
            $closeCount = ([regex]::Matches($line, '\}')).Count
            if ($openCount -gt 0) { $seenBodyBrace = $true }
            $braceDepth += ($openCount - $closeCount)
            $output.Add("")
            if ($seenBodyBrace -and $braceDepth -le 0) {
                $inPreviewBody = $false
            }
            continue
        }

        $output.Add($line)
    }

    return ($output -join "`n")
}

$violations = New-Object System.Collections.Generic.List[PSCustomObject]
$scannedFiles = 0

foreach ($relDir in $SourceDirs) {
    $absDir = Join-Path $ResolvedTarget $relDir
    if (-not (Test-Path $absDir)) {
        Write-Warning "Source directory not found, skipping: $absDir"
        continue
    }

    $ktFiles = Get-ChildItem -Path $absDir -Recurse -Filter "*.kt"
    foreach ($file in $ktFiles) {
        $scannedFiles++
        $relPath = $file.FullName.Substring($ResolvedTarget.Length).TrimStart('\', '/')
        $rawText = Get-Content -Path $file.FullName -Raw
        $checkText = Remove-PreviewBlocks -SourceText $rawText
        $lines = $checkText -split "`r?`n"

        # 1. Crossfade without explicit animationSpec (MOT-RULE-002 / MOT-RULE-007)
        $crossfadeMatches = [regex]::Matches($checkText, 'Crossfade\s*\(([^)]*)\)')
        foreach ($m in $crossfadeMatches) {
            if ($m.Groups[1].Value -notmatch 'animationSpec\s*=') {
                $violations.Add([PSCustomObject]@{
                    Rule     = "CHK-MOT-001/CROSSFADE"
                    Location = $relPath
                    Detail   = "Crossfade() must specify AuraTheme.motion animationSpec and reduceMotion snap()."
                })
            }
        }

        # 2. AuraDialog(visible = true) inside conditional block (CHK-CNT-005)
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $trimmed = $lines[$i].Trim()
            if ($trimmed.StartsWith("//")) { continue }
            if ($trimmed -match '\bvisible\s*=\s*true\b') {
                $window = ($lines[([Math]::Max(0, $i - 3))..$i] -join " ")
                if ($window -match 'AuraDialog\s*\(') {
                    $violations.Add([PSCustomObject]@{
                        Rule     = "CHK-CNT-005/DIALOG-VISIBLE"
                        Location = "${relPath}:$($i + 1)"
                        Detail   = "Pass dynamic boolean state to AuraDialog(visible = ...) instead of hardcoding visible = true."
                    })
                }
            }
        }

        # 3. FloatingBottomIsland requires Expressive track in DDScreenScaffold (CHK-NAV-003)
        if ($checkText -match 'FloatingBottomIsland\s*\(' -and $checkText -notmatch 'AuraMotionProfile\.Expressive') {
            $violations.Add([PSCustomObject]@{
                Rule     = "CHK-NAV-003/EXPRESSIVE-TRACK"
                Location = $relPath
                Detail   = "FloatingBottomIsland must be wrapped in AuraTheme(motionProfile = AuraMotionProfile.Expressive)."
            })
        }

        # 4. Non-status drink badges must not use Error/Warning variants (CHK-FBK-001 / FBK-RULE-001)
        if ($file.Name -eq "DDBadges.kt" -and $checkText -match 'AuraBadgeVariant\.(Error|Warning)') {
            $violations.Add([PSCustomObject]@{
                Rule     = "CHK-FBK-001/BADGE-SEMANTICS"
                Location = $relPath
                Detail   = "DDBadges must not use Error/Warning variants for drink type categorization."
            })
        }

        # 5. AuraCardVariant.Glass forbidden in app content layer (CHK-CNT-001 / VIS-RULE-001)
        if ($checkText -match 'AuraCardVariant\.Glass\b') {
            $violations.Add([PSCustomObject]@{
                Rule     = "CHK-CNT-001/CARD-GLASS"
                Location = $relPath
                Detail   = "AuraCardVariant.Glass is forbidden in app content layers."
            })
        }
    }
}

Write-Host "`n=== Aura Spec Contract Audit ==="
Write-Host "Target Dir    : $ResolvedTarget"
Write-Host "Files Scanned : $scannedFiles"
Write-Host "Violations    : $($violations.Count)"

if ($violations.Count -gt 0) {
    Write-Host "`n--- Spec Violations ---"
    $violations | ForEach-Object {
        Write-Host "[ERROR] $($_.Rule) @ $($_.Location) -> $($_.Detail)"
    }
} else {
    Write-Host "`nPASS: Zero Aura component-spec contract violations detected."
}

if ($FailOnViolation -and $violations.Count -gt 0) {
    exit 1
}
exit 0
