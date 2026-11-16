<#
.SYNOPSIS
    Audits Kotlin Jetpack Compose source files in an Aura consumer app for EVAL-002,
    MOT-RULE-002, and VIS-RULE-001/006 compliance.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File scripts/verify-aura-app.ps1 -TargetAppDir C:\path\to\my-app
    powershell -ExecutionPolicy Bypass -File scripts/verify-aura-app.ps1 -TargetAppDir C:\path\to\my-app -FailOnViolation
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$TargetAppDir,

    [string[]]$SourceDirs = @("app\src\main"),

    [switch]$IncludePreviews,

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
$warnings = New-Object System.Collections.Generic.List[PSCustomObject]
$scannedFiles = 0

foreach ($relDir in $SourceDirs) {
    $absDir = Join-Path $ResolvedTarget $relDir
    if (-not (Test-Path $absDir)) {
        Write-Warning "Source directory not found, skipping: $absDir"
        continue
    }

    $ktFiles = Get-ChildItem -Path $absDir -Recurse -Filter "*.kt" | Where-Object {
        $_.FullName -notmatch '[\\/](build|\.gradle)[\\/]'
    }

    foreach ($file in $ktFiles) {
        $scannedFiles++
        $relPath = $file.FullName.Substring($ResolvedTarget.Length).TrimStart('\', '/')
        $rawText = Get-Content -Path $file.FullName -Raw
        $checkText = if ($IncludePreviews) { $rawText } else { Remove-PreviewBlocks -SourceText $rawText }
        $lines = $checkText -split "`r?`n"

        $glassCallCount = 0
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            $lineNo = $i + 1
            $trimmed = $line.Trim()

            if ($trimmed.StartsWith("//") -or $trimmed.StartsWith("*") -or $trimmed.StartsWith("/*")) {
                continue
            }

            # Strip inline comments before matching code patterns
            $codeOnly = $line -replace '//.*$', ''

            if ($codeOnly -match 'androidx\.compose\.material3\.') {
                $violations.Add([PSCustomObject]@{
                    Severity = "ERROR"
                    Rule     = "EVAL-002/M3"
                    Location = "${relPath}:${lineNo}"
                    Detail   = $trimmed
                })
            }

            if ($codeOnly -match 'Color\(\s*0x[0-9A-Fa-f]+') {
                $violations.Add([PSCustomObject]@{
                    Severity = "ERROR"
                    Rule     = "EVAL-002/COLOR"
                    Location = "${relPath}:${lineNo}"
                    Detail   = $trimmed
                })
            }

            if ($codeOnly -match '(?<![A-Za-z0-9_.])\d+(?:\.\d+)?\.sp\b') {
                $violations.Add([PSCustomObject]@{
                    Severity = "ERROR"
                    Rule     = "EVAL-002/TYPO"
                    Location = "${relPath}:${lineNo}"
                    Detail   = $trimmed
                })
            }

            if ($codeOnly -match '\btween\s*\([^)]*\d' -or
                $codeOnly -match '\bspring\s*\([^)]*(dampingRatio|stiffness)\s*=\s*[0-9.]') {
                $violations.Add([PSCustomObject]@{
                    Severity = "ERROR"
                    Rule     = "MOT-RULE-002"
                    Location = "${relPath}:${lineNo}"
                    Detail   = $trimmed
                })
            }

            if ($codeOnly -match '\bGlassSurface\s*\(' -or $codeOnly -match 'AuraCardVariant\.Glass\b') {
                $glassCallCount++
                if ($codeOnly -match 'AuraCardVariant\.Glass\b') {
                    $warnings.Add([PSCustomObject]@{
                        Severity = "WARN"
                        Rule     = "VIS-RULE-001"
                        Location = "${relPath}:${lineNo}"
                        Detail   = "Verify AuraCardVariant.Glass is not inside a scrollable content list: $trimmed"
                    })
                }
            }
        }

        if ($glassCallCount -gt 3) {
            $warnings.Add([PSCustomObject]@{
                Severity = "WARN"
                Rule     = "VIS-RULE-006"
                Location = $relPath
                Detail   = "Found $glassCallCount GlassSurface/Glass references in one file; verify <= 3 live blur panels per visible viewport."
            })
        }
    }
}

Write-Host "`n=== Aura Consumer App Audit ==="
Write-Host "Target Dir    : $ResolvedTarget"
Write-Host "Files Scanned : $scannedFiles"
Write-Host "Errors        : $($violations.Count)"
Write-Host "Warnings      : $($warnings.Count)"

if ($violations.Count -gt 0) {
    Write-Host "`n--- Violations ---"
    $violations | ForEach-Object {
        Write-Host "[$($_.Severity)] $($_.Rule) @ $($_.Location) -> $($_.Detail)"
    }
}

if ($warnings.Count -gt 0) {
    Write-Host "`n--- Warnings ---"
    $warnings | ForEach-Object {
        Write-Host "[$($_.Severity)] $($_.Rule) @ $($_.Location) -> $($_.Detail)"
    }
}

if ($violations.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "`nPASS: Zero Aura compliance violations or warnings detected."
}

if ($FailOnViolation -and $violations.Count -gt 0) {
    exit 1
}
exit 0
