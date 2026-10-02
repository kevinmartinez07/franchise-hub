# Guía de estudio para sustentación

Esta guía está pensada para estudiar el proyecto completo, no sólo memorizar nombres de tecnologías.

## 1. Empieza por el problema

Debes poder explicar sin mirar código:

> La API administra franquicias. Cada franquicia tiene sucursales y cada sucursal tiene productos con stock. Se pueden crear, consultar, renombrar, actualizar stock, eliminar productos y obtener el producto de mayor stock por sucursal.

Luego explica que se añadieron puntos extra:

- Docker;
- reactive programming;
- Terraform;
- cloud;
- renombrados;
- seguridad JWT;
- CI/CD;
- tests.

## 2. Explica una request completa

Ejemplo: crear producto.

```text
POST /api/products
        |
        v
ProductController
        |
        v
@Valid AddProductRequest
        |
        v
AddProductCommand
        |
        v
AddProductHandler
        |
        +--> BranchRepository.findById
        |       |
        |       +--> si no existe: ResourceNotFoundException
        |
        +--> Product.create
        |
        +--> ProductRepository.save
                    |
                    v
          MongoProductRepositoryAdapter
                    |
                    v
         ReactiveProductMongoRepository
                    |
                    v
                  MongoDB
```

Después el resultado vuelve como:

```text
Product -> ProductDto -> ApiResponse<ProductDto>
```

## 3. Pregunta: ¿qué hace @Valid?

`@Valid` activa Bean Validation sobre el request antes de entrar a la lógica principal del Controller.

Ejemplos:
- `@NotBlank`;
- `@NotNull`;
- `@Min(0)`.

Si falla, Spring produce `WebExchangeBindException` y el handler global devuelve HTTP 400.

Importante:
Bean Validation protege la frontera HTTP, pero el dominio también tiene invariantes propias.

## 4. Pregunta: ¿por qué Commands/Queries?

Para expresar intención.

`CreateFranchiseCommand`:
- cambia estado.

`GetFranchisesQuery`:
- consulta.

No es necesario un Mediator para obtener ese beneficio.

## 5. Pregunta: ¿por qué Handler?

El Controller debería ocuparse de HTTP, no de reglas de aplicación.

El Handler:
- coordina repositorios;
- verifica existencia;
- crea/modifica dominio;
- persiste;
- devuelve DTO.

## 6. Pregunta: ¿por qué repository interface?

Para invertir la dependencia.

Application dice:

> necesito guardar y consultar franquicias.

No dice:

> necesito Spring Data MongoDB.

Infrastructure decide cómo cumplirlo.

Esto es el corazón del patrón Ports & Adapters.

## 7. Pregunta: ¿por qué Domain separado de Document?

Porque:

- Domain model representa negocio.
- Mongo Document representa almacenamiento.

Si mañana cambia Mongo, el dominio no debería cambiar por anotaciones de persistencia.

## 8. Reactividad

### Mono

0..1 elementos.

### Flux

0..N elementos.

### map

Transformación síncrona:

```text
Product -> ProductDto
```

### flatMap

Cuando la siguiente operación devuelve otro Mono.

```text
findById -> flatMap -> save
```

### flatMapMany

Cuando después de un Mono necesitas producir Flux.

```text
franchise -> branches
```

### switchIfEmpty

Si un Mono termina vacío, permite convertir ese caso en una excepción.

## 9. Pregunta: ¿por qué WebFlux si Mongo es reactivo?

Porque se conserva un pipeline no bloqueante:

```text
Netty/WebFlux -> Reactor -> Reactive Mongo Driver
```

Si usáramos una capa bloqueante en medio, perderíamos parte del beneficio.

## 10. Máximo stock

El requerimiento pide producto de mayor stock por cada sucursal de una franquicia.

La solución:

1. verifica franquicia;
2. obtiene sucursales;
3. por cada sucursal consulta el primer producto ordenado por stock descendente;
4. devuelve branch + product.

Índice:

```text
branchId ASC
stock DESC
```

Esto ayuda al patrón de consulta.

## 11. JWT

Flujo:

```text
username/password
       |
       v
DemoAuthenticationService
       |
       v
JwtTokenService
       |
       v
JWT HS256
```

Posteriormente:

```text
Authorization: Bearer token
       |
       v
Spring Security Resource Server
       |
       v
firma + issuer + audience + expiration
```

### HS256

Es HMAC simétrico:
- misma clave firma y verifica;
- el secreto debe permanecer protegido.

### ¿Por qué 32 bytes?

La configuración exige al menos 32 bytes para una clave suficientemente larga para HS256.

## 12. CI vs CD

### CI

Pregunta:
> ¿el código es integrable?

GitHub Actions:
- lint/style;
- compile;
- tests;
- integration;
- package;
- Docker smoke;
- Terraform validate.

### CD

Pregunta:
> ¿puedo construir y desplegar el artefacto?

CodePipeline:
- Source;
- CodeBuild;
- ECS Deploy.

## 13. CodePipeline vs CodeBuild

CodePipeline:
- orquesta etapas.

CodeBuild:
- ejecuta comandos.

No digas:
> CodePipeline corre Maven directamente.

Mejor:
> CodePipeline entra a la etapa Build y delega la ejecución de `buildspec.yml` a CodeBuild.

## 14. ECR

Container Registry privado de AWS.

Guarda las imágenes Docker que CodeBuild construye.

Tags:
- commit;
- latest.

## 15. ECS y Fargate

ECS:
- orquestador/servicio de contenedores.

Fargate:
- capacidad serverless para ejecutar tasks sin administrar EC2.

Conceptos:
- cluster;
- task definition;
- service;
- task.

### Task Definition

Plantilla:
- imagen;
- CPU/memoria;
- puerto;
- variables;
- secretos;
- logs;
- execution role.

### Service

Mantiene el número deseado de tasks.

`desired_count=1`:
ECS intenta mantener una task viva.

## 16. Secrets Manager

No es para guardar secretos en Git.

Contiene runtime JSON:

- Mongo URI;
- JWT secret;
- login username/password.

ECS obtiene esas claves al iniciar la task.

## 17. IAM

Principio:

> cada servicio recibe sólo permisos necesarios.

### Execution role ECS

Puede:
- pull ECR;
- leer secret;
- escribir logs.

### CodeBuild role

Puede:
- logs;
- S3 artifacts;
- push ECR.

### CodePipeline role

Puede:
- usar connection;
- ejecutar CodeBuild;
- acceder artifacts;
- actualizar ECS;
- PassRole limitado.

## 18. Terraform

Terraform declara estado deseado.

Ejemplo:

```hcl
resource "aws_ecs_cluster" "app" { ... }
```

Comandos:

```text
init      -> prepara providers
validate  -> valida sintaxis/esquema
plan      -> muestra diferencias
apply     -> lleva infraestructura al estado deseado
destroy   -> elimina recursos administrados
```

### State

Relaciona recursos Terraform con recursos reales.

No es simplemente un log.

### Drift

Cuando la realidad cambia fuera de Terraform.

En ECS se ignora `task_definition` deliberadamente porque CodePipeline registra nuevas revisiones.

## 19. Atlas

Se usa MongoDB Atlas M0.

Terraform crea:
- project;
- cluster;
- database user;
- access lists.

Usuario DB:

```text
franchise_app
```

Rol:

```text
readWrite sobre franchise_hub
```

No confundir con:

```text
FRANCHISE_APP_USERNAME
```

Ese segundo es el usuario demo de login HTTP.

## 20. Problemas reales que aparecieron y qué enseñan

### ECR lifecycle inválido

Una policy con `tagStatus=tagged` necesitaba especificar patrón/prefijo.

Lección:
Terraform puede validar estructura HCL pero una API cloud todavía puede rechazar semántica específica del proveedor.

### Password write-only Atlas

El password no quedó recuperable desde state.

Lección:
los secretos write-only exigen diseñar cuidadosamente bootstrap y rotación.

### CodeConnection PENDING

Requirió autorización manual.

Lección:
no toda integración debe o puede automatizarse; OAuth humano es intencional.

### Docker Hub 429

CodeBuild falló aunque el código estaba bien.

Lección:
un fallo de pipeline no siempre es fallo de aplicación. Hay que diagnosticar el primer error raíz.

Se sustituyeron pulls críticos por ECR Public.

## 21. Trade-offs que debes saber defender

### Atlas 0.0.0.0/0 temporal

Se usa sólo para evaluación porque Fargate tiene IP pública dinámica.

Ventaja:
- simplicidad;
- evita NAT/ALB/VPC avanzada.

Desventaja:
- superficie de red demasiado amplia.

Mitigación:
- credenciales;
- uso temporal;
- para producción usar conectividad privada/controlada.

### Default VPC

Ventaja:
- rápido para prueba.

Desventaja:
- menos control de arquitectura.

### Sin ALB

Ventaja:
- menor costo/complejidad.

Desventaja:
- IP de task dinámica;
- sin endpoint estable;
- health/routing menos robusto.

### JWT demo local

Ventaja:
- demuestra seguridad de endpoints.

Desventaja:
- no es identity management productivo.

En producción considerar Cognito/Keycloak/Auth0/IdP equivalente.

## 22. Simulacro rápido

### ¿Por qué arquitectura hexagonal?

Para aislar lógica de aplicación de detalles externos como MongoDB.

### ¿Dónde está el puerto?

Interfaces de repository en application.

### ¿Dónde está el adaptador?

Mongo*RepositoryAdapter en infrastructure.

### ¿Por qué no guardar entidades directamente?

Para no acoplar dominio a Mongo.

### ¿Qué pasa si una franquicia no existe al crear una sucursal?

`switchIfEmpty` produce `ResourceNotFoundException`, que termina como 404.

### ¿Qué diferencia hay entre unit e integration test?

Unit prueba una unidad aislada.
Integration comprueba colaboración real, por ejemplo adapter + MongoDB real con Testcontainers.

### ¿Por qué Testcontainers?

Para probar contra MongoDB real reproducible, no un mock de persistencia.

### ¿Qué es un smoke test?

Una validación pequeña del artefacto construido: arrancar, health, login y una operación protegida.

### ¿Por qué correr tests en GitHub y CodeBuild?

GitHub protege la integración antes del merge; CodeBuild protege el artefacto real antes de publicarlo/desplegarlo.

## 23. Orden recomendado de estudio este fin de semana

### Bloque 1 - 90 min
Dominio + endpoints + flujo de una request.

### Bloque 2 - 90 min
Reactive: Mono, Flux, map, flatMap, flatMapMany, switchIfEmpty.

### Bloque 3 - 90 min
Clean/Hexagonal: Controller, Handler, Port, Adapter, Document.

### Bloque 4 - 60 min
JWT + Spring Security.

### Bloque 5 - 90 min
Tests + Testcontainers + GitHub Actions.

### Bloque 6 - 120 min
AWS: ECR, ECS/Fargate, IAM, Secrets, CloudWatch.

### Bloque 7 - 90 min
Terraform + Atlas.

### Bloque 8 - 60 min
CI/CD completo y problemas encontrados.

### Bloque 9 - 60 min
Practicar explicando el proyecto sin leer.

Regla:
si no puedes explicar un componente en 2-3 frases sencillas, vuelve a estudiarlo.
