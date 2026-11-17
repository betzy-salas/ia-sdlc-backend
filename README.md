# IA SDLC — Backend (Entrega 1)

Backend de la aplicación **IA SDLC**: por cada fase del proceso de desarrollo un agente de IA propone un
artefacto y una persona con cierto rol lo aprueba (gate).

Alcance de la entrega 1:

- Usuarios con registro y login (JWT).
- Squads con integrantes y roles (titular o suplente).
- Catálogo de agentes con versiones.
- Proyectos, iniciativas e historias de usuario (modelo de tres niveles, ver abajo).
- Fase 1 (`REQUIREMENTS`): el agente de refinamiento se ejecuta sobre las historias de una iniciativa, el
  resultado queda pendiente de aprobación del Product Owner del squad del proyecto y se registra la decisión
  del gate.

Modelo de tres niveles:

- **Proyecto**: lo estable (nombre, descripción y squad). No recorre fases ni tiene historias directas.
- **Iniciativa**: un lote de trabajo dentro de un proyecto. Es la unidad que recorre las fases y tiene las
  historias, las ejecuciones y los gates. Un proyecto puede tener varias, cada una en una fase distinta.
- **Historia**: pertenece a una iniciativa.

El contrato REST que implementa está en [`specs/contrato-api.md`](specs/contrato-api.md) y es el mismo que usa
el frontend.

> **Estado de verificación.** En el ambiente donde se generó este código no había acceso a Maven Central
> (el proxy de red bloquea `repo.maven.apache.org`), así que **no se pudo ejecutar `mvn verify` ni arrancar la
> aplicación**. Sí se verificó (también después del cambio al modelo proyecto → iniciativa → historia):
> la compilación con `javac` del dominio y la capa de aplicación; la compilación de persistencia, web
> (controladores, DTO y mappers) y el runner simulado contra clases simuladas de Spring/JPA/Jackson (valida
> la coherencia interna del código, no el uso real de esos frameworks); el flujo de negocio completo de los
> casos de uso con repositorios en memoria; las pruebas unitarias de dominio y del runner con un ejecutor
> mínimo (sin JUnit ni AssertJ reales); el análisis sintáctico de todo el código; y las migraciones SQL
> contra un PostgreSQL 16 real. **No se ha ejecutado nada con Spring**: el mapeo JPA en tiempo de ejecución,
> las consultas derivadas de Spring Data, la seguridad, la serialización JSON, las migraciones en H2 y la
> prueba de integración con MockMvc siguen sin verificar. El primer paso en un ambiente con acceso a Maven
> Central es correr `mvn verify`.

## Requisitos

- Java 21
- Maven 3.9 o superior (el proyecto no incluye Maven Wrapper; ver "Pendientes")
- PostgreSQL 16. Lo más simple es Docker con el `docker-compose.yml` del proyecto

## Cómo correrla

```bash
docker compose up -d       # levanta PostgreSQL en localhost:5432 (base, usuario y clave: iasdlc)
mvn verify                 # compila y corre las pruebas
mvn spring-boot:run        # arranca en http://localhost:8080
```

La aplicación usa PostgreSQL. Flyway crea el esquema y los datos semilla al arrancar, y los datos se
conservan entre reinicios. Para empezar de cero: `docker compose down -v` y luego `docker compose up -d`.

> **Si ya tenía una base creada con el modelo anterior** (proyectos con historias directas): la migración
> `V1__schema.sql` se reescribió para el modelo de iniciativas en lugar de agregar una migración de
> alteración, así que Flyway rechazará esa base por diferencia de checksum. Hay que recrearla
> (`docker compose down -v && docker compose up -d`, o borrar y crear la base `iasdlc`).

Sin Docker, con un PostgreSQL ya instalado:

```sql
CREATE USER iasdlc WITH PASSWORD 'iasdlc';
CREATE DATABASE iasdlc OWNER iasdlc;
```

## Configuración

| Uso | Base de datos |
|---|---|
| Aplicación (`mvn spring-boot:run`) | PostgreSQL |
| Pruebas (`mvn test`, `mvn verify`) | H2 en memoria, modo PostgreSQL (`src/test/resources/application.yml`); no necesitan PostgreSQL |

Variables de entorno:

| Variable | Uso | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP | `8080` |
| `IASDLC_DB_URL` | URL JDBC | `jdbc:postgresql://localhost:5432/iasdlc` |
| `IASDLC_DB_USERNAME` | Usuario de la base de datos | `iasdlc` |
| `IASDLC_DB_PASSWORD` | Contraseña de la base de datos | `iasdlc` |
| `IASDLC_JWT_SECRET` | Secreto para firmar los JWT (mínimo 32 caracteres) | un valor solo para desarrollo |
| `IASDLC_JWT_EXPIRATION_MINUTES` | Vigencia del token | `480` |
| `IASDLC_CORS_ALLOWED_ORIGINS` | Orígenes permitidos por CORS (separados por coma) | `http://localhost:5173` |

Las migraciones (`src/main/resources/db/migration`) usan SQL compatible con PostgreSQL y con H2. La clave de
la base de datos y el secreto JWT por defecto son solo para desarrollo: en cualquier otro ambiente hay que
definir `IASDLC_DB_PASSWORD` e `IASDLC_JWT_SECRET`.

### Datos semilla

- Las 5 fases: `REQUIREMENTS` (Product Owner), `ANALYSIS` (Tech Lead), `DESIGN` (Architect),
  `CONSTRUCTION` (Developer) y `TESTING` (QA).
- El **Agente de refinamiento**: fase `REQUIREMENTS`, estado `PILOT`, versión `0.1.0` con instrucciones de
  ejemplo. No tiene responsable (`owner` es `null`) porque no hay usuarios semilla.

No hay usuarios semilla: el flujo de prueba empieza registrando usuarios.

## Flujo de prueba con curl

Requiere `jq` para extraer valores de las respuestas.

```bash
API=http://localhost:8080/api/v1
JSON='Content-Type: application/json'

# 1. Registrar dos usuarios: la Product Owner y un desarrollador
PO=$(curl -s -X POST $API/auth/register -H "$JSON" \
  -d '{"fullName":"Ana Product Owner","email":"ana@example.com","password":"secreto123"}')
DEV=$(curl -s -X POST $API/auth/register -H "$JSON" \
  -d '{"fullName":"Luis Developer","email":"luis@example.com","password":"secreto123"}')
PO_TOKEN=$(echo "$PO" | jq -r .token);   PO_ID=$(echo "$PO" | jq -r .user.id)
DEV_TOKEN=$(echo "$DEV" | jq -r .token); DEV_ID=$(echo "$DEV" | jq -r .user.id)

# Login (devuelve un token nuevo) y fases semilla
curl -s -X POST $API/auth/login -H "$JSON" -d '{"email":"ana@example.com","password":"secreto123"}' | jq .
curl -s $API/phases -H "Authorization: Bearer $PO_TOKEN" | jq .

# 2. Crear el squad y agregar a la Product Owner
SQUAD_ID=$(curl -s -X POST $API/squads -H "$JSON" -H "Authorization: Bearer $PO_TOKEN" \
  -d '{"name":"Squad Cuentas"}' | jq -r .id)
curl -s -X POST $API/squads/$SQUAD_ID/members -H "$JSON" -H "Authorization: Bearer $PO_TOKEN" \
  -d "{\"userId\":\"$PO_ID\",\"role\":\"PRODUCT_OWNER\",\"substitute\":false}" | jq .

# 3. Crear el proyecto (lo estable: nombre, descripción y squad)
PROJECT_ID=$(curl -s -X POST $API/projects -H "$JSON" -H "Authorization: Bearer $DEV_TOKEN" \
  -d "{\"name\":\"Apertura de cuenta digital\",\"description\":\"Piloto\",\"squadId\":\"$SQUAD_ID\"}" | jq -r .id)

# 4. Crear una iniciativa en el proyecto (arranca en REQUIREMENTS) y agregarle dos historias
INITIATIVE_ID=$(curl -s -X POST $API/projects/$PROJECT_ID/initiatives -H "$JSON" -H "Authorization: Bearer $DEV_TOKEN" \
  -d '{"name":"Lote 1: apertura básica","description":"Primer lote de historias"}' | jq -r .id)
curl -s -X POST $API/initiatives/$INITIATIVE_ID/stories -H "$JSON" -H "Authorization: Bearer $DEV_TOKEN" \
  -d '{"title":"Consultar saldo","description":"Como cliente del banco, quiero consultar el saldo de mi cuenta para saber cuánto dinero tengo disponible.","origin":"EXISTING"}' | jq .
curl -s -X POST $API/initiatives/$INITIATIVE_ID/stories -H "$JSON" -H "Authorization: Bearer $DEV_TOKEN" \
  -d '{"title":"Exportar movimientos","description":"Debe ser rápido","origin":"ASSISTED"}' | jq .

# 5. Ejecutar la fase 1 de la iniciativa: la respuesta llega en PENDING_APPROVAL
EXECUTION_ID=$(curl -s -X POST $API/initiatives/$INITIATIVE_ID/phases/REQUIREMENTS/executions \
  -H "Authorization: Bearer $DEV_TOKEN" | jq -r .id)

# 6. Bandeja de la Product Owner (aprueba por ser PRODUCT_OWNER del squad del proyecto)
curl -s $API/approvals/pending -H "Authorization: Bearer $PO_TOKEN" | jq .

# 7. Quien no es aprobador recibe 403 NOT_GATE_APPROVER
curl -s -X POST $API/executions/$EXECUTION_ID/decision -H "$JSON" -H "Authorization: Bearer $DEV_TOKEN" \
  -d '{"decision":"APPROVE"}' | jq .

# 8. La Product Owner aprueba y la iniciativa pasa a ANALYSIS
curl -s -X POST $API/executions/$EXECUTION_ID/decision -H "$JSON" -H "Authorization: Bearer $PO_TOKEN" \
  -d '{"decision":"APPROVE","comment":"Listo para análisis"}' | jq .
curl -s $API/initiatives/$INITIATIVE_ID -H "Authorization: Bearer $PO_TOKEN" | jq '{currentPhase, journey}'

# 9. Una segunda iniciativa del mismo proyecto arranca en REQUIREMENTS, independiente de la primera
curl -s -X POST $API/projects/$PROJECT_ID/initiatives -H "$JSON" -H "Authorization: Bearer $DEV_TOKEN" \
  -d '{"name":"Lote 2: firma digital"}' | jq '{name, currentPhase}'
curl -s $API/projects/$PROJECT_ID -H "Authorization: Bearer $PO_TOKEN" | jq '.initiatives[] | {name, currentPhase, storyCount}'
curl -s "$API/initiatives?projectId=$PROJECT_ID&phase=ANALYSIS" -H "Authorization: Bearer $PO_TOKEN" | jq .
curl -s "$API/executions?projectId=$PROJECT_ID" -H "Authorization: Bearer $PO_TOKEN" | jq .

# Un nombre de iniciativa repetido en el mismo proyecto responde 409 DUPLICATE_NAME
curl -s -X POST $API/projects/$PROJECT_ID/initiatives -H "$JSON" -H "Authorization: Bearer $DEV_TOKEN" \
  -d '{"name":"Lote 2: firma digital"}' | jq .
```

## Estructura hexagonal

Paquete base `com.iasdlc`. Las dependencias apuntan siempre hacia adentro:
`infrastructure -> application -> domain`.

```
com.iasdlc
├── domain                      Reglas de negocio puras (sin Spring ni JPA)
│   ├── model                   User, Squad, Phase, Agent, Project, Initiative, Story, Execution, enums…
│   ├── service                 GatePolicy (quién aprueba), GateDecisionService (efecto de la decisión)
│   └── exception               DomainException + ErrorCode (códigos del contrato)
├── application                 Orquestación
│   ├── port.in                 Casos de uso (interfaces que usa la capa web)
│   ├── port.out                Repositorios, PasswordHasher, TokenIssuer y AgentRunner
│   ├── usecase                 Implementación de los casos de uso
│   └── view                    Modelos de lectura que devuelven los casos de uso
└── infrastructure              Adaptadores
    ├── web                     controller, dto, mapper, error (@RestControllerAdvice)
    ├── persistence             entity (JPA + mapper), repository (Spring Data), adapter (puertos de salida)
    ├── security                JWT, filtro de autenticación, BCrypt, CORS
    ├── agent                   Adaptadores de AgentRunner (hoy: SimulatedAgentRunner)
    └── config                  Registro de los casos de uso y reloj
```

Notas de diseño:

- El **dominio** no importa nada de Spring ni de JPA. Las reglas están en los agregados (`Initiative`,
  `Execution`, `Squad`, `Agent`; `Project` solo lleva nombre, descripción y squad) y en los servicios de dominio, y son las que cubren las pruebas unitarias.
- La **capa de aplicación** solo depende del dominio y de sus puertos. Los casos de uso se marcan con la
  anotación propia `@UseCase` (la infraestructura los registra como beans en `UseCaseConfig`). La única
  dependencia de Spring en esta capa es `@Transactional`, una concesión práctica para delimitar la
  transacción en el caso de uso.
- La **persistencia** guarda cada agregado completo (el squad con sus integrantes, la iniciativa con sus
  historias y el avance por fase). La iniciativa y la ejecución guardan solo el id de su padre (`project_id`,
  `initiative_id`); el proyecto de una ejecución se obtiene a través de su iniciativa. El resultado del
  agente se guarda como JSON en la ejecución, sin llave foránea a las historias, para que el registro histórico no cambie si después se borra una historia.
- Las **ejecuciones y decisiones** no se editan ni se borran: no hay rutas ni métodos de repositorio para
  hacerlo, y una ejecución decidida rechaza una segunda decisión.

## El puerto `AgentRunner`

Es el punto de extensión del motor de agentes (`application.port.out`):

```java
public interface AgentRunner {
    AgentOutput run(AgentDefinition agent, AgentInput input);
}
```

- `AgentDefinition`: el agente y su versión vigente (nombre, fase, versión, `instructions` — el texto del
  `agent.md` — y la ubicación en el repositorio).
- `AgentInput`: la iniciativa (`initiativeId`, `initiativeName`, `initiativeDescription`), el nombre de su
  proyecto como contexto (`projectName`) y sus historias (id, título, descripción).
- `AgentOutput`: una `RefinedStory` por cada historia (`title`, `asA`, `iWant`, `soThat`,
  `acceptanceCriteria`, `ambiguities`).
- Si el motor no puede producir un resultado lanza `AgentRunnerException`: la ejecución queda `FAILED` y la
  fase sigue `IN_PROGRESS` para reintentar. También queda `FAILED` si la salida no trae resultado para
  alguna historia.

El caso de uso `ExecutionService` elige el agente de la fase (en `PILOT` o `ACTIVE` con versión vigente;
si hay varios prefiere `ACTIVE`), arma la entrada, llama al puerto y guarda la ejecución. No sabe qué motor
hay detrás.

### Adaptador de esta entrega: `SimulatedAgentRunner`

Determinista y sin red. Para cada historia:

- limpia el título;
- busca el actor (`como …`), la necesidad (`quiero/necesito/deseo …`) y el beneficio (`para …`) en la
  descripción; si no los encuentra usa un valor por defecto;
- genera 3 criterios de aceptación en formato Dado / Cuando / Entonces (camino feliz, error y trazabilidad);
- lista ambigüedades: sin descripción o descripción corta, actor no identificado, beneficio no explícito y
  términos subjetivos ("rápido", "fácil"…).

No interpreta las instrucciones del agente. Se selecciona con la propiedad:

```yaml
iasdlc:
  agent:
    runner: simulated
```

### Cómo agregar otro adaptador (por ejemplo, GitHub Copilot SDK para Java)

1. Agregar la dependencia del SDK al `pom.xml`.
2. Crear la clase en `com.iasdlc.infrastructure.agent`, por ejemplo:

   ```java
   @Component
   @ConditionalOnProperty(name = "iasdlc.agent.runner", havingValue = "copilot")
   public class CopilotAgentRunner implements AgentRunner {

       @Override
       public AgentOutput run(AgentDefinition agent, AgentInput input) {
           // 1. Abrir una sesión con el motor usando agent.instructions() como instrucciones del agente.
           // 2. Enviar las historias de input.stories() y pedir la respuesta en un formato estructurado.
           // 3. Convertir la respuesta a AgentOutput (una RefinedStory por storyId).
           // 4. Ante cualquier falla (red, tiempo de espera, respuesta inválida) lanzar AgentRunnerException.
       }
   }
   ```

3. Agregar su configuración (credenciales, modelo, tiempos de espera) bajo `iasdlc.agent.copilot.*`, leída
   desde variables de entorno.
4. Activarlo con `iasdlc.agent.runner=copilot`.

No hay que tocar `domain` ni `application`. Dos puntos a tener en cuenta cuando el motor sea real:

- Hoy la ejecución es síncrona y ocurre dentro de la transacción del caso de uso. Con un motor lento conviene
  pasar a ejecución asíncrona (el estado `RUNNING` ya existe) para no mantener la transacción abierta.
- Si el valor de `iasdlc.agent.runner` no corresponde a ningún adaptador, la aplicación no arranca (falta el
  bean de `AgentRunner`), lo que evita ejecutar con un motor distinto al esperado.

## Reglas de negocio implementadas

- Un proyecto solo tiene nombre, descripción y squad; no recorre fases. Sus iniciativas sí.
- Una iniciativa nueva arranca en `REQUIREMENTS` / `IN_PROGRESS`; las demás fases quedan `PENDING`. Cada
  iniciativa avanza por su cuenta: aprobar una no cambia la fase de las otras del mismo proyecto.
- El squad de una iniciativa es el de su proyecto.
- El nombre de la iniciativa es único dentro del proyecto (409 `DUPLICATE_NAME`); se puede repetir en otro
  proyecto.
- Solo se agregan o borran historias mientras `REQUIREMENTS` esté `IN_PROGRESS` en esa iniciativa.
- Ejecutar la fase: `NO_STORIES`, `AGENT_NOT_AVAILABLE`, `PHASE_AWAITING_GATE`, `PHASE_ALREADY_APPROVED`.
- El aprobador de una fase es un integrante del squad del proyecto de la iniciativa con el rol
  `approverRole` de la fase, titular o suplente. Otro usuario recibe 403 `NOT_GATE_APPROVER`. La bandeja de
  aprobaciones se arma con la misma regla.
- `APPROVE`: ejecución `ACCEPTED`, fase `APPROVED`, la iniciativa pasa a `ANALYSIS` (`IN_PROGRESS`).
- `REQUEST_REWORK` / `REJECT` exigen comentario; la fase vuelve a `IN_PROGRESS`.
- `story.refined` refleja la última ejecución no rechazada: una ejecución pendiente, aceptada o con
  reproceso deja su propuesta en las historias; al rechazar se restaura la propuesta de la ejecución válida
  anterior de esa iniciativa o queda `null` si no hay.
- `GET /executions` filtra por `initiativeId`, `projectId` (el proyecto de la iniciativa) y `status`;
  `GET /initiatives` filtra por `projectId` y por `phase` (compara contra `currentPhase`).
- `/dashboard/phases` cuenta en `initiativesInPhase` las iniciativas cuyo `currentPhase` es esa fase.
- Un usuario puede estar en varios squads y tener varios roles en un squad, pero cada rol una sola vez.

## Decisiones donde el contrato no era explícito

| Tema | Decisión |
|---|---|
| Usuario con el mismo rol dos veces en un squad | 409 `DUPLICATE_MEMBER` |
| `owner` del agente semilla | `null` (no hay usuarios semilla) |
| `ownerUserId` al crear un agente | Opcional; si no llega, el responsable es quien crea el agente |
| `instructions` de las versiones | Se omite en `GET /agents`; se incluye en las respuestas de un agente individual (`GET`, `POST`, `PATCH`) |
| Versión repetida en un agente | 409 `DUPLICATE_NAME` |
| Nombre repetido de squad, proyecto o agente | 409 `DUPLICATE_NAME` (sin distinguir mayúsculas) |
| Nombre repetido de iniciativa en un proyecto | 409 `DUPLICATE_NAME`, sin distinguir mayúsculas y sin contar espacios al inicio o al final; el mismo nombre en otro proyecto sí se permite |
| `completed` de la iniciativa | Se calcula: `true` cuando el gate de la última fase (`TESTING`) queda aprobado. En esta entrega siempre es `false` porque solo se ejecuta `REQUIREMENTS` |
| `initiativeCount` y `project.initiatives` | Cuentan todas las iniciativas del proyecto; `initiatives` va de la más reciente a la más antigua, igual que `GET /initiatives` |
| `GET /initiatives` o `GET /executions` con un `projectId` o `initiativeId` que no existe | 200 con lista vacía (es un filtro, no un recurso) |
| `GET /executions` con `initiativeId` y `projectId` a la vez | Se aplican los dos filtros (si la iniciativa no es de ese proyecto, lista vacía) |
| `description` de la iniciativa | Opcional (máximo 4000 caracteres), igual que la del proyecto |
| Rutas anteriores bajo `/projects/{id}/stories` y `/projects/{id}/phases/...` | Eliminadas; responden como cualquier ruta inexistente |
| Proyectos sin iniciativas | Se permiten; no hay borrado ni edición de proyectos o iniciativas (el contrato no los define) |
| Agregar/borrar historias fuera de `IN_PROGRESS` | 409 `PHASE_AWAITING_GATE` o `PHASE_ALREADY_APPROVED` según el estado |
| Ejecutar una fase distinta de `REQUIREMENTS` | 400 `VALIDATION_ERROR` |
| Ejecución `FAILED` | Responde 201 con `status: FAILED`; la fase sigue `IN_PROGRESS`. El motivo se guarda pero no se expone (el contrato no tiene ese campo) |
| Decidir una ejecución `FAILED` | 409 `EXECUTION_ALREADY_DECIDED` |
| Orden de validación al decidir | Primero el aprobador (403), luego el estado (409) y el comentario (400) |
| `details` del error | Lista de textos `"campo: mensaje"` en los errores de validación; vacía en los demás |
| Contraseña | Entre 8 y 72 caracteres |
| `substitute` al agregar integrante | Opcional; por defecto `false` |
| Token de un usuario que ya no existe | 401 `UNAUTHORIZED` (por ejemplo, tras recrear la base de datos) |
| Permisos | Cualquier usuario autenticado puede crear squads, agentes, proyectos e iniciativas, y ejecutar la fase; solo el gate valida rol |
| Errores fuera del contrato | 405 `METHOD_NOT_ALLOWED`, 415 `UNSUPPORTED_MEDIA_TYPE`, 500 `INTERNAL_ERROR`, con la misma forma |

## Pruebas

- Unitarias de dominio (`src/test/java/com/iasdlc/domain`): iniciativa (incluye dos iniciativas del mismo
  proyecto con fases independientes), ejecución, gate, squad y agente.
- Unitaria del runner simulado (`infrastructure/agent`).
- Integración con MockMvc y H2 (`infrastructure/web/RequirementsFlowIntegrationTest`):
  - flujo completo: registra dos usuarios, crea squad, agrega la Product Owner, crea proyecto, crea una
    iniciativa, agrega 2 historias, ejecuta la fase 1, verifica `PENDING_APPROVAL` y la bandeja, comprueba el
    403 del no aprobador, aprueba y verifica que la iniciativa pasa a `ANALYSIS`;
  - varias iniciativas en un proyecto: se aprueba una y la otra sigue en `REQUIREMENTS`; filtros de
    `GET /initiatives` y `GET /executions`;
  - nombre de iniciativa duplicado en el proyecto (409 `DUPLICATE_NAME`) y permitido en otro proyecto;
  - datos semilla, catálogo de agentes y forma de los errores.

## Pendientes

- Ejecutar `mvn verify` y arrancar la aplicación en un ambiente con acceso a Maven Central (ver "Estado de
  verificación").
- Generar el Maven Wrapper: `mvn -N wrapper:wrapper`.
- Probar la aplicación completa contra PostgreSQL (las migraciones sí se probaron contra PostgreSQL 16).
- Sin control de concurrencia optimista: dos ejecuciones simultáneas de la misma iniciativa podrían pasar ambas la
  validación.
