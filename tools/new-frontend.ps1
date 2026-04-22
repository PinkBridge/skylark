param(
  [Parameter(Mandatory = $true)]
  [string]$AppName,

  [int]$Port = 9530,

  [string]$Title = ""
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

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$templateDir = Join-Path $repoRoot "web\apps\skylark-demo-web"
$targetDir = Join-Path $repoRoot "web\apps\$AppName"

$dockerComposePath = Join-Path $repoRoot "docker-compose.yml"
$dockerComposeRaw = $null
if (Test-Path $dockerComposePath) {
  $dockerComposeRaw = Get-Content -LiteralPath $dockerComposePath -Raw
}

if (!(Test-Path $templateDir)) {
  Fail "Template not found: $templateDir"
}
Ensure-NotExists $targetDir

Copy-Item -Path $templateDir -Destination $targetDir -Recurse

if ([string]::IsNullOrWhiteSpace($Title)) {
  $Title = $AppName
}

$envDev = Join-Path $targetDir ".env.development"
if (!(Test-Path $envDev)) {
  Set-Content -LiteralPath $envDev -Value @"
VUE_APP_SYSTEM_TITLE=$Title
VUE_APP_CLIENT_ID=$AppName
VUE_APP_MENU_APP=$AppName
"@ -NoNewline
}
$envProd = Join-Path $targetDir ".env.production"
if (!(Test-Path $envProd)) {
  Set-Content -LiteralPath $envProd -Value @"
VUE_APP_SYSTEM_TITLE=$Title
VUE_APP_CLIENT_ID=$AppName
VUE_APP_MENU_APP=$AppName
"@ -NoNewline
}

Replace-InFile -Path (Join-Path $targetDir "package.json") -Replacements @{
  '"name": "skylark-demo-web"' = "`"name`": `"$AppName`""
}

Replace-InFile -Path (Join-Path $targetDir "vue.config.js") -Replacements @{
  "port: 9529" = "port: $Port"
  "title: 'Skylark Demo Web'" = "title: '$Title'"
}

Replace-InFile -Path (Join-Path $targetDir "README.md") -Replacements @{
  "# skylark-demo-web" = "# $AppName"
  "skylark-demo-web" = $AppName
}

# Home dashboard card title (shell layout: Welcome/Home come from @skylark/admin-shell)
Replace-InFile -Path (Join-Path $targetDir "src\views\Dashboard.vue") -Replacements @{
  "<span>Dashboard</span>" = "<span>$Title</span>"
}

# env files: client id/secret and title
Replace-InFile -Path (Join-Path $targetDir ".env.development") -Replacements @{
  "VUE_APP_SYSTEM_TITLE=Skylark Demo Web" = "VUE_APP_SYSTEM_TITLE=$Title"
  "VUE_APP_CLIENT_ID=permission-web" = "VUE_APP_CLIENT_ID=$AppName"
  "VUE_APP_MENU_APP=permission-web" = "VUE_APP_MENU_APP=$AppName"
}
Replace-InFile -Path (Join-Path $targetDir ".env.production") -Replacements @{
  "VUE_APP_SYSTEM_TITLE=Skylark Demo Web" = "VUE_APP_SYSTEM_TITLE=$Title"
  "VUE_APP_CLIENT_ID=permission-web" = "VUE_APP_CLIENT_ID=$AppName"
  "VUE_APP_MENU_APP=permission-web" = "VUE_APP_MENU_APP=$AppName"
}

# Dockerfile: set pnpm filter to new app name
Replace-InFile -Path (Join-Path $targetDir "Dockerfile") -Replacements @{
  "COPY web/apps/skylark-demo-web/package.json ./apps/skylark-demo-web/" = "COPY web/apps/$AppName/package.json ./apps/$AppName/"
  "COPY web/apps/skylark-demo-web ./apps/skylark-demo-web" = "COPY web/apps/$AppName ./apps/$AppName"
  "pnpm install --frozen-lockfile --store-dir /pnpm/store --filter `"skylark-demo-web...`"" = "pnpm install --frozen-lockfile --store-dir /pnpm/store --filter `"$AppName...`""
  "ARG PNPM_FILTER=skylark-demo-web" = "ARG PNPM_FILTER=$AppName"
  "COPY web/apps/skylark-demo-web/docker/nginx.default.conf" = "COPY web/apps/$AppName/docker/nginx.default.conf"
  "COPY --from=builder /skylark-frontend/apps/skylark-demo-web/dist" = "COPY --from=builder /skylark-frontend/apps/$AppName/dist"
}

# Ensure new workspace package manifests are included for BuildKit cache (when adding new packages under web/packages).
Replace-InFile -Path (Join-Path $targetDir "Dockerfile") -Replacements @{
  "COPY web/packages/tenant-client/package.json ./packages/tenant-client/" = @"
COPY web/packages/tenant-client/package.json ./packages/tenant-client/
COPY web/packages/i18n-client/package.json ./packages/i18n-client/
"@.TrimEnd()
}

# docker-compose: add a service for this frontend (compose-only deployment)
if ($dockerComposeRaw) {
  if ($dockerComposeRaw -notmatch "(?m)^\s{2}$([regex]::Escape($AppName))\s*:") {
    $insertBlock = @"

  ${AppName}:
    build:
      context: .
      dockerfile: web/apps/${AppName}/Dockerfile
    container_name: ${AppName}
    restart: unless-stopped
    ports:
      - `"$Port`:80`"
    depends_on:
      gateway:
        condition: service_started

"@
    $dockerComposeRaw = $dockerComposeRaw -replace "(?m)^volumes:\s*$", ($insertBlock + "`r`nvolumes:")
    Set-Content -LiteralPath $dockerComposePath -Value $dockerComposeRaw -NoNewline
  }
}

Write-Host "Created frontend app at: $targetDir"
Write-Host "Template includes @skylark/admin-shell (permission-app style layout)."
Write-Host "Next:"
Write-Host "  Register OAuth client in permission (redirect URIs for your dev port and /home)."
Write-Host "  pnpm -C `"$repoRoot\web`" install"
Write-Host "  pnpm -C `"$repoRoot\web`" --filter $AppName run serve"
