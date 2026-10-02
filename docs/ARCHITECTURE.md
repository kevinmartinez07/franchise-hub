# Arquitectura y decisiones técnicas

## 1. Visión general

Franchise Hub está dividido en cuatro zonas principales:

```text
presentation -> application -> domain
                    ^
                    |
             infrastructure
```

La dirección importante de dependencias es que el dominio no conoce Spring, MongoDB, AWS ni HTTP. La capa application conoce contratos e interfaces, pero no conoce implementaciones concretas de infraestructura.

## 2. Presentation

Responsabilidades:

- recibir HTTP;
- validar requests con Bean Validation;
- transformar requests cuando hace falta;
- invocar un handler;
- construir `ApiResponse<T>`;
- delegar errores a `GlobalExceptionHandler`.

Ejemplo conceptual:

```text
POST /api/products
        |
        v
AddProductRequest
        |
        v
AddProductCommand
        |
        v
AddProductHandler
```

### Por qué no hay un Response wrapper por entidad

La respuesta HTTP común se modela con:

```java
ApiResponse<T>
```

Por tanto, no se necesita `FranchiseResponse`, `BranchResponse` y `ProductResponse` si únicamente repetirían:

```text
success
message
data
```

Los DTOs de datos sí son distintos porque representan información diferente.

## 3. Application

Contiene:

- commands;
- queries;
- handlers;
- DTOs de caso de uso;
- puertos de repositorio;
- abstracciones de seguridad.

Ejemplo de command:

```java
CreateFranchiseCommand
```

Ejemplo de query:

```java
GetMaxStockProductsByFranchiseQuery
```

### Command vs Query

- Command: expresa intención de modificar estado.
- Query: expresa intención de consultar estado.

No se implementó CQRS con buses, brokers o bases separadas. Sólo se usa una separación conceptual liviana para mantener claros los casos de uso.

### Handler

Un handler:

1. recibe un command/query;
2. valida precondiciones de aplicación;
3. usa repository ports;
4. ejecuta el caso de uso;
5. devuelve un `Mono` o `Flux`.

No depende directamente de Spring Data.

## 4. Domain

Entidades:

- `Franchise`
- `Branch`
- `Product`

Características:

- clases Java puras;
- sin anotaciones Mongo;
- sin anotaciones Spring;
- invariantes propias;
- métodos de negocio como `rename` y `updateStock`.

### Reglas relevantes

- IDs y nombres no pueden ser nulos o vacíos.
- Stock no puede ser negativo.
- `updatedAt` no puede ser anterior a `createdAt`.
- una modificación no puede usar una fecha anterior a la última actualización.

Esto evita depender únicamente de la validación HTTP. Un objeto de dominio debería seguir siendo válido aunque se construya desde otra entrada distinta de un Controller.

## 5. Repository Port + Adapter

Ejemplo:

```text
FranchiseRepository             <- port
        ^
        |
MongoFranchiseRepositoryAdapter <- adapter
        |
        v
ReactiveFranchiseMongoRepository
```

La interfaz vive en application:

```java
Mono<Franchise> save(Franchise franchise);
Flux<Franchise> findAll();
Mono<Franchise> findById(String id);
```

La implementación vive en infrastructure.

Ventajas:

- los handlers no conocen MongoDB;
- los tests de application pueden mockear el contrato;
- cambiar la persistencia no obliga a reescribir los casos de uso;
- se respeta inversión de dependencias.

## 6. Domain Model vs Mongo Document

Se separan porque cumplen responsabilidades diferentes.

`Product`:
- comportamiento;
- reglas;
- invariantes.

`ProductDocument`:
- representación persistente;
- anotaciones Spring Data;
- índices.

Los mappers convierten entre ambas representaciones.

## 7. Programación reactiva

La aplicación usa WebFlux y Reactive MongoDB.

Tipos principales:

### Mono<T>

0 o 1 elemento.

Ejemplos:

```java
Mono<Product>
Mono<FranchiseDto>
Mono<Void>
```

### Flux<T>

0 a N elementos.

Ejemplos:

```java
Flux<Product>
Flux<BranchDto>
```

### map

Transforma el valor sin cambiar la naturaleza reactiva.

```text
Mono<Entity>
   map
Mono<Dto>
```

### flatMap

Se usa cuando la función interna ya devuelve un Publisher.

Incorrecto conceptualmente:

```text
Mono<Mono<Product>>
```

Con `flatMap`:

```text
Mono<Product>
```

Ejemplo real:

```text
find franchise
   flatMap
save branch
```

### flatMapMany

Convierte un flujo `Mono` en uno de múltiples resultados.

En máximo stock:

```text
find franchise -> Mono
        |
        flatMapMany
        v
branches -> Flux
```

### switchIfEmpty

Convierte un resultado vacío en un error de negocio/aplicación.

```java
repository.findById(id)
    .switchIfEmpty(Mono.error(new ResourceNotFoundException(...)))
```

## 8. Caso de uso: máximo stock

Flujo:

```text
franchiseId
   |
find franchise
   |
si no existe -> 404
   |
find branches by franchise
   |
por cada branch
   |
findFirstByBranchIdOrderByStockDesc
   |
MaxStockProductDto
```

El índice compuesto:

```json
{"branchId": 1, "stock": -1}
```

ayuda a consultar eficientemente el producto de mayor stock dentro de una sucursal.

## 9. Seguridad

`SecurityConfig`:

- deshabilita form login, HTTP Basic y sesión de servidor;
- usa JWT Bearer;
- deja públicos login, Swagger y health;
- protege el resto.

El token usa HS256.

Claims principales:

- issuer;
- subject;
- audience;
- issuedAt;
- expiresAt;
- jti.

Además se valida:

- firma;
- issuer;
- audience;
- expiración.

La API se mantiene stateless con `NoOpServerSecurityContextRepository`.

## 10. Login demo

La prueba no pedía un sistema completo de usuarios. Se agregó un login demo protegido por variables de entorno para poder demostrar JWT sin introducir otra base de usuarios.

Esto es deliberadamente simple y no pretende reemplazar un Identity Provider productivo.

## 11. Errores

`GlobalExceptionHandler` entrega `ProblemDetail`.

Mapeo principal:

- `ResourceNotFoundException` -> 404.
- `IllegalArgumentException` -> 400.
- `WebExchangeBindException` -> 400 con errores de campos.
- `BadCredentialsException` -> 401.
- excepciones inesperadas -> 500.

La excepción completa se registra internamente, pero al consumidor no se le expone stack trace.

## 12. Bootstrap de beans

Los handlers no están anotados con `@Service`.

Se registran explícitamente en:

```text
ApplicationHandlersConfig
```

Esto mantiene la capa application libre de anotaciones Spring y deja la composición de dependencias en infrastructure.

## 13. Trade-offs

### Clean Architecture completa vs pragmática

Se evitó introducir:

- Mediator;
- buses de comandos;
- eventos de dominio innecesarios;
- factories complejas;
- wrappers HTTP repetidos.

La prioridad fue separación clara sin sobreingeniería.

### WebFlux

Ventaja:
- flujo no bloqueante desde HTTP hasta MongoDB.

Costo:
- requiere comprender composición reactiva;
- bloquear el thread accidentalmente puede degradar el modelo.

### MongoDB

Ventaja:
- encaja bien con documentos simples;
- soporte reactivo directo;
- Atlas M0 sirve para evaluación.

Costo:
- las relaciones no tienen foreign keys como SQL;
- la consistencia entre IDs se controla desde los casos de uso.

## 14. Preguntas típicas

### ¿Por qué no usar JpaRepository?

Porque la persistencia elegida es MongoDB y el objetivo era conservar un flujo reactivo completo.

### ¿Por qué `ReactiveMongoRepository`?

Porque devuelve `Mono` y `Flux`, integrándose naturalmente con WebFlux.

### ¿Por qué no devolver documentos Mongo desde el Controller?

Porque acoplaría la API y los casos de uso a la tecnología de persistencia.

### ¿Por qué no existe una clase Response diferente para cada entidad?

Porque el envelope HTTP es común y genérico. Los DTOs internos del campo `data` sí cambian según el caso.

### ¿Esto es CQRS?

Es una separación liviana Command/Query. No es CQRS distribuido ni con modelos de lectura/escritura separados.

### ¿Dónde está la inversión de dependencias?

Los handlers dependen de interfaces `FranchiseRepository`, `BranchRepository`, `ProductRepository`; Mongo implementa esas interfaces desde infrastructure.
