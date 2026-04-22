param(
  [Parameter(Mandatory = $true)]
  [string]$AppName,

  [bool]$RemoveCompose = $true
)

$ErrorActionPreference = "Stop"

function Fail($msg) {
  Write-Error $msg
  exit 1
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$appDir = Join-Path $repoRoot "web\apps\$AppName"

if (!(Test-Path $appDir)) {
  Fail "Web app directory not found: $appDir"
}

# 1) Remove directory
Remove-Item -LiteralPath $appDir -Recurse -Force

# 2) Remove from docker-compose.yml
if ($RemoveCompose) {
  $composePath = Join-Path $repoRoot "docker-compose.yml"
  if (Test-Path $composePath) {
    $compose = Get-Content -LiteralPath $composePath -Raw
    $pattern = "(?ms)^\s{2}$([regex]::Escape($AppName))\s*:\s*\r?\n(.*?)(?=^\s{2}[A-Za-z0-9_.-]+\s*:\s*\r?\n|^volumes:\s*\r?\n|\z)"
    $compose2 = [regex]::Replace($compose, $pattern, "")
    $compose2 = $compose2 -replace "(\r?\n){4,}", "`r`n`r`n`r`n"
    Set-Content -LiteralPath $composePath -Value $compose2 -NoNewline
  }
}

Write-Host "Removed web app: $AppName"
