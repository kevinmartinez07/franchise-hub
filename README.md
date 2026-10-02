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

Esta sección prepara el despliegue, pero no afirma que exista una infraestructura AWS activa. La arquitectura prevista es:

```text
GitHub
  ↓ GitHub Actions + OIDC
Amazon ECR
  ↓ imagen Docker
AWS App Runner
  ↓ MONGODB_URI mediante Secrets Manager
MongoDB cloud, por ejemplo MongoDB Atlas
```

La aplicación mantiene MongoDB reactivo y recibe la conexión mediante `MONGODB_URI`. No se reemplaza MongoDB por DynamoDB, RDS o DocumentDB.

### Prerrequisitos

- Cuenta AWS con una región elegida, por defecto `us-east-1`.
- AWS CLI configurado para las comprobaciones autorizadas.
- Terraform `>= 1.7.0`.
- Docker para construir la imagen.
- Una instancia MongoDB cloud administrada fuera de este stack, por ejemplo MongoDB Atlas.
- Cuatro secretos creados previamente en AWS Secrets Manager:
  - `MONGODB_URI`: URI completa de MongoDB cloud.
  - `JWT_SECRET`: secreto HMAC de al menos 32 bytes.
  - `FRANCHISE_APP_USERNAME`: usuario de aplicación.
  - `FRANCHISE_APP_PASSWORD`: contraseña de aplicación.

Los valores de esos secretos no se guardan en Terraform, `terraform.tfvars`, README, GitHub Actions ni Git. Terraform recibe únicamente sus ARNs.

### Estructura Terraform

El directorio [`terraform/`](terraform/) contiene:

- `ecr.tf`: repositorio privado `franchise-hub`, escaneo al publicar y lifecycle de imágenes.
- `iam.tf`: roles de acceso ECR de App Runner, instancia App Runner, lectura de secretos y deploy OIDC.
- `apprunner.tf`: servicio opcional con puerto `8080` y health check `/actuator/health`.
- `variables.tf`: región, nombre, tag, ARNs de secretos y restricciones GitHub.
- `outputs.tf`: URL ECR, URL/ARN App Runner y ARN del rol GitHub cuando están habilitados.
- `terraform.tfvars.example`: valores de ejemplo sin secretos.

`enable_service` es `false` por defecto para permitir preparar ECR y roles antes de que exista la primera imagen. `enable_github_actions_role` también es `false` por defecto hasta confirmar el proveedor OIDC de la cuenta.

### Secrets Manager

Crear los cuatro secretos fuera de Terraform, usando la consola AWS o un procedimiento operativo protegido. Como ejemplo de nombres, sin incluir valores en este repositorio:

```text
franchise-hub/prod/mongodb-uri
franchise-hub/prod/jwt-secret
franchise-hub/prod/app-username
franchise-hub/prod/app-password
```

Después de crearlos, usar sus ARNs en una copia local de `terraform/terraform.tfvars`. No reemplazar los placeholders en `terraform.tfvars.example` ni hacer commit de `terraform.tfvars`.

La policy del rol de instancia sólo permite `secretsmanager:GetSecretValue` sobre esos cuatro ARNs. App Runner recibe las referencias mediante `runtime_environment_secrets`; no recibe los valores en texto plano.

### Validar infraestructura sin modificar AWS

Desde la raíz del repositorio:

```bash
terraform fmt -recursive
terraform fmt -check -recursive
terraform -chdir=terraform init -backend=false
terraform -chdir=terraform validate
```

Para revisar la identidad AWS sin cambiar recursos:

```bash
aws sts get-caller-identity
aws configure get region
```

El workflow de CI ejecuta `fmt -check`, `init -backend=false` y `validate` sin credenciales AWS. `terraform plan` sólo debe ejecutarse con una configuración AWS válida y ARNs ficticios o reales ya autorizados; nunca debe guardarse un plan que contenga información sensible.

### Primer despliegue y bootstrap

ECR debe existir antes de subir la primera imagen y App Runner necesita una imagen válida antes de poder crear el servicio. La secuencia futura es:

1. Crear MongoDB cloud y los cuatro secretos de Secrets Manager fuera de Terraform.
2. Copiar `terraform/terraform.tfvars.example` a `terraform/terraform.tfvars` y completar sólo región, ARNs y configuración no sensible.
3. Ejecutar `terraform init`, `terraform validate` y un `terraform plan` revisado.
4. Aplicar Terraform con `enable_service=false` para crear ECR, roles y policies base. `terraform apply` modifica recursos y puede generar costos; no se ejecuta como parte de este PR.
5. Construir y publicar manualmente la primera imagen con el repository URL del output ECR:

```bash
aws ecr get-login-password --region <AWS_REGION> | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com
docker build --tag <ECR_REPOSITORY_URL>:latest .
docker push <ECR_REPOSITORY_URL>:latest
```

6. Cambiar `enable_service=true`, mantener `image_tag="latest"` y confirmar los cuatro ARNs.
7. Ejecutar un nuevo plan y aplicar Terraform para crear App Runner.
8. Revisar el output `apprunner_service_url` y validar `/actuator/health`.
9. Configurar las variables del environment `production` de GitHub y usar el workflow CD manual para las siguientes versiones.

No se debe intentar crear App Runner antes de publicar la primera imagen ni ejecutar la aplicación sin los cuatro secretos.

### OIDC GitHub Actions

Terraform puede crear el proveedor OIDC con `create_github_oidc_provider=true` si la cuenta no lo tiene, o recibir un ARN existente mediante `github_actions_oidc_provider_arn`. El rol de deploy sólo confía en:

```text
repo:kevinmartinez07/franchise-hub:ref:refs/heads/main
```

El rol tiene permisos limitados para publicar en el repositorio ECR, iniciar/actualizar el servicio App Runner y pasar únicamente los dos roles App Runner. El permiso `ecr:GetAuthorizationToken` usa `Resource="*"` porque AWS lo exige para esa API global; las operaciones restantes se limitan al repositorio o servicio de Franchise Hub.

### Workflow CD manual

`.github/workflows/cd.yml` sólo tiene el trigger `workflow_dispatch`; no se ejecuta automáticamente ni se ejecutó durante este PR. Antes de usarlo, crear las variables del environment `production`:

| Variable | Valor |
| --- | --- |
| `AWS_REGION` | Región AWS elegida |
| `AWS_DEPLOY_ROLE_ARN` | Output `github_actions_deploy_role_arn` |
| `ECR_REPOSITORY` | `franchise-hub` |
| `APP_RUNNER_SERVICE_ARN` | Output `apprunner_service_arn` |

Al ejecutarse manualmente, el workflow configura credenciales mediante OIDC, construye la imagen, publica el tag del SHA seleccionado y actualiza `latest`, inicia el deployment App Runner y espera que `/actuator/health` responda `UP`. No coloca secretos de aplicación en la imagen ni en el workflow.

### Terraform state y costos

Este directorio no versiona `terraform.tfstate`, planes, `.terraform/` ni `terraform.tfvars`. Antes de aplicar en un entorno real se debe elegir un backend remoto protegido y revisar su costo, bloqueo y permisos. Las operaciones `terraform apply`, creación de roles IAM, ECR, App Runner y secretos pueden modificar AWS o generar costos; requieren autorización explícita y no forman parte de esta iteración.
