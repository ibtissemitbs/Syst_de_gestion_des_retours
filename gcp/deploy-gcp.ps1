param(
  [string]$ProjectId = "systemgestiondesretours",
  [string]$Region = "europe-west1",
  [string]$ArtifactRepo = "retours",
  [string]$CloudSqlInstance = "retours-mysql",
  [string]$DatabaseName = "retoursdb",
  [string]$DatabaseUser = "retours_user",
  [string]$RootPassword = "",
  [string]$DbPassword = "",
  [string]$JwtSecret = "",
  [switch]$CreateBillableResources
)

$ErrorActionPreference = "Stop"

if (-not $CreateBillableResources) {
  throw "Cloud SQL est une ressource payante. Relancez avec -CreateBillableResources pour confirmer."
}

function New-RandomPassword {
  $chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%*-_"
  -join (1..40 | ForEach-Object { $chars[(Get-Random -Minimum 0 -Maximum $chars.Length)] })
}

function New-Base64Secret {
  $bytes = New-Object byte[] 64
  $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
  $rng.GetBytes($bytes)
  [Convert]::ToBase64String($bytes)
}

function Test-GcloudCommand {
  param([scriptblock]$Command)

  $previousPreference = $ErrorActionPreference
  $ErrorActionPreference = "Continue"
  & $Command *> $null
  $exitCode = $LASTEXITCODE
  $ErrorActionPreference = $previousPreference

  return $exitCode -eq 0
}

function Set-SecretValue {
  param(
    [string]$Name,
    [string]$Value
  )

  $secretFile = Join-Path $env:TEMP "$Name.txt"
  Set-Content -Path $secretFile -Value $Value -NoNewline -Encoding utf8

  $secretExists = Test-GcloudCommand { gcloud.cmd secrets describe $Name }
  if (-not $secretExists) {
    gcloud.cmd secrets create $Name --data-file $secretFile
  }
  else {
    gcloud.cmd secrets versions add $Name --data-file $secretFile
  }

  Remove-Item $secretFile -ErrorAction SilentlyContinue
}

$BackendService = "retours-backend"
$FrontendService = "retours-frontend"
$BackendImage = "$Region-docker.pkg.dev/$ProjectId/$ArtifactRepo/$BackendService`:latest"
$FrontendImage = "$Region-docker.pkg.dev/$ProjectId/$ArtifactRepo/$FrontendService`:latest"
$CloudSqlConnectionName = "$ProjectId`:$Region`:$CloudSqlInstance"

Write-Host "Projet GCP: $ProjectId"
Write-Host "Region: $Region"

gcloud.cmd config set project $ProjectId

gcloud.cmd services enable `
  run.googleapis.com `
  artifactregistry.googleapis.com `
  cloudbuild.googleapis.com `
  sqladmin.googleapis.com `
  secretmanager.googleapis.com

$repoExists = Test-GcloudCommand { gcloud.cmd artifacts repositories describe $ArtifactRepo --location $Region }
if (-not $repoExists) {
  gcloud.cmd artifacts repositories create $ArtifactRepo `
    --repository-format docker `
    --location $Region `
    --description "Images Docker de la plateforme de gestion des retours"
}

$sqlExists = Test-GcloudCommand { gcloud.cmd sql instances describe $CloudSqlInstance }
if (-not $sqlExists) {
  if (-not $RootPassword) {
    $RootPassword = New-RandomPassword
  }

  gcloud.cmd sql instances create $CloudSqlInstance `
    --database-version MYSQL_8_0 `
    --tier db-f1-micro `
    --region $Region `
    --storage-size 10 `
    --root-password $RootPassword
}

$dbExists = Test-GcloudCommand { gcloud.cmd sql databases describe $DatabaseName --instance $CloudSqlInstance }
if (-not $dbExists) {
  gcloud.cmd sql databases create $DatabaseName --instance $CloudSqlInstance
}

if (-not $DbPassword) {
  $DbPassword = New-RandomPassword
}

$dbUserExists = gcloud.cmd sql users list --instance $CloudSqlInstance --format "value(name)" | Select-String -SimpleMatch $DatabaseUser
if (-not $dbUserExists) {
  gcloud.cmd sql users create $DatabaseUser --instance $CloudSqlInstance --password $DbPassword
}
else {
  gcloud.cmd sql users set-password $DatabaseUser --instance $CloudSqlInstance --password $DbPassword
}

Set-SecretValue -Name "retours-db-password" -Value $DbPassword

if (-not $JwtSecret) {
  $JwtSecret = New-Base64Secret
}

Set-SecretValue -Name "retours-jwt-secret" -Value $JwtSecret

$ProjectNumber = gcloud.cmd projects describe $ProjectId --format "value(projectNumber)"
$CloudRunServiceAccount = "$ProjectNumber-compute@developer.gserviceaccount.com"

gcloud.cmd projects add-iam-policy-binding $ProjectId `
  --member "serviceAccount:$CloudRunServiceAccount" `
  --role "roles/cloudsql.client"

gcloud.cmd secrets add-iam-policy-binding retours-db-password `
  --member "serviceAccount:$CloudRunServiceAccount" `
  --role "roles/secretmanager.secretAccessor"

gcloud.cmd secrets add-iam-policy-binding retours-jwt-secret `
  --member "serviceAccount:$CloudRunServiceAccount" `
  --role "roles/secretmanager.secretAccessor"

gcloud.cmd builds submit . --tag $BackendImage

$CloudSqlServiceAccount = gcloud.cmd sql instances describe $CloudSqlInstance --format "value(serviceAccountEmailAddress)"
$ImportBucket = "$ProjectId`_cloudbuild"

gcloud.cmd storage buckets add-iam-policy-binding "gs://$ImportBucket" `
  --member "serviceAccount:$CloudSqlServiceAccount" `
  --role "roles/storage.objectViewer"

$GrantSqlFile = Join-Path $env:TEMP "retours-grant-user.sql"
@(
  "CREATE DATABASE IF NOT EXISTS $DatabaseName;"
  "GRANT ALL PRIVILEGES ON $DatabaseName.* TO '$DatabaseUser'@'%';"
  "FLUSH PRIVILEGES;"
) | Set-Content -Path $GrantSqlFile -Encoding utf8

gcloud.cmd storage cp $GrantSqlFile "gs://$ImportBucket/retours-grant-user.sql"
gcloud.cmd sql import sql $CloudSqlInstance "gs://$ImportBucket/retours-grant-user.sql" --quiet
Remove-Item $GrantSqlFile -ErrorAction SilentlyContinue

$DatasourceUrl = 'jdbc:mysql:///' + $DatabaseName + '?cloudSqlInstance=' + $CloudSqlConnectionName + '&socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlRefreshStrategy=lazy&useSSL=false'
$BackendEnvFile = Join-Path $env:TEMP 'retours-backend-env.yaml'

@(
  'SPRING_DATASOURCE_URL: "' + $DatasourceUrl + '"'
  'SPRING_DATASOURCE_USERNAME: "' + $DatabaseUser + '"'
  'SPRING_DATASOURCE_DRIVER_CLASS_NAME: "com.mysql.cj.jdbc.Driver"'
  'SPRING_JWT_EXPIRATION_MS: "3600000"'
) | Set-Content -Path $BackendEnvFile -Encoding utf8

gcloud.cmd run deploy $BackendService `
  --image $BackendImage `
  --region $Region `
  --platform managed `
  --allow-unauthenticated `
  --port 8080 `
  --execution-environment gen2 `
  --timeout 300 `
  --add-cloudsql-instances $CloudSqlConnectionName `
  --env-vars-file $BackendEnvFile `
  --set-secrets "SPRING_DATASOURCE_PASSWORD=retours-db-password:latest,SPRING_JWT_SECRET=retours-jwt-secret:latest"

Remove-Item $BackendEnvFile -ErrorAction SilentlyContinue

$BackendUrl = gcloud.cmd run services describe $BackendService --region $Region --format "value(status.url)"
$BackendHost = $BackendUrl -replace "^https://", ""

gcloud.cmd builds submit frontend --tag $FrontendImage

gcloud.cmd run deploy $FrontendService `
  --image $FrontendImage `
  --region $Region `
  --platform managed `
  --allow-unauthenticated `
  --port 80 `
  --set-env-vars "BACKEND_URL=$BackendUrl,BACKEND_HOST=$BackendHost"

$FrontendUrl = gcloud.cmd run services describe $FrontendService --region $Region --format "value(status.url)"

Write-Host ""
Write-Host "Deploiement termine."
Write-Host "Backend:  $BackendUrl"
Write-Host "Frontend: $FrontendUrl"
