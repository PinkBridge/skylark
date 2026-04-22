param(
  [Parameter(Mandatory = $true)]
  [string]$ServiceName,

  [string]$WebAppName,

  [switch]$KeepCompose
)

$ErrorActionPreference = "Stop"

$toolsDir = $PSScriptRoot

if ([string]::IsNullOrWhiteSpace($WebAppName)) {
  $WebAppName = "$ServiceName-web"
}

$rmSvc = Join-Path $toolsDir "remove-service.ps1"
$rmWeb = Join-Path $toolsDir "remove-web.ps1"

if (!(Test-Path $rmSvc)) { throw "Missing: $rmSvc" }
if (!(Test-Path $rmWeb)) { throw "Missing: $rmWeb" }

$removeCompose = -not $KeepCompose

Write-Host "==> Remove service: $ServiceName"
& $rmSvc -ServiceName $ServiceName -RemoveCompose $removeCompose

Write-Host "==> Remove web: $WebAppName"
& $rmWeb -AppName $WebAppName -RemoveCompose $removeCompose

Write-Host "Done."
