param(
  [Parameter(Mandatory = $true)]
  [string]$ServiceName,

  [string]$ArtifactId,

  [string]$BasePackage = "cn.skylark",

  [int]$Port = 18080
)

$ErrorActionPreference = "Stop"

function Fail($msg) {
  Write-Error $msg
  exit 1
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

