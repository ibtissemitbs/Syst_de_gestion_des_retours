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

$repoExists = gcloud.cmd artifacts repositories describe $ArtifactRepo --location $Region --format "value(name)" 2>$null
if (-not $repoExists) {
  gcloud.cmd artifacts repositories create $ArtifactRepo `
    --repository-format docker `
    --location $Region `
    --description "Images Docker de la plateforme de gestion des retours"
}

$sqlExists = gcloud.cmd sql instances describe $CloudSqlInstance --format "value(name)" 2>$null
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

$dbExists = gcloud.cmd sql databases describe $DatabaseName --instance $CloudSqlInstance --format "value(name)" 2>$null
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

$DbPassword | gcloud.cmd secrets create retours-db-password --data-file=- 2>$null
if ($LASTEXITCODE -ne 0) {
  $DbPassword | gcloud.cmd secrets versions add retours-db-password --data-file=-
}

if (-not $JwtSecret) {
  $JwtSecret = New-Base64Secret
}

$JwtSecret | gcloud.cmd secrets create retours-jwt-secret --data-file=- 2>$null
if ($LASTEXITCODE -ne 0) {
  $JwtSecret | gcloud.cmd secrets versions add retours-jwt-secret --data-file=-
}

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

$DatasourceUrl = "jdbc:mysql:///$DatabaseName`?cloudSqlInstance=$CloudSqlConnectionName&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false"

gcloud.cmd run deploy $BackendService `
  --image $BackendImage `
  --region $Region `
  --platform managed `
  --allow-unauthenticated `
  --port 8080 `
  --add-cloudsql-instances $CloudSqlConnectionName `
  --set-env-vars "SPRING_DATASOURCE_URL=$DatasourceUrl,SPRING_DATASOURCE_USERNAME=$DatabaseUser,SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver,SPRING_JWT_EXPIRATION_MS=3600000" `
  --set-secrets "SPRING_DATASOURCE_PASSWORD=retours-db-password:latest,SPRING_JWT_SECRET=retours-jwt-secret:latest"

$BackendUrl = gcloud.cmd run services describe $BackendService --region $Region --format "value(status.url)"

gcloud.cmd builds submit frontend --tag $FrontendImage

gcloud.cmd run deploy $FrontendService `
  --image $FrontendImage `
  --region $Region `
  --platform managed `
  --allow-unauthenticated `
  --port 80 `
  --set-env-vars "BACKEND_URL=$BackendUrl"

$FrontendUrl = gcloud.cmd run services describe $FrontendService --region $Region --format "value(status.url)"

Write-Host ""
Write-Host "Deploiement termine."
Write-Host "Backend:  $BackendUrl"
Write-Host "Frontend: $FrontendUrl"
