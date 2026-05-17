# Frontend Angular - Gestion des retours

Interface Angular pour la plateforme de gestion des retours produits, des non-conformites et de l'historique des operations.

## Technologies

- Angular 21
- TypeScript
- SCSS
- Nginx pour l'image Docker de production

## Installation locale

```bash
cd frontend
npm install
```

## Lancer en developpement

```bash
npm start
```

L'application est disponible sur `http://localhost:4200`.

Le proxy de developpement redirige `/api` vers le backend Spring Boot.

## Build

```bash
npm run build
```

## Tests

```bash
npm test -- --watch=false
```

## Docker

Construire l'image frontend depuis ce dossier :

```bash
docker build -t retours-frontend .
```

Le conteneur sert l'application Angular avec Nginx.
