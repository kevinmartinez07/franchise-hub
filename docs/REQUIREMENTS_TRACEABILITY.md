# Trazabilidad de requisitos

Esta tabla conecta cada criterio de la prueba con la implementación real.

| Criterio | Estado | Evidencia |
| --- | --- | --- |
| API en Spring Boot | Cumplido | Spring Boot 3.5 / Java 21 |
| Crear franquicia | Cumplido | `POST /api/franchises` |
| Crear sucursal | Cumplido | `POST /api/branches` |
| Crear producto | Cumplido | `POST /api/products` |
| Eliminar producto | Cumplido | `DELETE /api/products/{productId}` |
| Actualizar stock | Cumplido | `PATCH /api/products/{productId}/stock` |
| Producto mayor stock por sucursal para franquicia | Cumplido | `GET /api/products/max-stock?franchiseId=...` |
| Persistencia | Cumplido | MongoDB Reactive / Atlas M0 |
| Docker | Cumplido | Dockerfile + Compose |
| Programación reactiva | Cumplido | WebFlux + Reactor + Reactive MongoDB |
| Actualizar nombre franquicia | Cumplido | PATCH name |
| Actualizar nombre sucursal | Cumplido | PATCH name |
| Actualizar nombre producto | Cumplido | PATCH name |
| IaC | Cumplido | Terraform para Atlas + AWS |
| Cloud | Cumplido | Atlas + AWS ECS/Fargate |
| Flujo Git | Cumplido | branches, PR, GitHub Actions |
| Repositorio público | Cumplido | GitHub |
| README para despliegue local | Cumplido | README + docs |

## Extras adicionales implementados

Aunque no eran requeridos directamente:

- JWT y Spring Security.
- OpenAPI/Swagger.
- manejo uniforme de respuestas exitosas.
- `ProblemDetail` para errores.
- tests unitarios y de integración.
- Testcontainers.
- Docker smoke test en CI.
- Checkstyle.
- ECR lifecycle.
- Secrets Manager.
- IAM least-privilege orientado al proyecto.
- CloudWatch.
- pipeline CD completo.

## Notas de alcance

La prueba pide documentación para despliegue local; el repositorio además documenta bootstrap cloud y CI/CD.

El runtime cloud de evaluación prioriza bajo costo y simplicidad. El acceso Atlas `0.0.0.0/0` cuando se habilita es temporal y no representa un diseño productivo definitivo.
