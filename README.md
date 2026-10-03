# Franchise Hub

Franchise Hub es una API REST reactiva para administrar franquicias,
sucursales, productos y stock.

Está construida con Java 21, Spring Boot WebFlux y MongoDB reactivo.

## Stack principal

- Java 21
- Spring Boot 3.5
- Spring WebFlux y Project Reactor
- Spring Data Reactive MongoDB
- Spring Security y JWT
- OpenAPI / Swagger
- Maven Wrapper
- Docker y Docker Compose
- JUnit 5, Reactor Test y Testcontainers
- Terraform
- AWS ECS/Fargate, ECR, Secrets Manager, CloudWatch, CodeBuild y CodePipeline
- MongoDB Atlas M0

## Arquitectura

El proyecto separa las responsabilidades en cuatro capas:

- `presentation`: controllers, contratos HTTP y manejo de errores.
- `application`: casos de uso, commands, queries, handlers y contratos de repositorio.
- `domain`: entidades y reglas de negocio sin dependencia directa de Spring o MongoDB.
- `infrastructure`: persistencia MongoDB reactiva, seguridad JWT y configuración.

```text
HTTP -> presentation -> application -> repository port
                                      -> infrastructure -> MongoDB
```

El flujo permanece reactivo desde WebFlux hasta MongoDB utilizando `Mono` y
`Flux`.

## Decisiones técnicas

### Spring Boot y Java 21

Spring Boot permite construir la API aprovechando el ecosistema de Spring para
HTTP, validación, seguridad, persistencia y pruebas sin añadir configuración
innecesaria. Java 21 se utiliza como versión LTS compatible con Spring Boot.

### WebFlux y MongoDB reactivo

La API utiliza WebFlux y Project Reactor. Controllers y casos de uso trabajan
con `Mono` y `Flux`, mientras Spring Data Reactive MongoDB mantiene el mismo
modelo en persistencia. MongoDB fue una alternativa permitida en la prueba y
ofrece integración reactiva directa. Localmente se ejecuta con Docker Compose y
en cloud se utiliza MongoDB Atlas.

### Separación por capas, Commands, Queries y Handlers

Los controllers se ocupan de HTTP, Application coordina los casos de uso,
Domain contiene entidades y reglas, e Infrastructure concentra detalles como
MongoDB y seguridad. El dominio y los casos de uso no dependen directamente de
los controllers ni de la implementación MongoDB; los contratos de repositorio
se definen fuera del adaptador.

Los controllers delegan en handlers y las operaciones de lectura y escritura
se organizan mediante queries y commands. Es una separación interna: no se
implementó un bus de mensajes, CQRS distribuido ni un componente Mediator.

### JWT

La prueba no solicita gestión de usuarios, por lo que no se implementó un CRUD
de usuarios. JWT se añadió para proteger los endpoints funcionales mediante un
usuario técnico configurable con `FRANCHISE_APP_USERNAME` y
`FRANCHISE_APP_PASSWORD`.

### Docker, Testcontainers y Swagger

Docker Compose inicia MongoDB, construye la API y configura la comunicación
entre ambos servicios para que el evaluador no tenga que instalar Java, Maven y
MongoDB en la ejecución local recomendada. Los tests de integración de
persistencia utilizan una instancia real de MongoDB en un contenedor.

Swagger permite inspeccionar endpoints, revisar requests y responses, ejecutar
operaciones desde el navegador y probar JWT sin depender de Postman.

### Terraform, ECR/ECS, Secrets Manager y CI/CD

Terraform define la infraestructura cloud como código para documentar los
recursos y revisarlos mediante archivos versionados. La aplicación se empaqueta
como imagen Docker, ECR la almacena y ECS/Fargate ejecuta el contenedor sin
administrar directamente una instancia EC2.

Los valores sensibles del entorno desplegado no se incluyen en la imagen ni en
el código; ECS los obtiene desde Secrets Manager. GitHub Actions valida el
código, CodePipeline orquesta el despliegue desde `main`, CodeBuild ejecuta la
construcción, pruebas y publicación de la imagen, y ECS/Fargate ejecuta la nueva
versión.

```text
main -> CodePipeline -> CodeBuild -> ECR -> ECS/Fargate
```

## Requisitos previos

### Opción recomendada: Docker

Debes tener instalados:

- Git
- Docker Desktop o Docker Engine
- Docker Compose

Con Docker no necesitas instalar Java, Maven ni MongoDB localmente. Docker
Compose levanta MongoDB y la API.

### Opción sin Docker

Debes tener:

- Git
- Java 21
- MongoDB local iniciado y accesible

No necesitas instalar Maven globalmente porque el repositorio incluye Maven
Wrapper (`mvnw` y `mvnw.cmd`).

## Ejecutar localmente con Docker

Esta es la forma recomendada para evaluar el proyecto localmente.

Todos los comandos de esta sección deben ejecutarse desde la raíz del
repositorio, es decir, desde la carpeta que contiene `pom.xml`.

### 1. Clonar el repositorio

Abre una terminal y ejecuta:

```bash
git clone https://github.com/kevinmartinez07/franchise-hub.git
cd franchise-hub
```

En esa carpeta deben existir, entre otros:

```text
pom.xml
compose.yaml
Dockerfile
mvnw
mvnw.cmd
.env.example
src/
terraform/
```

### 2. Crear el archivo `.env`

El repositorio incluye `.env.example`. Crea una copia llamada `.env`.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux/macOS:

```bash
cp .env.example .env
```

El `.env` local debe contener valores equivalentes a estos:

```env
JWT_SECRET=local-only-example-secret-for-franchise-hub-32-bytes
FRANCHISE_APP_USERNAME=reviewer
FRANCHISE_APP_PASSWORD=reviewer123
MONGODB_URI=mongodb://localhost:27017/franchise_hub
JWT_EXPIRATION=PT30M
JWT_ISSUER=franchise-hub
JWT_AUDIENCE=franchise-hub-api
```

Estas credenciales son ficticias y sólo sirven para ejecución local.

```text
Usuario local: reviewer
Contraseña local: reviewer123
```

No cambies la URI local por una URI de producción para ejecutar esta guía.

### 3. Construir e iniciar la aplicación

Desde la raíz del repositorio ejecuta:

```bash
docker compose up --build -d
```

El comando inicia MongoDB, construye la imagen de la API, inicia Spring Boot y
publica la API en el puerto `8080`.

### 4. Verificar los contenedores

Ejecuta:

```bash
docker compose ps
```

Debes ver los servicios `mongo` y `api` en ejecución. Si la API todavía está
iniciando, espera unos segundos y consulta nuevamente.

Para consultar los logs de la API:

```bash
docker compose logs -f api
```

Para salir de los logs sin detener el contenedor pulsa `Ctrl+C`.

### 5. Verificar el health check

Abre esta URL en el navegador:

```text
http://localhost:8080/actuator/health
```

También puedes ejecutar:

```bash
curl http://localhost:8080/actuator/health
```

El resultado esperado es HTTP `200` y un cuerpo que contenga:

```json
{
  "status": "UP"
}
```

Si no aparece `UP`, revisa los logs con `docker compose logs -f api` antes de
continuar.

### 6. Abrir Swagger y OpenAPI

Con la API saludable, abre Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

La especificación OpenAPI está disponible en:

```text
http://localhost:8080/v3/api-docs
```

`/v3/api-docs` es la ruta de la especificación OpenAPI; no representa una
versión `v3` de los endpoints de negocio.

### 7. Detener la aplicación

Cuando termines la evaluación ejecuta desde la raíz:

```bash
docker compose down
```

El comando detiene los contenedores y conserva el volumen local de MongoDB.

## Autenticación local

Con el entorno local iniciado, utiliza las credenciales de `.env.example`:

```text
Usuario: reviewer
Contraseña: reviewer123
```

### 1. Ejecutar el login desde Swagger

1. Abre `http://localhost:8080/swagger-ui.html`.
2. Busca `POST /api/auth/login`.
3. Pulsa **Try it out**.
4. Introduce este body:

```json
{
  "username": "reviewer",
  "password": "reviewer123"
}
```

5. Pulsa **Execute**.
6. Verifica que la respuesta sea HTTP `200`.

La respuesta incluye el JWT en `data.accessToken`:

```json
{
  "success": true,
  "message": "Autenticación exitosa",
  "data": {
    "accessToken": "<TOKEN>",
    "tokenType": "Bearer",
    "expiresIn": 1800
  }
}
```

### 2. Autorizar Swagger

1. Copia únicamente el valor de `data.accessToken`.
2. No copies las comillas, el objeto JSON completo ni la palabra `Bearer`.
3. Pulsa **Authorize** en la parte superior de Swagger.
4. Pega únicamente el token.
5. Pulsa **Authorize** y luego **Close**.
6. Ejecuta `GET /api/franchises`.
7. Verifica que la respuesta sea HTTP `200`.

Los endpoints protegidos utilizan el header:

```text
Authorization: Bearer <TOKEN>
```

## Flujo funcional mínimo

Después de autorizar Swagger, ejecuta las operaciones siguientes en orden. Los
identificadores se obtienen de la respuesta de cada creación y se utilizan en
la operación siguiente.

### 1. Crear una franquicia

Ejecuta `POST /api/franchises` con:

```json
{
  "name": "Franquicia Demo"
}
```

Copia `data.id` de la respuesta. Ese valor será `FRANCHISE_ID`.

### 2. Crear una sucursal

Ejecuta `POST /api/branches` con este body, reemplazando el marcador:

```json
{
  "franchiseId": "<FRANCHISE_ID>",
  "name": "Sucursal Centro"
}
```

Copia `data.id` de la respuesta. Ese valor será `BRANCH_ID`.

### 3. Crear un producto

Ejecuta `POST /api/products` con:

```json
{
  "branchId": "<BRANCH_ID>",
  "name": "Producto Demo",
  "stock": 10
}
```

Copia `data.id` de la respuesta. Ese valor será `PRODUCT_ID`.

### 4. Modificar el stock

Ejecuta `PATCH /api/products/<PRODUCT_ID>/stock` con:

```json
{
  "stock": 30
}
```

Reemplaza `<PRODUCT_ID>` por el identificador real del producto.

### 5. Consultar el mayor stock por sucursal

Ejecuta:

```text
GET /api/products/max-stock?franchiseId=<FRANCHISE_ID>
```

Reemplaza `<FRANCHISE_ID>` por el identificador real de la franquicia. La
respuesta identifica el producto con mayor stock de cada sucursal de esa
franquicia.

## Endpoints

El detalle de los contratos, cuerpos y respuestas está disponible en Swagger.

| Método | Ruta | Descripción | Acceso |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Obtener JWT | Público |
| `POST` | `/api/franchises` | Crear franquicia | JWT |
| `GET` | `/api/franchises` | Listar franquicias | JWT |
| `GET` | `/api/franchises/{franchiseId}` | Consultar franquicia | JWT |
| `PATCH` | `/api/franchises/{franchiseId}/name` | Renombrar franquicia | JWT |
| `POST` | `/api/branches` | Crear sucursal | JWT |
| `GET` | `/api/branches` | Listar sucursales | JWT |
| `GET` | `/api/branches/{branchId}` | Consultar sucursal | JWT |
| `PATCH` | `/api/branches/{branchId}/name` | Renombrar sucursal | JWT |
| `POST` | `/api/products` | Crear producto | JWT |
| `GET` | `/api/products` | Listar productos | JWT |
| `GET` | `/api/products/{productId}` | Consultar producto | JWT |
| `PATCH` | `/api/products/{productId}/stock` | Modificar stock | JWT |
| `PATCH` | `/api/products/{productId}/name` | Renombrar producto | JWT |
| `DELETE` | `/api/products/{productId}` | Eliminar producto | JWT |
| `GET` | `/api/products/max-stock?franchiseId={id}` | Mayor stock por sucursal | JWT |

`GET /api/branches` y `GET /api/products` admiten los filtros documentados en
Swagger.

## Validaciones y errores

- Los nombres e identificadores requeridos no pueden estar vacíos.
- El stock debe ser mayor o igual que cero.
- Los requests se validan mediante Bean Validation.
- Los errores HTTP se centralizan mediante `ProblemDetail`.

## Ejecutar sin Docker

Esta opción necesita MongoDB local iniciado y escuchando en el puerto `27017`.
La aplicación utilizará la base `franchise_hub` mediante esta URI:

```text
mongodb://localhost:27017/franchise_hub
```

Los comandos siguientes deben ejecutarse desde la raíz del repositorio.

### Windows PowerShell

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

### Linux/macOS

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

Cuando Spring Boot indique que inició correctamente, valida:

```text
http://localhost:8080/actuator/health
```

## Variables de entorno

| Variable | Obligatoria | Uso | Ejemplo local |
| --- | --- | --- | --- |
| `MONGODB_URI` | No | Conexión con MongoDB | `mongodb://localhost:27017/franchise_hub` |
| `JWT_SECRET` | Sí | Secreto HMAC de al menos 32 bytes | `local-only-example-secret-for-franchise-hub-32-bytes` |
| `JWT_ISSUER` | No | Emisor del token | `franchise-hub` |
| `JWT_AUDIENCE` | No | Audiencia del token | `franchise-hub-api` |
| `JWT_EXPIRATION` | No | Duración del token | `PT30M` |
| `FRANCHISE_APP_USERNAME` | Sí | Usuario técnico de evaluación | `reviewer` |
| `FRANCHISE_APP_PASSWORD` | Sí | Contraseña local del usuario técnico | `reviewer123` |

Los valores de producción no se incluyen en el repositorio. Los archivos `.env`
reales no deben versionarse.

## Tests y calidad

Ejecuta los comandos desde la raíz del repositorio.

### Windows PowerShell

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd checkstyle:check
.\mvnw.cmd clean package
```

### Linux/macOS

```bash
./mvnw clean test
./mvnw checkstyle:check
./mvnw clean package
```

Los tests de integración requieren Docker disponible porque utilizan
Testcontainers con MongoDB.

## Integración continua y entrega

GitHub Actions valida los Pull Requests y los pushes configurados en el
repositorio. El workflow comprueba:

- Checkstyle y compilación.
- Tests unitarios, reactivos y de persistencia.
- Package y generación del JAR.
- Construcción y smoke test de Docker.
- Formato y validación de Terraform.

El flujo de entrega cloud es:

```text
GitHub main -> CodePipeline -> CodeBuild -> ECR -> ECS/Fargate -> MongoDB Atlas
```

GitHub Actions valida el proyecto; CodePipeline y CodeBuild realizan la
construcción y entrega de la imagen hacia ECS/Fargate.

## Entorno AWS de evaluación

La solución está desplegada en AWS para permitir una evaluación directa.

API:

```text
http://54.152.239.208:8080
```

Swagger UI:

```text
http://54.152.239.208:8080/swagger-ui.html
```

Health:

```text
http://54.152.239.208:8080/actuator/health
```

Para el login del entorno AWS utiliza el usuario:

```text
reviewer
```

La contraseña del usuario de evaluación se entrega junto con el correo de
entrega.

Para probar el entorno AWS:

1. Abre la URL de Swagger indicada arriba.
2. Ejecuta `POST /api/auth/login` con el usuario `reviewer` y la contraseña
   entregada por correo.
3. Verifica HTTP `200`.
4. Copia únicamente `data.accessToken`.
5. Pulsa **Authorize** y pega el token sin comillas ni la palabra `Bearer`.
6. Ejecuta `GET /api/franchises`.
7. Verifica HTTP `200`.

La IP pública pertenece a la task Fargate actual y puede cambiar si la task es
reemplazada.

La infraestructura utiliza Terraform, MongoDB Atlas M0, ECR, ECS/Fargate,
Secrets Manager, CloudWatch, CodeBuild, CodePipeline y CodeConnections.

## Estructura principal

```text
franchise-hub/
├── .github/workflows/
├── src/main/
├── src/test/
├── terraform/
├── Dockerfile
├── compose.yaml
├── buildspec.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .env.example
└── README.md
```

## Flujo Git

Los cambios del proyecto siguen este flujo:

```text
feature / fix / chore / docs -> Pull Request -> dev -> Pull Request -> main
```

El README describe la ejecución local y la evaluación del estado desplegado,
pero no contiene credenciales reales ni tokens.
