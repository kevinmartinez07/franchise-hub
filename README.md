# Franchise Hub

API reactiva para gestionar franquicias, sucursales, productos y stock. Usa Java 21, Spring Boot, WebFlux, MongoDB reactivo y JWT.

## Requisitos

- Java 21
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`)
- MongoDB local, o Docker y Docker Compose

## Variables de entorno

Las variables obligatorias son:

| Variable | Descripcion |
| --- | --- |
| `JWT_SECRET` | Secreto HMAC de al menos 32 bytes |
| `FRANCHISE_APP_USERNAME` | Usuario demo para el login |
| `FRANCHISE_APP_PASSWORD` | Password demo para el login |

Opcionales:

| Variable | Default |
| --- | --- |
| `MONGODB_URI` | `mongodb://localhost:27017/franchise_hub` |
| `JWT_ISSUER` | `franchise-hub` |
| `JWT_AUDIENCE` | `franchise-hub-api` |
| `JWT_EXPIRATION` | `PT30M` |

Para desarrollo local, copia `.env.example` como `.env` y reemplaza sus valores. `.env` no debe versionarse.

## Ejecucion local

Con MongoDB disponible en `localhost:27017`:

```powershell
$env:MONGODB_URI="mongodb://localhost:27017/franchise_hub"
$env:JWT_SECRET="una-clave-local-de-al-menos-32-bytes"
$env:FRANCHISE_APP_USERNAME="reviewer"
$env:FRANCHISE_APP_PASSWORD="change-me"
.\mvnw.cmd spring-boot:run
```

## Ejecucion con Docker Compose

Compose levanta la API y MongoDB, y conserva los datos en el volumen `franchise_mongo_data`:

```powershell
docker compose --env-file .env up --build
```

MongoDB usa `mongo:8.0` por defecto. Para una VM sin soporte AVX, sobrescribe la imagen sólo para esa ejecución:

```powershell
$env:MONGO_IMAGE="mongo:4.4"
docker compose --env-file .env up --build
```

La API queda publicada en `http://localhost:8080`. MongoDB no se publica al host.

Para detener los servicios sin borrar datos:

```powershell
docker compose down
```

## Autenticacion

El login publico es `POST /api/auth/login`:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"reviewer","password":"change-me"}'
```

El JWT se devuelve en `data.accessToken`. Usalo como `Authorization: Bearer <token>` en los endpoints de negocio. La API valida firma HS256, expiracion, issuer y audience.

## Rutas principales

Todos los endpoints de negocio requieren JWT.

- `POST /api/auth/login`
- `POST`, `GET` `/api/franchises`
- `GET /api/franchises/{franchiseId}`
- `PATCH /api/franchises/{franchiseId}/name`
- `POST`, `GET` `/api/branches`
- `GET /api/branches/{branchId}`
- `PATCH /api/branches/{branchId}/name`
- `POST`, `GET` `/api/products`
- `GET`, `PATCH`, `DELETE /api/products/{productId}`
- `GET /api/products/max-stock?franchiseId={franchiseId}`

## Swagger y health

Son publicos:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

Swagger documenta el esquema `bearerAuth` para probar endpoints protegidos.

## Tests

Tests locales:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

Los tests de persistencia usan Testcontainers y requieren Docker. Sin Docker, esos tests pueden quedar skipped; con Docker disponible deben ejecutarse sobre MongoDB real.

## Arquitectura

El proyecto aplica Clean Architecture de forma pragmatica:

```text
presentation -> application -> domain
infrastructure -> application/domain
```

Domain no depende de Spring, MongoDB ni HTTP. Application orquesta casos de uso y contratos. Infrastructure implementa persistencia y seguridad. Presentation expone WebFlux y transforma los contratos HTTP.
