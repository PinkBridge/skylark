param(
  [Parameter(Mandatory = $true)]
  [string]$ServiceName,

  [string]$ArtifactId,

  [string]$BasePackage = "cn.skylark",

  [int]$Port = 18080,

  # Optional: also register an "app" record for the corresponding frontend.
  # This writes to skylark_permission.oauth_client_details + skylark_permission.sys_oauth_client_meta.
  [string]$WebAppName,

  [int]$FrontendPort = 9531,

  [string]$AppDisplayName,

  # docker-compose.yml sets: services.mysql.container_name=skylark-mysql
  [string]$MysqlContainerName = "skylark-mysql",

  [string]$MysqlRootPassword = "123456",

  [string]$PermissionDbName = "skylark_permission",

  [string]$OauthClientSecret = "112233"
)

$ErrorActionPreference = "Stop"

function Fail($msg) {
  Write-Error $msg
  exit 1
}

function Test-ContainerRunning([string]$ContainerName) {
  try {
    $status = docker inspect -f '{{.State.Status}}' $ContainerName 2>$null
    if ($LASTEXITCODE -ne 0) { return $false }
    return ($status -eq "running")
  } catch {
    return $false
  }
}

function Test-ContainerHealthyIfDefined([string]$ContainerName) {
  # If no healthcheck is defined, treat as healthy.
  try {
    $health = docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{end}}' $ContainerName 2>$null
    if ($LASTEXITCODE -ne 0) { return $false }
    if ([string]::IsNullOrWhiteSpace($health)) { return $true }
    return ($health -eq "healthy")
  } catch {
    return $false
  }
}

function Ensure-ComposeMysqlReadyOrWarn() {
  # Best-effort: detect if compose service 'mysql' is up and healthy.
  try {
    $null = Get-Command docker -ErrorAction Stop
  } catch {
    return $false
  }

  $hasComposeFile = $false
  try {
    $composePath = Join-Path $repoRoot "docker-compose.yml"
    $hasComposeFile = (Test-Path $composePath)
  } catch { }

  if (-not $hasComposeFile) {
    # Can't reliably infer service status without compose file; fallback to container checks.
    return (Test-ContainerRunning $MysqlContainerName -and (Test-ContainerHealthyIfDefined $MysqlContainerName))
  }

  try {
    $cid = docker compose ps -q mysql 2>$null
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($cid)) {
      Write-Host "Skip app registration: docker compose service 'mysql' is not started."
      Write-Host "Please start it, then re-run this script:"
      Write-Host "  docker compose up -d mysql"
      return $false
    }
  } catch {
    Write-Host "Skip app registration: failed to query docker compose service status."
    Write-Host "Please ensure services are up, then re-run:"
    Write-Host "  docker compose up -d mysql"
    return $false
  }

  if (-not (Test-ContainerRunning $MysqlContainerName)) {
    Write-Host "Skip app registration: container '$MysqlContainerName' is not running."
    Write-Host "Please start MySQL, then re-run:"
    Write-Host "  docker compose up -d mysql"
    return $false
  }

  if (-not (Test-ContainerHealthyIfDefined $MysqlContainerName)) {
    Write-Host "Skip app registration: container '$MysqlContainerName' is not healthy yet."
    Write-Host "Please wait for MySQL to become healthy, or restart it:"
    Write-Host "  docker compose up -d mysql"
    return $false
  }

  return $true
}

function Start-ComposeServicesIfPossible([string[]]$ServicesToStart) {
  if (-not $ServicesToStart -or $ServicesToStart.Count -eq 0) { return }

  try {
    $null = Get-Command docker -ErrorAction Stop
  } catch {
    Write-Host "Skip auto-start: docker not found."
    return
  }

  $composePath = Join-Path $repoRoot "docker-compose.yml"
  if (-not (Test-Path $composePath)) {
    Write-Host "Skip auto-start: docker-compose.yml not found."
    return
  }

  # Filter to services that actually exist in this compose file.
  $existing = @()
  try {
    $all = docker compose config --services 2>$null
    if ($LASTEXITCODE -eq 0 -and $all) {
      $set = @{}
      $all | ForEach-Object { $set[$_] = $true }
      foreach ($svc in $ServicesToStart) {
        if ($set.ContainsKey($svc)) { $existing += $svc }
      }
    }
  } catch {
    # If we can't query, still try to start requested services.
    $existing = $ServicesToStart
  }

  if (-not $existing -or $existing.Count -eq 0) {
    Write-Host "Skip auto-start: target services not found in compose: $($ServicesToStart -join ', ')"
    return
  }

  Write-Host "Auto-starting services: $($existing -join ', ')"
  try {
    docker compose up -d --build @existing | Out-Host
    if ($LASTEXITCODE -ne 0) {
      Write-Host "Auto-start: docker compose up failed (exit=$LASTEXITCODE). You can retry:"
      Write-Host "  docker compose up -d --build $($existing -join ' ')"
      return
    }
  } catch {
    Write-Host "Auto-start: docker compose up threw an error. You can retry:"
    Write-Host "  docker compose up -d --build $($existing -join ' ')"
    return
  }

  Write-Host "Startup result:"
  try {
    docker compose ps @existing | Out-Host
  } catch {
    # ignore
  }
}

function Replace-InFile([string]$Path, [hashtable]$Replacements) {
  if (!(Test-Path $Path)) { return }
  $content = Get-Content -LiteralPath $Path -Raw
  foreach ($k in $Replacements.Keys) {
    $content = $content.Replace($k, $Replacements[$k])
  }
  Set-Content -LiteralPath $Path -Value $content -NoNewline
}

function Ensure-NotExists([string]$Path) {
  if (Test-Path $Path) {
    Fail "Target already exists: $Path"
  }
}

$script:sqlAppUpsert = $null
function Register-OauthAppRecordIfPossible() {
  if ([string]::IsNullOrWhiteSpace($script:sqlAppUpsert)) {
    return
  }

  # Best-effort: only run if docker and target container exist.
  try {
    $null = Get-Command docker -ErrorAction Stop
  } catch {
    Write-Host "Skip app registration: docker not found. SQL to run manually:"
    Write-Host $script:sqlAppUpsert
    return
  }

  if (-not (Ensure-ComposeMysqlReadyOrWarn)) {
    Write-Host "SQL to run manually:"
    Write-Host $script:sqlAppUpsert
    return
  }

  $containerExists = $false
  try {
    $inspect = docker inspect $MysqlContainerName 2>$null
    if ($LASTEXITCODE -eq 0 -and $inspect) {
      $containerExists = $true
    }
  } catch {
    $containerExists = $false
  }

  if (-not $containerExists) {
    Write-Host "Skip app registration: container '$MysqlContainerName' not found. SQL to run manually:"
    Write-Host $script:sqlAppUpsert
    return
  }

  try {
    # Execute SQL inside container.
    docker exec $MysqlContainerName mysql -uroot "-p$MysqlRootPassword" $PermissionDbName -e $script:sqlAppUpsert | Out-Null
    if ($LASTEXITCODE -eq 0) {
      Write-Host "Registered app record in DB: $WebAppName (port=$FrontendPort)"
      return
    }
  } catch {
    # fallthrough
  }

  Write-Host "Skip app registration: failed to write DB. SQL to run manually:"
  Write-Host $script:sqlAppUpsert
}

$dockerComposePath = $null
$dockerComposeRaw = $null
function Load-DockerComposeIfExists() {
  $script:dockerComposePath = Join-Path $repoRoot "docker-compose.yml"
  if (Test-Path $script:dockerComposePath) {
    $script:dockerComposeRaw = Get-Content -LiteralPath $script:dockerComposePath -Raw
  }
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Load-DockerComposeIfExists

$templateDir = Join-Path $repoRoot "service\skylark-demo-service"
if (!(Test-Path $templateDir)) {
  Fail "Template not found: $templateDir"
}

if ([string]::IsNullOrWhiteSpace($ArtifactId)) {
  $ArtifactId = $ServiceName
}

$defaultWebAppName = "$ServiceName-web"
if ([string]::IsNullOrWhiteSpace($WebAppName)) {
  $WebAppName = $defaultWebAppName
}
if ([string]::IsNullOrWhiteSpace($AppDisplayName)) {
  $AppDisplayName = $WebAppName
}

$targetDir = Join-Path $repoRoot "service\$ServiceName"
Ensure-NotExists $targetDir

Copy-Item -Path $templateDir -Destination $targetDir -Recurse
$copiedTarget = Join-Path $targetDir "target"
if (Test-Path $copiedTarget) {
  Remove-Item -LiteralPath $copiedTarget -Recurse -Force
}

# Dockerfile: set service module name (for root-context docker builds)
Replace-InFile -Path (Join-Path $targetDir "Dockerfile") -Replacements @{
  "ARG SERVICE_MODULE=skylark-demo-service" = "ARG SERVICE_MODULE=$ServiceName"
}

# packages
$oldPkg = "cn.skylark.demo"
$newPkg = "$BasePackage.$ServiceName".Replace("-", "_")

$oldPkgPath = Join-Path $targetDir "src\main\java\cn\skylark\demo"
$newPkgPath = Join-Path $targetDir ("src\main\java\" + ($newPkg.Replace(".", "\")))

if (Test-Path $oldPkgPath) {
  New-Item -ItemType Directory -Force -Path (Split-Path -Parent $newPkgPath) | Out-Null
  Move-Item -Path $oldPkgPath -Destination $newPkgPath
  # Clean empty dirs (best-effort)
  $cnSkylarkDir = Join-Path $targetDir "src\main\java\cn\skylark"
  if (Test-Path $cnSkylarkDir) {
    Get-ChildItem -LiteralPath $cnSkylarkDir -Directory | ForEach-Object {
      if ($_.Name -eq "demo") { return }
    }
  }
}

# rename main class
$oldMain = "DemoServiceApplication"
$nameParts = $ServiceName -split '[-_]' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
$pascalName = ($nameParts | ForEach-Object { $_.Substring(0, 1).ToUpper() + $_.Substring(1) }) -join ""
$newMain = $pascalName + "Application"
$mainFile = Join-Path $newPkgPath "$oldMain.java"
if (Test-Path $mainFile) {
  Rename-Item -LiteralPath $mainFile -NewName "$newMain.java"
}

$repl = @{
  "skylark-demo-service" = $ArtifactId
  "<artifactId>skylark-demo-service</artifactId>" = "<artifactId>$ArtifactId</artifactId>"
  "<name>skylark-demo-service</name>" = "<name>$ArtifactId</name>"
  "package $oldPkg;" = "package $newPkg;"
  "SpringApplication.run($oldMain.class" = "SpringApplication.run($newMain.class"
  "public class $oldMain" = "public class $newMain"
}

Replace-InFile -Path (Join-Path $targetDir "pom.xml") -Replacements $repl

# Ensure common web DTO module is included (Ret/PageResult), for consistency with other services.
$svcPomPath = Join-Path $targetDir "pom.xml"
if (Test-Path $svcPomPath) {
  $svcPom = Get-Content -LiteralPath $svcPomPath -Raw
  if ($svcPom -notmatch "<artifactId>skylark-web-common</artifactId>") {
    $depBlock = @"

    <dependency>
      <groupId>cn.skylark</groupId>
      <artifactId>skylark-web-common</artifactId>
      <version>`${project.version}</version>
    </dependency>
"@
    $svcPom = $svcPom -replace "(?m)^\s*</dependencies>\s*$", ($depBlock + "`r`n  </dependencies>")
    Set-Content -LiteralPath $svcPomPath -Value $svcPom -NoNewline
  }
}
Replace-InFile -Path (Join-Path $targetDir "src\main\resources\application.yml") -Replacements @{
  "port: 18080" = "port: $Port"
  "name: skylark-demo-service" = "name: $ArtifactId"
}

# replace package declarations in java files
Get-ChildItem -Path (Join-Path $targetDir "src\main\java") -Recurse -Filter "*.java" | ForEach-Object {
  Replace-InFile -Path $_.FullName -Replacements @{
    "package $oldPkg." = "package $newPkg."
    "package $oldPkg;" = "package $newPkg;"
    "SpringApplication.run($oldMain.class" = "SpringApplication.run($newMain.class"
    "public class $oldMain" = "public class $newMain"
  }
}

$backendPomPath = Join-Path $repoRoot "service\pom.xml"
$backendPomRaw = Get-Content -LiteralPath $backendPomPath -Raw
$moduleTag = "    <module>$ServiceName</module>"
if ($backendPomRaw -notmatch "<module>$([regex]::Escape($ServiceName))</module>") {
  $backendPomRaw = $backendPomRaw -replace "(\r?\n)(\s*</modules>)", "`$1$moduleTag`$1`$2"
  Set-Content -LiteralPath $backendPomPath -Value $backendPomRaw -NoNewline
}

# docker-compose: add a service for this business service (compose-only deployment)
if ($dockerComposeRaw) {
  $svcKey = "  ${ServiceName}:"
  if ($dockerComposeRaw -notmatch "(?m)^\s{2}$([regex]::Escape($ServiceName))\s*:") {
    $insertBlock = @"

  ${ServiceName}:
    build:
      context: .
      dockerfile: service/${ServiceName}/Dockerfile
      args:
        MAVEN_BUILD_IMAGE: `${MAVEN_BUILD_IMAGE:-maven:3.9-eclipse-temurin-17}
        JRE_IMAGE: `${JRE_IMAGE:-eclipse-temurin:17-jre-alpine}
    container_name: ${ServiceName}
    restart: unless-stopped
    environment:
      - TZ=Asia/Shanghai
      - SERVER_PORT=80

"@
    # Insert right before root-level `volumes:` (must start at column 0)
    $dockerComposeRaw = $dockerComposeRaw -replace "(?m)^volumes:\s*$", ($insertBlock + "`r`nvolumes:")
    Set-Content -LiteralPath $dockerComposePath -Value $dockerComposeRaw -NoNewline
  }
}

Write-Host "Created service at: $targetDir"
Write-Host "Next:"
Write-Host "  mvn -q -f `"$(Join-Path $repoRoot 'service\pom.xml')`" install -pl $ServiceName -am"
Write-Host "  mvn -q -f `"$targetDir\pom.xml`" test"

# Also register the corresponding OAuth client + app meta (best-effort).
# The frontend generator uses VUE_APP_CLIENT_ID=$WebAppName and expects redirect_uri to include /home.
$redirectUris = @(
  "http://localhost:$FrontendPort/home",
  "http://127.0.0.1:$FrontendPort/home"
) -join ","

$script:sqlAppUpsert = @"
-- oauth_client_details (OAuth client)
INSERT INTO oauth_client_details(
  client_id, resource_ids, client_secret, scope, authorized_grant_types,
  web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity,
  additional_information, autoapprove
)
VALUES (
  '$WebAppName', NULL, '$OauthClientSecret', 'all',
  'password,authorization_code,refresh_token',
  '$redirectUris', NULL, 36000, 36000, NULL, 'true'
)
ON DUPLICATE KEY UPDATE
  client_secret = VALUES(client_secret),
  web_server_redirect_uri = VALUES(web_server_redirect_uri),
  scope = VALUES(scope),
  authorized_grant_types = VALUES(authorized_grant_types),
  access_token_validity = VALUES(access_token_validity),
  refresh_token_validity = VALUES(refresh_token_validity),
  autoapprove = VALUES(autoapprove);

-- sys_oauth_client_meta (display name + ui port)
INSERT INTO sys_oauth_client_meta(client_id, name, port)
VALUES ('$WebAppName', '$AppDisplayName', $FrontendPort)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  port = VALUES(port);
"@

Register-OauthAppRecordIfPossible

# Best-effort: after scaffolding, auto-start backend service and show status.
if (-not [string]::IsNullOrWhiteSpace($ServiceName)) {
  Start-ComposeServicesIfPossible -ServicesToStart @($ServiceName)
}

