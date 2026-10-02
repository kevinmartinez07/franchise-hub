# Runbook de despliegue y operación

> Este documento distingue bootstrap inicial de despliegue cotidiano. El bootstrap requiere más pasos; el despliegue diario debe ser automático desde `main`.

## 1. Arquitectura cloud

```text
GitHub
  |
  v
CodeConnections
  |
  v
CodePipeline V1
  |
  +--> Source
  |
  +--> CodeBuild
  |      - Checkstyle
  |      - tests
  |      - package
  |      - Docker build
  |      - push ECR
  |
  +--> ECS Standard Deploy
           |
           v
       ECS/Fargate
           |
           v
      MongoDB Atlas M0
```

Servicios de apoyo:

- Secrets Manager;
- CloudWatch;
- S3 artifacts;
- IAM.

## 2. Lo que es bootstrap y lo que es deploy

### Bootstrap, una vez

- crear Atlas project/cluster/user;
- crear ECR;
- crear ECS;
- crear roles IAM;
- crear Secrets Manager;
- crear CodeBuild/CodePipeline;
- crear CodeConnection;
- autorizar GitHub manualmente;
- crear el primer SecretVersion;
- habilitar el servicio.

### Deploy cotidiano

```text
feature branch
   |
Pull Request
   |
GitHub Actions
   |
merge main
   |
CodePipeline automático
   |
CodeBuild
   |
ECR
   |
ECS Deploy
```

No se debe ejecutar Terraform para cada cambio de Controller/Handler.

## 3. Prerrequisitos de bootstrap

- AWS CLI autenticado.
- Terraform >= 1.11.
- cuenta AWS correcta.
- perfil con permisos de infraestructura.
- MongoDB Atlas organization ID.
- MongoDB Atlas Service Account.
- IP autorizada para usar la API Atlas durante Terraform.

Variables sensibles:

```text
MONGODB_ATLAS_CLIENT_ID
MONGODB_ATLAS_CLIENT_SECRET
TF_VAR_atlas_org_id
TF_VAR_atlas_db_password
```

No guardar esos valores en el repositorio.

## 4. AWS SSO

Perfil usado durante el bootstrap:

```powershell
$env:AWS_PROFILE="franchise-hub-deploy"
aws sts get-caller-identity
```

Verificar:

- cuenta esperada;
- role de Terraform;
- no utilizar otro perfil accidentalmente.

## 5. Terraform

Inicializar:

```powershell
terraform -chdir=terraform init -backend=false
terraform -chdir=terraform validate
```

Revisar siempre un plan antes de aplicar.

La configuración tiene switches:

```text
atlas_allow_public_runtime
enable_service
```

Bootstrap seguro:

```text
false
false
```

Con esto:

- Atlas no abre runtime público;
- ECS service queda con desired_count 0.

## 6. Password de Atlas

El recurso usa:

```hcl
password_wo
password_wo_version
```

`password_wo` es write-only.

Consecuencia importante:

- Terraform no guarda el password legible en state;
- para una rotación real hay que incrementar `password_wo_version`;
- generar un password diferente manteniendo la misma versión no es un mecanismo válido de rotación.

Por eso, una vez creado/rotado, la URI final debe almacenarse inmediatamente en Secrets Manager.

## 7. Secrets Manager

Terraform crea el contenedor:

```text
franchise-hub/runtime
```

El contenido esperado es:

```json
{
  "MONGODB_URI": "...",
  "JWT_SECRET": "...",
  "FRANCHISE_APP_USERNAME": "...",
  "FRANCHISE_APP_PASSWORD": "..."
}
```

Terraform no administra el SecretVersion para evitar escribir esos secretos en state.

ECS Task Definition referencia claves individuales del JSON.

## 8. CodeConnection

Terraform crea la conexión, inicialmente `PENDING`.

Paso manual único:

1. AWS Console.
2. Developer Tools / Connections.
3. abrir `franchise-hub-github`.
4. Update pending connection.
5. instalar/autorizar GitHub App para el repositorio.
6. comprobar `AVAILABLE`.

Este paso es manual por diseño de autorización.

## 9. CodeBuild

`buildspec.yml` ejecuta:

```text
Checkstyle
Maven tests
Package
Docker build
Docker push ECR
imagedefinitions.json
```

Si cualquier comando falla, CodePipeline detiene el flujo antes del deploy.

### Incidente aprendido: Docker Hub 429

CodeBuild falló durante Testcontainers porque Docker Hub bloqueó pulls anónimos.

Solución aplicada:

- Mongo de Testcontainers desde ECR Public.
- imágenes base del Dockerfile desde ECR Public.

Lección:
un pipeline cloud no debería depender innecesariamente de límites anónimos de un registry externo.

## 10. ECR

Repositorio:

```text
franchise-hub
```

CodeBuild publica:

- tag basado en commit;
- `latest`.

Lifecycle:

- conserva las 10 imágenes tagged más recientes.

## 11. CodePipeline

Stages:

```text
Source -> Build -> Deploy
```

Source:
- GitHub;
- branch `main`;
- DetectChanges=true.

Build:
- CodeBuild.

Deploy:
- ECS Standard Deploy;
- consume `imagedefinitions.json`.

El pipeline se dispara automáticamente ante cambios de `main`.

## 12. ECS/Fargate

Configuración:

- CPU 256;
- memory 1024;
- awsvpc;
- public IP;
- puerto 8080;
- default VPC;
- CloudWatch logs.

`desired_count`:

```text
enable_service=false -> 0
enable_service=true  -> 1
```

Terraform ignora solamente el drift de `task_definition` porque CodePipeline registra nuevas revisiones durante deploy.

Terraform sí conserva control de `desired_count`.

## 13. Atlas networking

Para evaluación existe:

```hcl
atlas_allow_public_runtime
```

Cuando es `true`, Terraform permite temporalmente:

```text
0.0.0.0/0
```

Esto se utiliza porque Fargate recibe IP pública dinámica y la solución evita NAT Gateway/egress fijo por costo y complejidad.

No es diseño recomendado para producción.

Alternativas productivas:

- Private Endpoint/PrivateLink;
- networking privado;
- salida con IP fija controlada;
- arquitectura VPC dedicada.

## 14. Arranque inicial del runtime

Una vez:

- pipeline exitoso;
- imagen ECR disponible;
- secreto AWSCURRENT listo;

se puede aplicar:

```text
atlas_allow_public_runtime=true
enable_service=true
```

Esperado:

- access list runtime creada;
- desired_count 0 -> 1;
- una task RUNNING.

Después:

1. obtener Public IP;
2. probar `/actuator/health`;
3. probar login;
4. probar endpoint autenticado.

## 15. Observabilidad

Logs de app:

```text
/ecs/franchise-hub
```

Logs CodeBuild:

```text
/aws/codebuild/franchise-hub-build
```

Ante task detenida revisar:

- ECS service events;
- stoppedReason;
- container reason;
- CloudWatch app logs.

Ante Build failed revisar:

- fase exacta;
- CloudWatch CodeBuild;
- primer `Caused by` relevante.

## 16. GitHub Actions vs CodePipeline

GitHub Actions:
- valida PR/push;
- Checkstyle;
- compile;
- tests;
- Testcontainers;
- package;
- Docker build;
- smoke test;
- Terraform validate.

CodePipeline:
- toma `main`;
- CodeBuild vuelve a validar;
- crea imagen final;
- publica ECR;
- actualiza ECS.

¿Por qué duplicar tests?
Porque CI protege el merge y CD protege el artefacto que realmente será desplegado.

## 17. Troubleshooting rápido

### CodeConnection PENDING

Completar autorización manual en AWS Console.

### CodeBuild 429 Docker Hub

Usar ECR Public/mirror autenticado, no reintentar indefinidamente.

### ECS desired=1, running=0

Revisar:
- stopped tasks;
- permisos execution role;
- secretos;
- pull ECR;
- conectividad Atlas;
- logs.

### Health no responde

Revisar:
- task RUNNING;
- public IP actual;
- SG TCP 8080;
- Spring Boot startup logs.

### Mongo authentication failure

Revisar:
- URI de Secrets Manager;
- user `franchise_app`;
- password actual;
- authSource;
- Atlas access list.

### Terraform pide atlas_db_password pero no se quiere rotar

No inventar password.
Recuperar de forma segura el password actual si es imprescindible para el input, manteniendo la misma `password_wo_version`. Una rotación verdadera requiere nueva versión.

## 18. Destrucción

`terraform destroy` elimina infraestructura administrada por Terraform y puede borrar recursos con datos.

No ejecutar como parte del flujo normal.

Antes de destruir:
- revisar state;
- revisar recursos;
- confirmar que no hay datos que deban preservarse;
- confirmar costos/objetivo de evaluación.
