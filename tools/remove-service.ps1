param(
  [Parameter(Mandatory = $true)]
  [string]$ServiceName,

  [bool]$RemoveCompose = $true
)

$ErrorActionPreference = "Stop"

function Fail($msg) {
  Write-Error $msg
  exit 1
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$svcDir = Join-Path $repoRoot "service\$ServiceName"

if (!(Test-Path $svcDir)) {
  Fail "Service directory not found: $svcDir"
}

# 1) Remove directory
Remove-Item -LiteralPath $svcDir -Recurse -Force

# 2) Remove from service/pom.xml modules
$servicePomPath = Join-Path $repoRoot "service\pom.xml"
if (Test-Path $servicePomPath) {
  $pom = Get-Content -LiteralPath $servicePomPath -Raw
  $moduleLine = "    <module>$ServiceName</module>"
  # Remove exact line (and its trailing newline if present)
  $escaped = [regex]::Escape($moduleLine)
  $pom2 = [regex]::Replace($pom, "(\r?\n)$escaped(\r?\n)?", "`$1")
  # Fallback: remove any <module>ServiceName</module> line with indentation
  $pom2 = $pom2 -replace "(?m)^\s*<module>$([regex]::Escape($ServiceName))</module>\s*\r?\n", ""
  Set-Content -LiteralPath $servicePomPath -Value $pom2 -NoNewline
}

# 3) Remove from docker-compose.yml
if ($RemoveCompose) {
  $composePath = Join-Path $repoRoot "docker-compose.yml"
  if (Test-Path $composePath) {
    $compose = Get-Content -LiteralPath $composePath -Raw
    # Remove a top-level service block under `services:` with two-space indent.
    # Matches from "  ServiceName:" to the next "  <other>:" or to "volumes:" at column 0.
    $pattern = "(?ms)^\s{2}$([regex]::Escape($ServiceName))\s*:\s*\r?\n(.*?)(?=^\s{2}[A-Za-z0-9_.-]+\s*:\s*\r?\n|^volumes:\s*\r?\n|\z)"
    $compose2 = [regex]::Replace($compose, $pattern, "")
    # Clean up excessive blank lines
    $compose2 = $compose2 -replace "(\r?\n){4,}", "`r`n`r`n`r`n"
    Set-Content -LiteralPath $composePath -Value $compose2 -NoNewline
  }
}

Write-Host "Removed service: $ServiceName"
