# Franchise Hub

API backend reactiva para administrar franquicias, sucursales, productos y stock. Fue desarrollada como prueba técnica backend y cubre los criterios funcionales, persistencia, programación reactiva, Docker, pruebas automatizadas, infraestructura como código y despliegue en nube.

## 1. Objetivo

El dominio es:

- una franquicia tiene sucursales;
- una sucursal pertenece a una franquicia y tiene productos;
- un producto pertenece a una sucursal y tiene nombre y stock.

La solución expone operaciones para crear y consultar franquicias, sucursales y productos; actualizar nombres y stock; eliminar productos; y consultar el producto con mayor stock por cada sucursal de una franquicia.

## 2. Cumplimiento de la prueba

| Requisito | Implementación |
| --- | --- |
| Spring Boot | Spring Boot 3.5 + Java 21 |
| Crear franquicia | `POST /api/franchises` |
| Crear sucursal | `POST /api/branches` |
| Crear producto | `POST /api/products` |
| Eliminar producto | `DELETE /api/products/{productId}` |
| Modificar stock | `PATCH /api/products/{productId}/stock` |
| Máximo stock por sucursal | `GET /api/products/max-stock?franchiseId=...` |
| Persistencia | MongoDB reactivo; MongoDB Atlas M0 en nube |
| Docker | Dockerfile multi-stage + Docker Compose |
| Programación reactiva | Spring WebFlux + Project Reactor + Reactive MongoDB |
| Renombrar franquicia | `PATCH /api/franchises/{id}/name` |
| Renombrar sucursal | `PATCH /api/branches/{id}/name` |
| Renombrar producto | `PATCH /api/products/{id}/name` |
| Infraestructura como código | Terraform |
| Despliegue cloud | AWS ECR + ECS/Fargate + CodePipeline/CodeBuild + MongoDB Atlas |
| Flujo Git | ramas, Pull Requests, CI y `main` como fuente del CD |
| Documentación local | este README + carpeta `docs/` |

## 3. Stack

- Java 21
- Spring Boot 3.5
- Spring WebFlux
- Project Reactor
- Spring Data Reactive MongoDB
- Spring Security + JWT HS256
- OpenAPI / Swagger
- Maven Wrapper
- MongoDB 8
- JUnit 5, Reactor Test y Testcontainers
- Checkstyle
- Docker / Docker Compose
- GitHub Actions
- Terraform 1.16.x
- MongoDB Atlas M0
- AWS ECR, ECS/Fargate, IAM, CloudWatch, Secrets Manager, S3, CodeBuild, CodePipeline y CodeConnections

## 4. Arquitectura

La aplicación usa una separación Clean/Hexagonal pragmática:

```text
HTTP
  |
  v
presentation
Controllers + request DTOs + ApiResponse + errores
  |
  v
application
Commands / Queries / Handlers + Repository Ports
  |
  v
domain
Franchise / Branch / Product
  |
  v
infrastructure
Mongo adapters + Spring Data + JWT + configuración
  |
  v
MongoDB
```

Regla principal: la capa de aplicación depende de interfaces de repositorio y no de Spring Data. La infraestructura implementa esos puertos.

No se usa un Mediator porque, para el tamaño de esta prueba, el Controller puede invocar directamente al Handler sin introducir una abstracción adicional.

Para respuestas HTTP se usa un único contenedor genérico:

```java
ApiResponse<T>
```

Los DTOs `FranchiseDto`, `BranchDto`, `ProductDto` y `MaxStockProductDto` representan datos distintos del dominio; no existen wrappers HTTP repetidos por cada entidad.

Más detalle: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## 5. Ejecución local recomendada: Docker Compose

### Requisitos

- Git
- Docker Engine o Docker Desktop
- Docker Compose

### 5.1 Clonar

```bash
git clone https://github.com/kevinmartinez07/franchise-hub.git
cd franchise-hub
```

### 5.2 Crear `.env`

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux/macOS:

```bash
cp .env.example .env
```

Los valores de `.env.example` son sólo para desarrollo local. No reemplazarlos por secretos reales.

### 5.3 Levantar MongoDB y API

```bash
docker compose up --build -d
```

Compose:

1. inicia MongoDB;
2. espera el health check de MongoDB;
3. construye la API;
4. inicia la API en `127.0.0.1:8080`;
5. conserva MongoDB en el volumen `franchise_mongo_data`.

### 5.4 Verificar

```bash
docker compose ps
curl http://localhost:8080/actuator/health
```

Esperado:

```json
{"status":"UP"}
```

Swagger:

- http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### 5.5 Detener

Conservar datos:

```bash
docker compose down
```

Eliminar también la base local:

```bash
docker compose down -v
```

## 6. Ejecución local sin Docker para la API

Requiere Java 21 y MongoDB accesible.

PowerShell:

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

## 7. Autenticación

Sólo login, health y Swagger son públicos. El resto requiere JWT.

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "reviewer",
  "password": "reviewer123"
}
```

Respuesta:

```json
{
  "success": true,
  "message": "Autenticación exitosa",
  "data": {
    "accessToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 1800
  }
}
```

Usar:

```text
Authorization: Bearer <TOKEN>
```

El JWT usa HS256, valida issuer y audience, e incluye `sub`, `iss`, `aud`, `iat`, `exp` y `jti`.

## 8. Endpoints

| Método | Endpoint | Descripción |
| --- | --- | --- |
| POST | `/api/auth/login` | Obtener JWT |
| POST | `/api/franchises` | Crear franquicia |
| GET | `/api/franchises` | Listar franquicias |
| GET | `/api/franchises/{id}` | Consultar franquicia |
| PATCH | `/api/franchises/{id}/name` | Renombrar franquicia |
| POST | `/api/branches` | Crear sucursal |
| GET | `/api/branches?franchiseId=...` | Listar/filtrar sucursales |
| GET | `/api/branches/{id}` | Consultar sucursal |
| PATCH | `/api/branches/{id}/name` | Renombrar sucursal |
| POST | `/api/products` | Crear producto |
| GET | `/api/products?branchId=...` | Listar/filtrar productos |
| GET | `/api/products/{id}` | Consultar producto |
| PATCH | `/api/products/{id}/stock` | Actualizar stock |
| PATCH | `/api/products/{id}/name` | Renombrar producto |
| DELETE | `/api/products/{id}` | Eliminar producto |
| GET | `/api/products/max-stock?franchiseId=...` | Mayor stock por sucursal |

## 9. Flujo funcional de prueba

1. `POST /api/auth/login`.
2. Crear una franquicia y guardar su `data.id`.
3. Crear una sucursal usando ese `franchiseId`.
4. Crear dos productos usando el `branchId`.
5. Cambiar el stock de uno.
6. Consultar `/api/products/max-stock?franchiseId=...`.
7. Renombrar franquicia, sucursal y producto.
8. Eliminar un producto.

Swagger permite ejecutar todo el flujo sin construir manualmente los curl.

## 10. Validación, errores y reglas

Hay dos niveles:

- Bean Validation en la frontera HTTP: `@Valid`, `@NotBlank`, `@NotNull`, `@Min`.
- invariantes del dominio: texto no vacío, stock no negativo y fechas consistentes.

Los errores HTTP se entregan con `ProblemDetail` (RFC 7807), por ejemplo:

- 400: request inválido;
- 401: JWT ausente/inválido o credenciales incorrectas;
- 404: recurso no encontrado;
- 500: error inesperado sin exponer stack trace al cliente.

## 11. Persistencia

Colecciones:

- `franchises`
- `branches`
- `products`

Relaciones por ID:

```text
Franchise.id
    |
    +--> Branch.franchiseId
              |
              +--> Product.branchId
```

Índices relevantes:

- `BranchDocument.franchiseId`: índice simple.
- `ProductDocument`: índice compuesto `{ branchId: 1, stock: -1 }`, útil para obtener el máximo stock de una sucursal.

La persistencia sigue siendo reactiva de extremo a extremo.

## 12. Tests y quality gates

Local:

```bash
./mvnw checkstyle:check
./mvnw clean test
./mvnw clean package
```

En Windows:

```powershell
.\mvnw.cmd checkstyle:check
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

La suite cubre:

- entidades de dominio;
- handlers;
- seguridad JWT;
- controllers WebFlux;
- persistencia real con MongoDB mediante Testcontainers;
- carga del contexto.

Testcontainers usa MongoDB real; en CodeBuild se usa la imagen pública de ECR para evitar depender del rate limit anónimo de Docker Hub.

## 13. CI con GitHub Actions

Se ejecuta en Pull Requests y pushes a `dev` o `main`.

Valida:

1. Checkstyle.
2. Compile.
3. tests Maven.
4. integración MongoDB/Testcontainers sin skipped.
5. package JAR.
6. Docker build.
7. smoke test real del contenedor:
   - health `UP`;
   - login;
   - endpoint autenticado.
8. Terraform fmt/init/validate.

GitHub Actions **no despliega**.

## 14. CD con AWS CodePipeline

```text
Merge / push a main
        |
        v
CodeConnections
        |
        v
CodePipeline
  Source
        |
        v
CodeBuild
  Checkstyle
  Tests
  Package
  Docker build
  ECR push
        |
        v
ECS Standard Deploy
```

CodePipeline orquesta. CodeBuild ejecuta `buildspec.yml`. Si Checkstyle, tests, package o Docker build fallan, el pipeline no llega al deploy.

El flujo normal después del bootstrap debe ser: PR -> CI -> merge a `main` -> CD automático.

## 15. Infraestructura cloud

Arquitectura de evaluación:

```text
GitHub
  |
  v
AWS CodeConnections
  |
  v
CodePipeline ---- S3 artifacts
  |
  v
CodeBuild
  |
  v
ECR
  |
  v
ECS/Fargate ---- CloudWatch Logs
  |                  |
  |                  +--> Secrets Manager
  v
MongoDB Atlas M0
```

Terraform administra:

- Atlas project, M0 cluster, database user e IP access list;
- ECR y lifecycle;
- ECS cluster, task definition, service y security group;
- Secrets Manager container;
- IAM roles/policies;
- CloudWatch log groups;
- CodeConnection;
- CodeBuild;
- CodePipeline;
- S3 de artifacts.

No se usan ALB, NAT Gateway, EC2, EKS, RDS ni DocumentDB para esta prueba.

### Nota de seguridad

Para la evaluación, `atlas_allow_public_runtime=true` puede crear temporalmente `0.0.0.0/0` en Atlas porque una task Fargate con IP pública no tiene una IP de salida fija. Esto **no es una recomendación de producción**. En producción se preferiría conectividad privada o un egress controlado.

## 16. Despliegue desde cero

No ejecutar esta sección como parte del flujo diario. Es bootstrap.

Guía completa, incluyendo AWS SSO, Atlas, Terraform, CodeConnection, Secrets Manager, CodePipeline, arranque de ECS y troubleshooting:

[docs/DEPLOYMENT_RUNBOOK.md](docs/DEPLOYMENT_RUNBOOK.md)

## 17. Variables de entorno de aplicación

| Variable | Obligatoria | Uso |
| --- | --- | --- |
| `JWT_SECRET` | Sí | clave HS256; mínimo 32 bytes |
| `FRANCHISE_APP_USERNAME` | Sí | usuario demo de la API |
| `FRANCHISE_APP_PASSWORD` | Sí | contraseña demo de la API |
| `MONGODB_URI` | No local / Sí cloud | conexión MongoDB |
| `JWT_ISSUER` | No | default `franchise-hub` |
| `JWT_AUDIENCE` | No | default `franchise-hub-api` |
| `JWT_EXPIRATION` | No | default `PT30M` |

En AWS, los cuatro valores sensibles se almacenan como JSON en `franchise-hub/runtime` y ECS los inyecta desde Secrets Manager.

## 18. Estructura del repositorio

```text
src/main/java/.../franchise/
├── presentation/
│   ├── controller/
│   ├── dto/
│   └── error/
├── application/
│   ├── repository/
│   ├── security/
│   └── usecase/
├── domain/
│   └── model/
└── infrastructure/
    ├── config/
    ├── persistence/mongo/
    └── security/

src/test/                 tests
terraform/                infraestructura como código
.github/workflows/        CI
Dockerfile                imagen de aplicación
compose.yaml              ejecución local
buildspec.yml             CodeBuild
```

## 19. Documentación para estudiar el proyecto

- [Arquitectura y decisiones](docs/ARCHITECTURE.md)
- [Runbook de despliegue y recuperación](docs/DEPLOYMENT_RUNBOOK.md)
- [Guía de estudio para sustentación/entrevista](docs/STUDY_GUIDE.md)
- [Trazabilidad de requisitos](docs/REQUIREMENTS_TRACEABILITY.md)

## 20. Qué debe poder explicar el autor

Al sustentar este proyecto, las ideas centrales son:

- por qué WebFlux usa `Mono` y `Flux`;
- diferencia entre `map`, `flatMap`, `flatMapMany` y `switchIfEmpty`;
- por qué los handlers dependen de repository ports;
- cómo Mongo adapters implementan esos puertos;
- por qué se separan domain model y Mongo document;
- cómo funciona el JWT;
- diferencia entre CI y CD;
- qué hace CodePipeline y qué hace CodeBuild;
- para qué sirven ECR, ECS/Fargate, Secrets Manager e IAM;
- cómo Terraform construye la infraestructura;
- qué trade-offs existen en el networking de evaluación;
- por qué el deploy cotidiano no debería repetir el bootstrap manual.

La guía de estudio desarrolla cada uno de estos temas paso a paso.
