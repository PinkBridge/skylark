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

function Stop-AndRemoveComposeServiceIfExists([string]$Svc) {
  if ([string]::IsNullOrWhiteSpace($Svc)) { return }

  try {
    $null = Get-Command docker -ErrorAction Stop
  } catch {
    return
  }

  $composePath = Join-Path $repoRoot "docker-compose.yml"
  if (Test-Path $composePath) {
    $exists = $false
    try {
      $services = docker compose config --services 2>$null
      if ($LASTEXITCODE -eq 0 -and $services) {
        foreach ($s in $services) { if ($s -eq $Svc) { $exists = $true; break } }
      }
    } catch { $exists = $true }

    if ($exists) {
      try { docker compose stop $Svc | Out-Host } catch { }
      try { docker compose rm -f $Svc | Out-Host } catch { }
    }
  }

  # Fallback: remove container with the same name if it exists.
  try {
    $null = docker inspect $Svc 2>$null
    if ($LASTEXITCODE -eq 0) {
      docker rm -f $Svc | Out-Host
    }
  } catch { }
}

if (!(Test-Path $appDir)) {
  Fail "Web app directory not found: $appDir"
}

# 0) Stop & remove container/service (best-effort)
Stop-AndRemoveComposeServiceIfExists -Svc $AppName

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
