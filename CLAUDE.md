# IA SDLC · Backend

Contexto del proyecto para trabajar con un asistente de IA. Léelo antes de cambiar código.

## Qué es

Aplicación web que gestiona un SDLC asistido por IA: en cada fase del proceso de desarrollo un agente de IA propone un artefacto y una persona con un rol definido lo aprueba (gate) antes de avanzar. Es una idea que la autora construye para proponerla; todavía no tiene usuarios reales.

Este repositorio es el back. El front está en el repositorio hermano `ia-sdlc-frontend` (React).

## Estado actual (importante)

- **Este código nunca se ha compilado con Maven ni ejecutado.** Se generó en un entorno sin acceso a Maven Central. Solo se verificó: compilación con `javac` del dominio y la capa de aplicación, el flujo de negocio con repositorios en memoria y las migraciones SQL sobre PostgreSQL 16.
- **Primera tarea al abrir el proyecto**: correr `mvn verify`, corregir los errores de compilación y de pruebas, y luego arrancar la aplicación y recorrer el flujo de `VALIDACION.md`.
- Lo más probable es que los errores estén en `infrastructure` (JPA, seguridad, controladores, pruebas con MockMvc), que solo pasó un análisis sintáctico.
- Las migraciones no se han probado en H2, que es la base que usan las pruebas.

## Comandos

```bash
docker compose up -d        # PostgreSQL local (base, usuario y clave: iasdlc)
mvn verify                  # compila y corre las pruebas (usan H2 en memoria)
mvn spring-boot:run         # http://localhost:8080
docker compose down -v      # borra la base; necesario si cambia V1__schema.sql
```

## Stack y arquitectura

- Java 21, Spring Boot 3.5, Maven. Spring Security con JWT, Spring Data JPA, Flyway.
- PostgreSQL para la aplicación; H2 en memoria solo para pruebas (`src/test/resources/application.yml`).
- Arquitectura hexagonal, paquete base `com.iasdlc`:
  - `domain`: modelo, reglas de negocio y errores con código. Sin Spring ni JPA.
  - `application`: `port.in` (casos de uso), `port.out` (repositorios y `AgentRunner`), servicios. La única dependencia de Spring permitida es `@Transactional`; los casos de uso se registran con la anotación propia `@UseCase`.
  - `infrastructure`: `web` (controllers, DTO, mappers, manejo de errores), `persistence` (entidades JPA y adaptadores), `security` (JWT), `agent` (adaptadores de `AgentRunner`).
- Las reglas de negocio viven en el dominio y se prueban sin infraestructura.

## Modelo

- **Proyecto**: lo estable. Nombre, descripción y squad. No recorre fases.
- **Iniciativa**: lote de trabajo dentro de un proyecto. Es la unidad que recorre las fases, tiene las historias, las ejecuciones y los gates. Un proyecto tiene varias; su nombre es único dentro del proyecto.
- **Historia**: pertenece a una iniciativa. Puede venir ya escrita (`EXISTING`) o construirse con ayuda (`ASSISTED`).
- **Squad** y **roles**: cinco roles fijos (`PRODUCT_OWNER`, `TECH_LEAD`, `ARCHITECT`, `DEVELOPER`, `QA`), con titular y suplente. Cada fase la aprueba un rol.
- **Fases**: `REQUIREMENTS`, `ANALYSIS`, `DESIGN`, `CONSTRUCTION`, `TESTING`. Solo `REQUIREMENTS` está implementada.
- **Agente**: entrada del catálogo con versiones; cada versión guarda el texto de sus instrucciones y la referencia a su repositorio Git (URL, carpeta, commit).
- **Ejecución**: resultado de correr un agente sobre una iniciativa. Queda `PENDING_APPROVAL` hasta que el rol aprobador decide. Ejecuciones y decisiones no se editan ni se borran.

## Contrato de API

- `specs/contrato-api.md` es la fuente de verdad de la API REST. **Es el mismo archivo en los dos repositorios.**
- Regla: primero se cambia el contrato (en ambos repositorios), después el código. No cambies rutas, nombres de campos JSON, enums ni códigos de error sin actualizarlo.
- Los errores responden `{ code, message, details }`; los códigos están en el contrato.

## Motor de agentes

- El puerto `AgentRunner` (`application.port.out`) aísla la ejecución del agente del resto de la aplicación.
- Hoy solo existe `SimulatedAgentRunner`: determinista, sin red, genera un refinamiento de ejemplo. Se elige con `iasdlc.agent.runner=simulated`.
- Decisión de diseño: los agentes se construyen en un repositorio Git (un `agent.md` más scripts y configuración de MCP) y la app registra versiones publicadas. El motor real previsto es el GitHub Copilot SDK para Java, como un adaptador nuevo de `AgentRunner`. Antes de implementarlo hay que hacer una prueba de concepto y verificar el estado actual del SDK.
- No conectes un motor real sin que la autora lo pida.

## Decisiones tomadas

- Usuarios, squads y roles viven dentro de la app. La integración con el directorio corporativo queda para después.
- La base de datos de la aplicación es PostgreSQL (decisión explícita de la autora).
- El esquema inicial (`V1__schema.sql`) se puede reescribir mientras no haya ambientes desplegados; después, solo migraciones nuevas.
- Fuera de alcance por ahora: fases de seguridad, despliegue y operación; integración con Jira y draw.io; proyectos existentes que nacieron sin SDLC; métricas; notificaciones.

## Plan por fases

1. Usuarios, squads, agentes, proyectos, iniciativas y fase de Requerimientos (esta entrega).
2. Motor de agentes real.
3. Análisis, con un resultado de ocho secciones estándar: resumen, alcance, componentes involucrados, reglas de negocio, dependencias, riesgos y supuestos, preguntas abiertas, complejidad.
4. Diseño (texto estructurado; más adelante, arquitectura y detallado por separado).
5. Construcción (genera un proyecto nuevo descargable).
6. Pruebas.

## Convenciones

- Documentación, comentarios y mensajes de commit en español. Identificadores de código, rutas y campos JSON en inglés.
- No hacer commits directamente en `main`: trabajar en ramas.
- Al terminar un cambio, deben pasar `mvn verify` y el recorrido de `VALIDACION.md` que aplique.
- Si un cambio afecta al front, indicarlo y actualizar el contrato en los dos repositorios.

## Documentos del repositorio

- `README.md`: cómo correr, configuración, flujo con curl, decisiones de implementación.
- `VALIDACION.md`: recorrido de validación y dónde es más probable que falle.
- `specs/contrato-api.md`: contrato de API.
