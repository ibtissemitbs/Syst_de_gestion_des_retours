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
.\gcp\deploy-gcp.ps1 -CreateBillableResources
```

Le switch `-CreateBillableResources` est obligatoire car Cloud SQL est payant.

Si les mots de passe ne sont pas fournis, le script genere automatiquement :

- le mot de passe root Cloud SQL
- le mot de passe de l'utilisateur MySQL applicatif
- le secret JWT

Le script :

1. Configure le projet `systemgestiondesretours`.
2. Active les APIs necessaires.
3. Cree Artifact Registry si absent.
4. Cree Cloud SQL MySQL si absent.
5. Cree la base `retoursdb`.
6. Cree ou met a jour l'utilisateur MySQL.
7. Stocke les secrets dans Secret Manager.
8. Donne les droits SQL necessaires a l'utilisateur applicatif.
9. Build et deploie le backend sur Cloud Run.
10. Build et deploie le frontend sur Cloud Run.
11. Configure le frontend avec l'URL du backend.

Si vous lancez le backend manuellement en PowerShell, gardez l'URL JDBC dans une variable et passez la valeur finale entre guillemets simples, sinon les `&` de la chaine `cloudSqlInstance=...&socketFactory=...&useSSL=false` peuvent etre interpretes par PowerShell.

## Erreur Cloud Run PORT=8080

Si Cloud Run affiche :

```text
The user-provided container failed to start and listen on the port defined by the PORT=8080 environment variable
```

ce n'est pas toujours un probleme de port. Dans ce projet, cette erreur est aussi apparue quand Spring Boot n'arrivait pas a demarrer a cause de la connexion Cloud SQL :

- URL JDBC mal passee depuis PowerShell a cause des `&`.
- Secret MySQL different du mot de passe de l'utilisateur `retours_user`.
- Utilisateur MySQL sans droits sur la base `retoursdb`.

Le script `gcp/deploy-gcp.ps1` corrige ces points en utilisant un fichier YAML pour les variables backend, Secret Manager sans saut de ligne final et un import SQL qui applique les privileges de `retours_user`.

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

- Le frontend utilise `BACKEND_URL` et `BACKEND_HOST` pour proxy `/api` vers le backend Cloud Run.
- Le backend utilise le connecteur Cloud SQL MySQL via `socketFactory=com.google.cloud.sql.mysql.SocketFactory`.
- Pour un deploy manuel, preferer le script gcp/deploy-gcp.ps1 avec -CreateBillableResources plutot qu'une commande gcloud run deploy recopieuse.
- Cloud SQL est une ressource payante. Supprimer l'instance si elle n'est plus utilisee.
