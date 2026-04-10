# retours-backend

Backend Spring Boot pour le **systeme de gestion des retours**.

## Architecture

Le code suit l'arborescence suivante sous `src/main/java/com/retours`:

- `config`: `SecurityConfig`, `SwaggerConfig`, `CorsConfig`
- `controller`: `RetourProduitController`, `NonConformiteController`, `UtilisateurController`, `HistoriqueRetourController`
- `service`: `RetourProduitService`, `NonConformiteService`, `UtilisateurService`, `HistoriqueRetourService`
- `repository`: `RetourProduitRepository`, `NonConformiteRepository`, `UtilisateurRepository`, `HistoriqueRetourRepository`
- `entity`: `RetourProduit`, `NonConformite`, `Utilisateur`, `HistoriqueRetour`
- `dto/request`: `CreateRetourRequest`, `CreateNonConformiteRequest`, `CreateUtilisateurRequest`, `UpdateEtatRetourRequest`
- `dto/response`: `RetourDTO`, `NonConformiteDTO`
- `enums`: `EtatTraitement`, `Gravite`, `Role`
- `exception`: `ResourceNotFoundException`, `GlobalExceptionHandler`

## Technologies

- Java 17+
- Spring Boot 4
- Spring Web / Spring Data JPA / Validation
- Spring Security (configuration permissive pour dev)
- Swagger OpenAPI (`springdoc`)
- H2 (par defaut) ou MySQL via variables d'environnement

## Lancer le projet

1. Build + tests:

```cmd
cd /d c:\Users\LENOVO\eclipse-workspace\Syst_de_gestion_des_retours
mvnw.cmd test
```

2. Lancer l'application:

```cmd
cd /d c:\Users\LENOVO\eclipse-workspace\Syst_de_gestion_des_retours
mvnw.cmd spring-boot:run
```

## URLs utiles

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

## Configuration base de donnees

Par defaut, `application.properties` utilise H2 en memoire.

Pour utiliser MySQL, definir par exemple:

- `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/gestion_retours`
- `SPRING_DATASOURCE_USERNAME=root`
- `SPRING_DATASOURCE_PASSWORD=...`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver`
