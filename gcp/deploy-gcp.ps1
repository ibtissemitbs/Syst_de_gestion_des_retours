param(
  [string]$ProjectId = "systemgestiondesretours",
  [string]$Region = "europe-west1",
  [string]$ArtifactRepo = "retours",
  [string]$CloudSqlInstance = "retours-mysql",
  [string]$DatabaseName = "retoursdb",
  [string]$DatabaseUser = "retours_user"
)

$ErrorActionPreference = "Stop"

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
  $RootPassword = Read-Host "Mot de passe root Cloud SQL" -AsSecureString
  $RootPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($RootPassword))

  gcloud.cmd sql instances create $CloudSqlInstance `
    --database-version MYSQL_8_0 `
    --tier db-f1-micro `
    --region $Region `
    --storage-size 10 `
    --root-password $RootPasswordPlain
}

$dbExists = gcloud.cmd sql databases describe $DatabaseName --instance $CloudSqlInstance --format "value(name)" 2>$null
if (-not $dbExists) {
  gcloud.cmd sql databases create $DatabaseName --instance $CloudSqlInstance
}

$DbPassword = Read-Host "Mot de passe utilisateur MySQL '$DatabaseUser'" -AsSecureString
$DbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($DbPassword))

$dbUserExists = gcloud.cmd sql users list --instance $CloudSqlInstance --format "value(name)" | Select-String -SimpleMatch $DatabaseUser
if (-not $dbUserExists) {
  gcloud.cmd sql users create $DatabaseUser --instance $CloudSqlInstance --password $DbPasswordPlain
}
else {
  gcloud.cmd sql users set-password $DatabaseUser --instance $CloudSqlInstance --password $DbPasswordPlain
}

$DbPasswordPlain | gcloud.cmd secrets create retours-db-password --data-file=- 2>$null
if ($LASTEXITCODE -ne 0) {
  $DbPasswordPlain | gcloud.cmd secrets versions add retours-db-password --data-file=-
}

$JwtSecret = Read-Host "Secret JWT base64" -AsSecureString
$JwtSecretPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($JwtSecret))
$JwtSecretPlain | gcloud.cmd secrets create retours-jwt-secret --data-file=- 2>$null
if ($LASTEXITCODE -ne 0) {
  $JwtSecretPlain | gcloud.cmd secrets versions add retours-jwt-secret --data-file=-
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
