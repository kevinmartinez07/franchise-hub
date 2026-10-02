# Franchise Hub

API reactiva para administrar franquicias, sucursales, productos y stock. La aplicación usa Java 21, Spring Boot WebFlux, MongoDB reactivo, JWT y OpenAPI/Swagger.

## Stack

- Java 21
- Spring Boot 3.5
- Spring WebFlux y Project Reactor
- Spring Data Reactive MongoDB
- MongoDB 8.0 para ejecución local y pruebas de integración
- Maven Wrapper
- Docker Compose
- JUnit 5, Reactor Test y Testcontainers
- Checkstyle como quality gate

## Requisitos

Para ejecutar la aplicación con Docker:

- Git
- Docker Engine o Docker Desktop con Docker Compose

Para ejecutar sin Docker:

- Java 21
- Maven Wrapper incluido en el repositorio
- MongoDB accesible en `localhost:27017`

## Inicio rápido con Docker

Estos pasos levantan MongoDB y la API con una configuración demo local reproducible.

### 1. Clonar el repositorio

```bash
git clone https://github.com/kevinmartinez07/franchise-hub.git
cd franchise-hub
```

### 2. Crear el archivo `.env`

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux/macOS:

```bash
cp .env.example .env
```

`.env.example` contiene credenciales demo locales y puede utilizarse tal cual para evaluar la aplicación:

```text
FRANCHISE_APP_USERNAME=reviewer
FRANCHISE_APP_PASSWORD=reviewer123
```

El `JWT_SECRET` incluido es un secreto LOCAL ficticio de ejemplo con más de 32 bytes. Estas credenciales son únicamente para ejecución local/evaluación y no deben utilizarse en producción. No sustituirlas por credenciales personales, claves AWS, tokens reales ni `PASSWORD_VM`.

### 3. Levantar servicios

```bash
docker compose up --build -d
```

El comando construye la imagen de la API, inicia MongoDB y espera a que MongoDB esté saludable antes de iniciar la API.

### 4. Ver el estado

```bash
docker compose ps
```

Los servicios `mongo` y `api` deben aparecer en ejecución. MongoDB se guarda en el volumen Docker `franchise_mongo_data` y no se publica directamente al host.

### 5. Validar el health check

```bash
curl http://localhost:8080/actuator/health
```

La respuesta esperada contiene:

```json
{"status":"UP"}
```

### 6. Abrir Swagger

Visita [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

### 7. Detener los servicios

```bash
docker compose down
```

`docker compose down` detiene y elimina los contenedores, pero no elimina el volumen de MongoDB ni sus datos. Para eliminar también los datos locales:

```bash
docker compose down -v
```

## Credenciales demo

Las credenciales configuradas por `.env.example` son:

| Campo | Valor |
| --- | --- |
| Usuario | `reviewer` |
| Contraseña | `reviewer123` |

Son credenciales ficticias para evaluación local. No deben reutilizarse en producción.

## Autenticación

El endpoint de login es público. Ejecuta:

```http
POST /api/auth/login
```

Body JSON:

```json
{
  "username": "reviewer",
  "password": "reviewer123"
}
```

Con curl en Linux/macOS:

```bash
curl --request POST http://localhost:8080/api/auth/login \
  --header 'Content-Type: application/json' \
  --data '{"username":"reviewer","password":"reviewer123"}'
```

Con Windows PowerShell:

```powershell
curl.exe --request POST http://localhost:8080/api/auth/login `
  --header "Content-Type: application/json" `
  --data '{"username":"reviewer","password":"reviewer123"}'
```

La respuesta tiene esta forma y el token está en `data.accessToken`:

```json
{
  "success": true,
  "message": "Autenticación exitosa",
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 1800
  }
}
```

Para usar Swagger:

1. Ejecuta `POST /api/auth/login` desde Swagger con las credenciales demo.
2. Copia únicamente el valor de `data.accessToken`, sin comillas.
3. Pulsa el botón **Authorize** en la parte superior de Swagger UI.
4. En el cuadro de `bearerAuth`, introduce el token. Swagger agrega el esquema `Bearer` automáticamente.
5. Pulsa **Authorize** y cierra el cuadro.
6. Ejecuta ahora los endpoints protegidos de franquicias, sucursales y productos.

Para curl, envía el token como `Authorization: Bearer <TOKEN>` y reemplaza `<TOKEN>` por el valor copiado desde `data.accessToken`.

## Ejemplo de uso

Todos los endpoints de esta sección requieren el header:

```text
Authorization: Bearer <TOKEN>
```

Los ids no se inventan: después de cada creación, copia el campo `data.id` de la respuesta y reemplaza los marcadores en el siguiente comando.

### 1. Crear una franquicia

```bash
curl --request POST http://localhost:8080/api/franchises \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN>' \
  --data '{"name":"Franchise Demo"}'
```

Guarda el `data.id` de la respuesta como `franchiseId`.

### 2. Crear una sucursal

Reemplaza `<FRANCHISE_ID>` por el id retornado en el paso anterior.

```bash
curl --request POST http://localhost:8080/api/branches \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN>' \
  --data '{"franchiseId":"<FRANCHISE_ID>","name":"Sucursal Centro"}'
```

Guarda el `data.id` de la respuesta como `branchId`.

### 3. Crear el producto A con stock menor

```bash
curl --request POST http://localhost:8080/api/products \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN>' \
  --data '{"branchId":"<BRANCH_ID>","name":"Producto A","stock":10}'
```

Guarda su `data.id` como `productAId`.

### 4. Crear el producto B con stock mayor

```bash
curl --request POST http://localhost:8080/api/products \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN>' \
  --data '{"branchId":"<BRANCH_ID>","name":"Producto B","stock":25}'
```

Guarda su `data.id` como `productBId`.

### 5. Listar productos de la sucursal

```bash
curl --request GET 'http://localhost:8080/api/products?branchId=<BRANCH_ID>' \
  --header 'Authorization: Bearer <TOKEN>'
```

La respuesta debe incluir los dos productos y sus stocks actuales.

### 6. Modificar el stock

Reemplaza `<PRODUCT_A_ID>` por el id real del producto A:

```bash
curl --request PATCH http://localhost:8080/api/products/<PRODUCT_A_ID>/stock \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN>' \
  --data '{"stock":30}'
```

### 7. Renombrar la franquicia

```bash
curl --request PATCH http://localhost:8080/api/franchises/<FRANCHISE_ID>/name \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN>' \
  --data '{"name":"Franchise Demo Renamed"}'
```

### 8. Renombrar la sucursal

```bash
curl --request PATCH http://localhost:8080/api/branches/<BRANCH_ID>/name \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN>' \
  --data '{"name":"Sucursal Centro Renamed"}'
```

### 9. Renombrar el producto

```bash
curl --request PATCH http://localhost:8080/api/products/<PRODUCT_B_ID>/name \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <TOKEN>' \
  --data '{"name":"Producto B Renamed"}'
```

### 10. Consultar el producto con mayor stock por sucursal

```bash
curl --request GET 'http://localhost:8080/api/products/max-stock?franchiseId=<FRANCHISE_ID>' \
  --header 'Authorization: Bearer <TOKEN>'
```

La respuesta contiene una entrada por sucursal con `branchId`, `branchName`, `productId`, `productName` y `stock`. Con los valores anteriores, el resultado debe identificar el producto que tenga el stock más alto en cada sucursal.

### 11. Eliminar un producto

```bash
curl --request DELETE http://localhost:8080/api/products/<PRODUCT_B_ID> \
  --header 'Authorization: Bearer <TOKEN>'
```

La API responde con HTTP `204 No Content`.

## Swagger y endpoints

| Método | Endpoint | Protección |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Público |
| `POST`, `GET` | `/api/franchises` | JWT |
| `GET` | `/api/franchises/{franchiseId}` | JWT |
| `PATCH` | `/api/franchises/{franchiseId}/name` | JWT |
| `POST`, `GET` | `/api/branches` | JWT |
| `GET` | `/api/branches/{branchId}` | JWT |
| `PATCH` | `/api/branches/{branchId}/name` | JWT |
| `POST`, `GET` | `/api/products` | JWT |
| `GET` | `/api/products/{productId}` | JWT |
| `PATCH` | `/api/products/{productId}/stock` | JWT |
| `PATCH` | `/api/products/{productId}/name` | JWT |
| `DELETE` | `/api/products/{productId}` | JWT |
| `GET` | `/api/products/max-stock?franchiseId={id}` | JWT |

Documentación interactiva: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health).

## Ejecución local sin Docker

Requisitos: Java 21, Maven Wrapper y MongoDB ejecutándose en `localhost:27017`.

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

## Tests

### Sin Docker disponible

Windows PowerShell:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

Si Docker no está disponible, los 5 tests de integración de MongoDB pueden quedar skipped. El estado local validado en ese escenario fue 68 tests, 0 failures y 0 errors.

### Con Docker disponible

Windows PowerShell:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

Testcontainers inicia MongoDB real usando `mongo:8.0`. El estado validado actualmente es 68 tests, 0 failures, 0 errors y 0 skipped cuando Docker está disponible. La comprobación de CI exige explícitamente que no haya tests skipped.

Quality gate y package:

```powershell
.\mvnw.cmd checkstyle:check
.\mvnw.cmd clean package
```

```bash
./mvnw checkstyle:check
./mvnw clean package
```

## Integración continua

GitHub Actions corre en cada Pull Request y push dirigido a `dev` o `main`.

El workflow valida, en steps separados y visibles:

- Java 21 y Maven Wrapper.
- Checkstyle como quality/style gate; el job falla si el check falla.
- Compilación.
- Tests unitarios, reactivos y de seguridad incluidos en la suite Maven.
- Persistencia con MongoDB real mediante Testcontainers y `mongo:8.0`.
- Cero failures, errors y skipped en los reportes de CI.
- Package y existencia del JAR generado.
- Construcción obligatoria de la imagen Docker.
- Smoke runtime de la imagen construida: health `UP`, login exitoso y `GET /api/franchises` autenticado.
- Limpieza de contenedores y red temporal incluso si el smoke test falla.

El workflow no publica imágenes, no crea infraestructura cloud y no realiza despliegues.

## Arquitectura

El proyecto aplica Clean Architecture con separación de responsabilidades:

- `presentation`: controllers WebFlux, DTOs HTTP y manejo de errores.
- `application`: casos de uso, handlers y contratos de repositorio.
- `domain`: entidades y reglas de negocio, sin dependencias de Spring o MongoDB.
- `infrastructure`: adaptadores MongoDB reactivos, seguridad JWT y configuración.

Flujo principal de una operación persistente:

```text
HTTP
  ↓
Controller
  ↓
Handler
  ↓
Repository Port
  ↓
Mongo Adapter
  ↓
ReactiveMongoRepository
  ↓
MongoDB
```

La cadena conserva reactividad desde WebFlux hasta Spring Data Reactive MongoDB.

## Variables de entorno

| Variable | Obligatoria | Descripción |
| --- | --- | --- |
| `JWT_SECRET` | Sí | Secreto HMAC de al menos 32 bytes. Usar uno diferente en producción. |
| `FRANCHISE_APP_USERNAME` | Sí | Usuario configurado para el login demo. |
| `FRANCHISE_APP_PASSWORD` | Sí | Contraseña configurada para el login demo. |
| `MONGODB_URI` | No | URI MongoDB; por defecto `mongodb://localhost:27017/franchise_hub`. Compose usa su servicio interno. |
| `JWT_ISSUER` | No | Emisor JWT; por defecto `franchise-hub`. |
| `JWT_AUDIENCE` | No | Audiencia JWT; por defecto `franchise-hub-api`. |
| `JWT_EXPIRATION` | No | Duración JWT; por defecto `PT30M`. |

`.env` es sólo para ejecución local y no debe versionarse. No almacenar secretos reales, claves AWS, tokens ni contraseñas de producción en este repositorio.

## Persistencia

La ejecución Docker usa MongoDB en el servicio `mongo` y conserva los datos en el volumen `franchise_mongo_data`. `docker compose down` conserva el volumen; `docker compose down -v` lo elimina.

La integración de persistencia se prueba con Testcontainers y MongoDB real. La configuración cloud definitiva no se necesita para ejecutar o evaluar localmente esta API.

## Despliegue AWS

Esta sección prepara y planifica una arquitectura cloud, pero no afirma que exista infraestructura desplegada. La arquitectura objetivo es:

```text
GitHub Pull Request
        ↓
GitHub Actions CI

main
        ↓
AWS CodeConnections
        ↓
AWS CodePipeline V1
        ↓
AWS CodeBuild
        ↓
Amazon ECR
        ↓
Amazon ECS / Fargate
        ↓
MongoDB Atlas M0 FREE
```

La aplicación continúa usando MongoDB reactivo y recibe `MONGODB_URI`. No se reemplaza persistencia por DynamoDB, RDS o DocumentDB.

### Prerrequisitos

- Cuenta AWS y perfil `franchise-hub`.
- Región AWS, por defecto `us-east-1`.
- Terraform `>= 1.11.0`.
- Docker para ejecución local; CodeBuild construye la imagen cloud.
- MongoDB Atlas Organization ID.
- Variables de provider Atlas fuera del repositorio: `MONGODB_ATLAS_CLIENT_ID` y `MONGODB_ATLAS_CLIENT_SECRET`.
- `TF_VAR_atlas_org_id` y `TF_VAR_atlas_db_password` proporcionados sólo en memoria.

El provider Atlas usa automáticamente `MONGODB_ATLAS_CLIENT_ID` y `MONGODB_ATLAS_CLIENT_SECRET`. No crear variables Terraform para esas credenciales ni guardarlas en archivos.

### Terraform y MongoDB Atlas

Terraform declara en `terraform/atlas.tf`:

- Proyecto Atlas `franchise-hub` en la organización configurada.
- Cluster `mongodbatlas_advanced_cluster` `REPLICASET` M0.
- M0 representado por `provider_name = "TENANT"`, `backing_provider_name = "AWS"` y región `US_EAST_1`.
- Usuario `franchise_app` con rol `readWrite` sobre `franchise_hub`.
- Usuario con `password_wo` y `password_wo_version`; nunca se usa `password`.
- Access list local `190.69.39.48/32`.
- `0.0.0.0/0` sólo si `atlas_allow_public_runtime=true`; no es una configuración de producción.

Genera una contraseña temporal para el plan sin imprimirla:

```powershell
$env:TF_VAR_atlas_db_password = [Convert]::ToHexString(
  [Security.Cryptography.RandomNumberGenerator]::GetBytes(24)
).ToLower()
```

Confirma únicamente su existencia:

```powershell
Test-Path Env:TF_VAR_atlas_db_password
```

### AWS ECS / Fargate

Terraform declara:

- ECR privado `franchise-hub`, scanning al publicar y lifecycle de imágenes.
- ECS cluster y ECS service.
- Task Definition Fargate `awsvpc` con `256` CPU y `1024` MB.
- Imagen `franchise-hub:latest`.
- Default VPC y subnets públicas resueltas mediante data sources, sin hardcodear IDs.
- Security Group TCP `8080` para evaluación pública temporal.
- `assign_public_ip=true`, sin ALB ni NAT Gateway.
- CloudWatch Log Group `/ecs/franchise-hub` con retención de 3 días.
- `desired_count=0` mientras `enable_service=false`; cambia a 1 cuando se autorice el despliegue.

La IP pública de una task Fargate es dinámica. No se crea una VPC nueva, ALB, NAT Gateway, EC2, EKS, RDS ni DocumentDB.

### Secrets Manager

Terraform crea únicamente el contenedor `franchise-hub/runtime`; no crea un `SecretVersion` ni escribe valores sensibles en state. Posteriormente, fuera de Terraform, el secreto deberá contener JSON con:

```json
{
  "MONGODB_URI": "...",
  "JWT_SECRET": "...",
  "FRANCHISE_APP_USERNAME": "...",
  "FRANCHISE_APP_PASSWORD": "..."
}
```

La Task Definition referencia las cuatro claves del mismo secreto. Los valores no sensibles `JWT_ISSUER`, `JWT_AUDIENCE` y `JWT_EXPIRATION` son variables normales.

### IAM del pipeline

Se declara un ECS Task Execution Role con permisos mínimos para descargar la imagen ECR, escribir logs y leer `franchise-hub/runtime`. La aplicación no recibe un Task Role porque no llama directamente APIs AWS.

También se declaran roles separados para CodeBuild y CodePipeline con permisos mínimos para:

- CodeBuild: logs, artifacts S3 y publicación de imágenes en ECR.
- CodePipeline: CodeConnections, artifacts S3, ejecución de CodeBuild y despliegue ECS estándar.
- `iam:PassRole`: únicamente el ECS Task Execution Role, limitado a `ecs-tasks.amazonaws.com`.

No se usa GitHub OIDC para CD. No se usa CloudFormation.

### Validación y plan

Desde la raíz:

```powershell
$env:AWS_PROFILE="franchise-hub"
terraform -chdir=terraform fmt -recursive
terraform -chdir=terraform fmt -check -recursive
terraform -chdir=terraform init -backend=false -upgrade
terraform -chdir=terraform validate
terraform -chdir=terraform plan `
  -var="aws_region=us-east-1" `
  -var="atlas_client_cidr=190.69.39.48/32" `
  -var="atlas_allow_public_runtime=false" `
  -var="enable_service=false"
```

El plan debe mostrar Atlas Project, M0 cluster, database user, access list local, ECR, ECS cluster, task definition, service con desired count 0, Security Group, CloudWatch Log Group, un secreto Secrets Manager e IAM según permisos. No debe mostrar App Runner, ALB ni NAT Gateway.

`terraform apply` modifica recursos y puede generar costos. `terraform destroy` elimina la infraestructura y también requiere autorización. Ninguno forma parte de esta fase.

### AWS CodeConnections

Terraform crea `franchise-hub-github` inicialmente en estado `PENDING`. Después de un `terraform apply`, completar una sola vez la autorización manual:

1. Abrir AWS Console.
2. Ir a Developer Tools / Connections.
3. Seleccionar `franchise-hub-github`.
4. Elegir `Update pending connection`.
5. Autorizar GitHub y seleccionar `kevinmartinez07/franchise-hub`.

No se guardan PATs ni tokens GitHub en Terraform, GitHub Actions o el repositorio.

### AWS CodePipeline y CodeBuild

`franchise-hub` es un pipeline AWS CodePipeline V1 con tres stages:

- **Source**: CodeStarSourceConnection para `kevinmartinez07/franchise-hub`, rama `main`, `DetectChanges=true`.
- **Build**: CodeBuild `franchise-hub-build`, imagen `aws/codebuild/standard:7.0`, runtime Corretto 21, `BUILD_GENERAL1_SMALL` y Docker privileged mode.
- **Deploy**: ECS Standard Deploy para cluster y service `franchise-hub`, usando `imagedefinitions.json`.

CodePipeline sólo escucha cambios reales en `main`. No se configuran triggers para `dev`, ramas feature ni Pull Requests.

El `buildspec.yml` ejecuta Checkstyle, tests, package, Docker build y publica en ECR los tags del commit y `latest`. El artifact mínimo es `imagedefinitions.json`, cuyo container name coincide exactamente con `franchise-hub`.

### Bootstrap posterior

1. Validar y revisar el plan sin aplicar.
2. Autorizar y aplicar la base con `enable_service=false`.
3. Completar manualmente la conexión GitHub pendiente.
4. Crear o actualizar fuera de esta fase el JSON de `franchise-hub/runtime`.
5. Habilitar el ECS service con `enable_service=true` y aplicar cuando corresponda.
6. Confirmar que el service tiene una task lista antes de permitir el primer deploy.
7. Hacer merge a `main`; CodePipeline detectará el cambio y ejecutará CodeBuild y ECS Standard Deploy.

No se deben ejecutar estos pasos como parte de la revisión actual.

### GitHub Actions CI

`.github/workflows/ci.yml` es únicamente CI y se ejecuta en Pull Requests y pushes a `dev` o `main`. Valida Checkstyle, compile, tests con Testcontainers, package, Docker build/smoke y Terraform fmt/init/validate.

GitHub Actions no ejecuta deployments, no publica imágenes y no se ejecuta CodePipeline o CodeBuild durante la revisión.

### Terraform state y costos

No se versionan `.terraform/`, `terraform.tfstate`, planes ni `terraform.tfvars` reales. CodePipeline V1 se configura para aprovechar el free tier aplicable; CodeBuild `BUILD_GENERAL1_SMALL` cobra según minutos y free tier disponible. S3 conserva artifacts mínimos durante 7 días. ECR, ECS/Fargate, CloudWatch y Secrets Manager pueden generar costos; Atlas M0 es el tier gratuito sujeto a límites y políticas vigentes. Fargate con desired count 0 evita mantener una task encendida durante bootstrap, pero cualquier task activa consume recursos.
