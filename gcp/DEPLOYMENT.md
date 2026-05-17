# Deploiement GCP

Projet cible :

- Nom : `systemGestionDesRetours`
- ID projet : `systemgestiondesretours`
- Numero projet : `843636078518`

Architecture conseillee :

- Cloud Run `retours-backend` pour Spring Boot
- Cloud Run `retours-frontend` pour Angular + Nginx
- Cloud SQL MySQL `retours-mysql`
- Artifact Registry `retours` pour les images Docker
- Secret Manager pour le mot de passe MySQL et le secret JWT

## Prerequis

1. Activer la facturation sur le projet GCP.
2. Installer et connecter Google Cloud SDK.
3. Depuis PowerShell, utiliser `gcloud.cmd` et non `gcloud`.
4. Verifier le compte actif :

```powershell
gcloud.cmd auth list
```

## Deploiement automatise

Depuis la racine du repo :

```powershell
.\gcp\deploy-gcp.ps1
```

Le script :

1. Configure le projet `systemgestiondesretours`.
2. Active les APIs necessaires.
3. Cree Artifact Registry si absent.
4. Cree Cloud SQL MySQL si absent.
5. Cree la base `retoursdb`.
6. Cree ou met a jour l'utilisateur MySQL.
7. Stocke les secrets dans Secret Manager.
8. Build et deploie le backend sur Cloud Run.
9. Build et deploie le frontend sur Cloud Run.
10. Configure le frontend avec l'URL du backend.

## Valeurs par defaut

```powershell
.\gcp\deploy-gcp.ps1 `
  -ProjectId systemgestiondesretours `
  -Region europe-west1 `
  -ArtifactRepo retours `
  -CloudSqlInstance retours-mysql `
  -DatabaseName retoursdb `
  -DatabaseUser retours_user
```

## URLs attendues

Apres le deploiement, le script affiche :

- URL backend Cloud Run
- URL frontend Cloud Run

L'application utilisateur doit etre ouverte avec l'URL frontend.

## Notes

- Le frontend utilise la variable `BACKEND_URL` pour proxy `/api` vers le backend Cloud Run.
- Le backend utilise le connecteur Cloud SQL MySQL via `socketFactory=com.google.cloud.sql.mysql.SocketFactory`.
- Cloud SQL est une ressource payante. Supprimer l'instance si elle n'est plus utilisee.
