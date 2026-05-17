# Système de Gestion des Retours

Application full-stack de gestion des retours produits et des non-conformités.

Ce projet est composé d'un backend robuste sous Spring Boot et d'une interface utilisateur (Frontend).

## 🛠 Technologies

### Backend :

- Java 17+
- Spring Boot 3.x (Web, Data JPA, Validation)
- Spring Security (configuration permissive pour le développement)
- Swagger OpenAPI (`springdoc`) pour la documentation de l'API
- Base de données : H2 (en mémoire par défaut) ou MySQL

### Frontend :

- TypeScript / HTML / SCSS

### DevOps :

- Docker (Dockerfile inclus)

## 📁 Architecture du Backend

Le code source backend suit l'arborescence suivante sous `src/main/java/com/retours` :

- `config/` : Configuration globale (`SecurityConfig`, `SwaggerConfig`, `CorsConfig`)
- `controller/` : Points d'entrée de l'API REST (`RetourProduitController`, `NonConformiteController`, etc.)
- `service/` : Logique métier de l'application
- `repository/` : Interfaces Spring Data JPA pour l'accès aux données
- `entity/` : Modèles de données (Entités JPA)
- `dto/` : Objets de transfert de données (`request/` et `response/`)
- `enums/` : Énumérations (`EtatTraitement`, `Gravite`, `Role`)
- `exception/` : Gestion centralisée des erreurs (`GlobalExceptionHandler`)

## 🚀 Lancer le projet localement

### Prérequis

- Java 17+
- Node.js (pour la partie Frontend)
- Maven (inclus via le wrapper `mvnw`)

### 1. Lancer le Backend

Ouvrez un terminal à la racine du projet backend et exécutez les commandes suivantes :

Pour lancer les tests :

```bash
# Sur Windows
mvnw.cmd test

# Sur Linux/macOS
./mvnw test
```

Pour démarrer l'application :

```bash
# Sur Windows
mvnw.cmd spring-boot:run

# Sur Linux/macOS
./mvnw spring-boot:run
```

L'API sera accessible sur : `http://localhost:8080`

### 2. Lancer le Frontend

(Assurez-vous de vous placer dans le dossier contenant le code frontend s'il est séparé, par exemple `cd frontend`)

```bash
npm install
npm start
```

## 🐳 Déploiement avec Docker

Le projet inclut un `Dockerfile` pour conteneuriser l'application.

```bash
# Construire l'image
docker build -t gestion-retours-app .

# Lancer le conteneur
docker run -p 8080:8080 gestion-retours-app
```

## 🔗 URLs utiles (Backend local)

- Swagger UI (Documentation de l'API) : http://localhost:8080/swagger-ui.html
- OpenAPI JSON : http://localhost:8080/v3/api-docs
- Console H2 : http://localhost:8080/h2-console

## 🗄️ Configuration de la base de données

Par défaut, l'application utilise une base de données H2 en mémoire pour faciliter le développement (`application.properties`).

Pour utiliser MySQL en environnement de production ou de test avancé, vous pouvez surcharger les variables d'environnement suivantes :

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/gestion_retours
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
```


