param(
  [Parameter(Mandatory = $true)]
  [string]$ServiceName,

  [string]$ArtifactId,

  [string]$WebAppName,

  [string]$BasePackage = "cn.skylark",

  [int]$BackendPort = 18080,

  [int]$FrontendPort = 9530,

  [string]$Title = ""
)

$ErrorActionPreference = "Stop"

$toolsDir = $PSScriptRoot
$repoRoot = Resolve-Path (Join-Path $toolsDir "..")

if ([string]::IsNullOrWhiteSpace($WebAppName)) {
  $WebAppName = "$ServiceName-web"
}

$svcScript = Join-Path $toolsDir "new-service.ps1"
$feScript = Join-Path $toolsDir "new-frontend.ps1"

if (!(Test-Path $svcScript)) { throw "Missing: $svcScript" }
if (!(Test-Path $feScript)) { throw "Missing: $feScript" }

$svcArgs = @{
  ServiceName = $ServiceName
  Port        = $BackendPort
  BasePackage = $BasePackage
  WebAppName  = $WebAppName
  FrontendPort = $FrontendPort
}
if (![string]::IsNullOrWhiteSpace($ArtifactId)) {
  $svcArgs.ArtifactId = $ArtifactId
}
if (![string]::IsNullOrWhiteSpace($Title)) {
  $svcArgs.AppDisplayName = $Title
}

Write-Host "==> Service: $ServiceName (port $BackendPort)"
& $svcScript @svcArgs

$feArgs = @{
  AppName = $WebAppName
  Port    = $FrontendPort
}
if (![string]::IsNullOrWhiteSpace($Title)) {
  $feArgs.Title = $Title
}

Write-Host "==> Web: $WebAppName (port $FrontendPort)"
& $feScript @feArgs

Write-Host ""
Write-Host "Done. Service: $(Join-Path $repoRoot "service\$ServiceName")"
Write-Host "       Web: $(Join-Path $repoRoot "web\apps\$WebAppName")"
Write-Host ""
Write-Host "Next:"
Write-Host "  mvn -q -f `"$(Join-Path $repoRoot 'service\pom.xml')`" install -pl $ServiceName -am"
Write-Host "  pnpm -C `"$(Join-Path $repoRoot 'web')`" install"
Write-Host "  pnpm -C `"$(Join-Path $repoRoot 'web')`" --filter $WebAppName run serve"
Write-Host ""
Write-Host "Note:"
Write-Host "  If docker compose is available, the service and web app are auto-started by their respective scripts:"
Write-Host "    - tools/new-service.ps1 starts backend and prints docker compose ps"
Write-Host "    - tools/new-frontend.ps1 starts frontend and prints docker compose ps"
