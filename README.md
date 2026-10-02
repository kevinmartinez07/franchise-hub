# Franchise Hub

API reactiva para administrar franquicias, sucursales, productos y stock.
La aplicación está construida con Java 21, Spring WebFlux y MongoDB reactivo.

## Stack

- Java 21
- Spring Boot 3.5
- Spring WebFlux y Project Reactor
- Spring Data Reactive MongoDB
- MongoDB
- Spring Security y JWT
- Maven Wrapper
- Docker y Docker Compose
- JUnit 5, Reactor Test y Testcontainers
- Terraform
- AWS

## Arquitectura

El proyecto separa las responsabilidades en cuatro capas:

- `presentation`: controllers, DTOs HTTP y manejo de errores.
- `application`: casos de uso, handlers y contratos de persistencia.
- `domain`: entidades y reglas de negocio sin dependencias de Spring o MongoDB.
- `infrastructure`: adaptadores MongoDB, seguridad JWT y configuración externa.

```text
HTTP -> Presentation -> Application -> Repository Port
                                      -> Mongo Adapter -> MongoDB
```

Los controllers reciben HTTP, Application coordina los casos de uso y los
adaptadores implementan las salidas técnicas. El flujo mantiene la reactividad
desde WebFlux hasta Spring Data Reactive MongoDB mediante `Mono` y `Flux`.

## Requisitos

### Con Docker

- Git
- Docker Engine o Docker Desktop con Docker Compose

### Sin Docker

- Java 21
- MongoDB local disponible
- Maven Wrapper, incluido en el repositorio

## Ejecución local con Docker

Esta es la forma recomendada para ejecutar el proyecto. Levanta MongoDB y la
API con una configuración local reproducible.

### 1. Clonar el repositorio

```bash
git clone https://github.com/kevinmartinez07/franchise-hub.git
cd franchise-hub
```

### 2. Preparar las variables locales

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux/macOS:

```bash
cp .env.example .env
```

`.env.example` contiene credenciales ficticias para evaluación local. No usar
estos valores en un entorno compartido o productivo.

### 3. Iniciar la aplicación

```bash
docker compose up --build -d
```

Comprobar el estado:

```bash
docker compose ps
```

Consultar logs si es necesario:

```bash
docker compose logs -f api
```

### 4. Verificar la API

Health:

```text
http://localhost:8080/actuator/health
```

La respuesta esperada contiene `{"status":"UP"}`.

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

### 5. Detener la aplicación

```bash
docker compose down
```

El comando conserva el volumen local de MongoDB.

## Ejecución sin Docker

Se necesita una instancia MongoDB escuchando en:

```text
mongodb://localhost:27017/franchise_hub
```

Windows PowerShell:

```powershell
$env:MONGODB_URI="mongodb://localhost:27017/franchise_hub"
$env:JWT_SECRET="local-only-example-secret-for-franchise-hub-32-bytes"
$env:JWT_ISSUER="franchise-hub"
$env:JWT_AUDIENCE="franchise-hub-api"
$env:JWT_EXPIRATION="PT30M"
$env:FRANCHISE_APP_USERNAME="reviewer"
$env:FRANCHISE_APP_PASSWORD="reviewer123"
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
export MONGODB_URI="mongodb://localhost:27017/franchise_hub"
export JWT_SECRET="local-only-example-secret-for-franchise-hub-32-bytes"
export JWT_ISSUER="franchise-hub"
export JWT_AUDIENCE="franchise-hub-api"
export JWT_EXPIRATION="PT30M"
export FRANCHISE_APP_USERNAME="reviewer"
export FRANCHISE_APP_PASSWORD="reviewer123"
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Autenticación

La prueba técnica no exige gestión de usuarios, por lo que el proyecto no
incluye un CRUD de usuarios. JWT protege los endpoints funcionales mediante un
usuario técnico configurable por variables de entorno:

- `FRANCHISE_APP_USERNAME`
- `FRANCHISE_APP_PASSWORD`

El login es público:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "reviewer",
  "password": "reviewer123"
}
```

Los endpoints protegidos requieren:

```text
Authorization: Bearer <TOKEN>
```

Las credenciales locales de `.env.example` son `reviewer / reviewer123` y son
ficticias. La contraseña del entorno desplegado se comparte por separado; no
se versionan contraseñas reales ni JWT.

## Endpoints

El detalle de los contratos está disponible en Swagger UI.

| Método | Endpoint | Acceso |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Público |
| `POST`, `GET` | `/api/franchises` | JWT |
| `GET` | `/api/franchises/{id}` | JWT |
| `PATCH` | `/api/franchises/{id}/name` | JWT |
| `POST`, `GET` | `/api/branches` | JWT |
| `GET` | `/api/branches/{id}` | JWT |
| `PATCH` | `/api/branches/{id}/name` | JWT |
| `POST`, `GET` | `/api/products` | JWT |
| `GET` | `/api/products/{id}` | JWT |
| `PATCH` | `/api/products/{id}/stock` | JWT |
| `PATCH` | `/api/products/{id}/name` | JWT |
| `DELETE` | `/api/products/{id}` | JWT |
| `GET` | `/api/products/max-stock?franchiseId=...` | JWT |

## Validaciones y errores

- Los nombres e identificadores requeridos no pueden estar vacíos.
- El stock debe ser mayor o igual que cero.
- Los requests se validan mediante Bean Validation.
- Los errores HTTP se centralizan con respuestas `ProblemDetail`.

Los recursos inexistentes y las solicitudes no válidas se informan mediante el
código HTTP correspondiente. Swagger documenta los cuerpos de cada operación.

## Tests y calidad

Windows PowerShell:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd checkstyle:check
```

Linux/macOS:

```bash
./mvnw clean test
./mvnw checkstyle:check
```

Los tests de persistencia usan Testcontainers con MongoDB cuando Docker está
disponible. También se puede generar el artefacto compilado con:

```bash
./mvnw clean package
```

## Variables de entorno

| Variable | Obligatoria | Uso |
| --- | --- | --- |
| `MONGODB_URI` | No | URI de MongoDB. Tiene un valor local por defecto. |
| `JWT_SECRET` | Sí | Secreto HMAC de al menos 32 bytes. |
| `JWT_ISSUER` | No | Emisor del token. |
| `JWT_AUDIENCE` | No | Audiencia del token. |
| `JWT_EXPIRATION` | No | Duración del token, por ejemplo `PT30M`. |
| `FRANCHISE_APP_USERNAME` | Sí | Usuario técnico de evaluación. |
| `FRANCHISE_APP_PASSWORD` | Sí | Contraseña del usuario técnico. |

Los archivos `.env` reales no deben versionarse. Nunca guardar secretos de
producción, credenciales cloud o tokens en el repositorio.

## Integración continua

GitHub Actions valida los cambios en Pull Requests y en las ramas configuradas.
El workflow comprueba:

- Checkstyle y compilación.
- Tests unitarios, reactivos y de persistencia.
- Package y generación del JAR.
- Construcción y smoke test de Docker.
- Formato y validación de Terraform.

GitHub Actions valida el proyecto, pero no publica imágenes ni despliega la
infraestructura.

## AWS y Terraform

La infraestructura está definida con Terraform y el entorno fue desplegado y
validado mediante los siguientes componentes:

- MongoDB Atlas M0
- Amazon ECR
- Amazon ECS/Fargate
- AWS Secrets Manager
- Amazon CloudWatch
- AWS CodeBuild
- AWS CodePipeline
- AWS CodeConnections

```text
GitHub main
    -> CodePipeline
    -> CodeBuild
    -> ECR
    -> ECS/Fargate
    -> MongoDB Atlas
```

La imagen se construye y publica en ECR; ECS ejecuta la task y la aplicación
usa MongoDB Atlas como persistencia. La IP pública de una task Fargate puede
cambiar cuando la task es reemplazada.

La ejecución local no depende de AWS. Para cambios de infraestructura se debe
revisar primero el plan de Terraform y seguir el flujo de Pull Requests del
proyecto.

## Estado del proyecto

La API está implementada con los casos funcionales de franquicias, sucursales,
productos y stock, incluyendo consulta del producto con mayor stock por
sucursal. El repositorio contiene el código, las pruebas, la configuración
local, la infraestructura como código y el pipeline de entrega.
