package com.iasdlc.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración (aplicación completa con H2 y las migraciones de Flyway) que recorre
 * el flujo de la fase 1: registro, squad, proyecto, iniciativa, historias, ejecución del agente
 * y gate. Todas las pruebas comparten la misma base en memoria, por eso cada una usa correos y
 * nombres propios y no asume que la base esté vacía.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RequirementsFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requirementsPhaseFromRegistrationToApproval() throws Exception {
        // 1. Registrar dos usuarios: la Product Owner y un desarrollador que no aprueba.
        JsonNode owner = register("Ana Product Owner", "ana.po@example.com");
        JsonNode developer = register("Luis Developer", "luis.dev@example.com");
        String ownerToken = owner.get("token").asText();
        String developerToken = developer.get("token").asText();
        String ownerId = owner.get("user").get("id").asText();
        String developerId = developer.get("user").get("id").asText();

        // 2. Crear el squad y agregar a la PRODUCT_OWNER (y al desarrollador con otro rol).
        String squadId = readJson(perform(post("/api/v1/squads"), ownerToken, Map.of("name", "Squad Cuentas"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.members", hasSize(0))))
                .get("id").asText();

        perform(post("/api/v1/squads/{id}/members", squadId), ownerToken,
                Map.of("userId", ownerId, "role", "PRODUCT_OWNER", "substitute", false))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(ownerId))
                .andExpect(jsonPath("$.fullName").value("Ana Product Owner"))
                .andExpect(jsonPath("$.role").value("PRODUCT_OWNER"))
                .andExpect(jsonPath("$.substitute").value(false));
        perform(post("/api/v1/squads/{id}/members", squadId), ownerToken,
                Map.of("userId", developerId, "role", "DEVELOPER", "substitute", false))
                .andExpect(status().isCreated());

        // El mismo rol dos veces para el mismo usuario en el squad no se permite.
        perform(post("/api/v1/squads/{id}/members", squadId), ownerToken,
                Map.of("userId", ownerId, "role", "PRODUCT_OWNER", "substitute", true))
                .andExpect(status().isConflict());

        perform(get("/api/v1/auth/me"), ownerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberships", hasSize(1)))
                .andExpect(jsonPath("$.memberships[0].squadName").value("Squad Cuentas"))
                .andExpect(jsonPath("$.memberships[0].role").value("PRODUCT_OWNER"));

        // 3. Crear el proyecto: es lo estable, no recorre fases ni tiene historias.
        String projectId = readJson(perform(post("/api/v1/projects"), developerToken,
                Map.of("name", "Apertura de cuenta digital", "description", "Piloto", "squadId", squadId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apertura de cuenta digital"))
                .andExpect(jsonPath("$.description").value("Piloto"))
                .andExpect(jsonPath("$.squad.id").value(squadId))
                .andExpect(jsonPath("$.squad.name").value("Squad Cuentas"))
                .andExpect(jsonPath("$.initiatives", hasSize(0)))
                .andExpect(jsonPath("$.currentPhase").doesNotExist())
                .andExpect(jsonPath("$.stories").doesNotExist())
                .andExpect(jsonPath("$.journey").doesNotExist()))
                .get("id").asText();

        JsonNode dashboardBefore = readJson(perform(get("/api/v1/dashboard/phases"), ownerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].phaseCode").value("REQUIREMENTS"))
                .andExpect(jsonPath("$[0].projectsInPhase").doesNotExist()));
        long initiativesBefore = dashboardBefore.get(0).get("initiativesInPhase").asLong();
        long pendingBefore = dashboardBefore.get(0).get("pendingApprovals").asLong();
        long analysisBefore = dashboardBefore.get(1).get("initiativesInPhase").asLong();

        // 4. Crear la iniciativa: arranca en REQUIREMENTS / IN_PROGRESS con el squad del proyecto.
        String initiativeId = readJson(perform(post("/api/v1/projects/{id}/initiatives", projectId),
                developerToken, Map.of("name", "Lote 1: apertura básica", "description", "Primer lote"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lote 1: apertura básica"))
                .andExpect(jsonPath("$.description").value("Primer lote"))
                .andExpect(jsonPath("$.project.id").value(projectId))
                .andExpect(jsonPath("$.project.name").value("Apertura de cuenta digital"))
                .andExpect(jsonPath("$.squad.id").value(squadId))
                .andExpect(jsonPath("$.squad.name").value("Squad Cuentas"))
                .andExpect(jsonPath("$.currentPhase").value("REQUIREMENTS"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.stories", hasSize(0)))
                .andExpect(jsonPath("$.journey", hasSize(5)))
                .andExpect(jsonPath("$.journey[0].phaseCode").value("REQUIREMENTS"))
                .andExpect(jsonPath("$.journey[0].progress").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.journey[0].approverRole").value("PRODUCT_OWNER"))
                .andExpect(jsonPath("$.journey[0].lastExecution", nullValue()))
                .andExpect(jsonPath("$.journey[1].progress").value("PENDING")))
                .get("id").asText();

        // Sin historias no se puede ejecutar la fase.
        perform(post("/api/v1/initiatives/{id}/phases/REQUIREMENTS/executions", initiativeId), developerToken,
                null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NO_STORIES"));

        // 5. Agregar dos historias a la iniciativa.
        String firstStoryId = readJson(perform(post("/api/v1/initiatives/{id}/stories", initiativeId),
                developerToken,
                Map.of("title", "Consultar saldo de la cuenta",
                        "description", "Como cliente del banco, quiero consultar el saldo de mi cuenta "
                                + "para saber cuánto dinero tengo disponible.",
                        "origin", "EXISTING"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.refined", nullValue())))
                .get("id").asText();
        perform(post("/api/v1/initiatives/{id}/stories", initiativeId), developerToken,
                Map.of("title", "Exportar movimientos", "description", "Debe ser rápido", "origin", "ASSISTED"))
                .andExpect(status().isCreated());

        // 6. Ejecutar la fase 1: queda pendiente de aprobación.
        JsonNode execution = readJson(
                perform(post("/api/v1/initiatives/{id}/phases/REQUIREMENTS/executions", initiativeId),
                        developerToken, null)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                        .andExpect(jsonPath("$.phaseCode").value("REQUIREMENTS"))
                        .andExpect(jsonPath("$.initiative.id").value(initiativeId))
                        .andExpect(jsonPath("$.initiative.name").value("Lote 1: apertura básica"))
                        .andExpect(jsonPath("$.project.id").value(projectId))
                        .andExpect(jsonPath("$.project.name").value("Apertura de cuenta digital"))
                        .andExpect(jsonPath("$.agent.name").value("Agente de refinamiento"))
                        .andExpect(jsonPath("$.agent.version").value("0.1.0"))
                        .andExpect(jsonPath("$.launchedBy.fullName").value("Luis Developer"))
                        .andExpect(jsonPath("$.createdAt", notNullValue()))
                        .andExpect(jsonPath("$.refinedStories", hasSize(2)))
                        .andExpect(jsonPath("$.refinedStories[0].storyId").value(firstStoryId))
                        .andExpect(jsonPath("$.refinedStories[0].originalTitle")
                                .value("Consultar saldo de la cuenta"))
                        .andExpect(jsonPath("$.refinedStories[0].refined.asA").value("cliente del banco"))
                        .andExpect(jsonPath("$.refinedStories[0].refined.iWant", notNullValue()))
                        .andExpect(jsonPath("$.refinedStories[0].refined.soThat", notNullValue()))
                        .andExpect(jsonPath("$.refinedStories[0].refined.acceptanceCriteria", hasSize(3)))
                        .andExpect(jsonPath("$.refinedStories[1].refined.ambiguities", hasSize(4)))
                        .andExpect(jsonPath("$.decision", nullValue())));
        String executionId = execution.get("id").asText();

        // La fase de la iniciativa queda AWAITING_GATE y las historias muestran la propuesta.
        perform(get("/api/v1/initiatives/{id}", initiativeId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journey[0].progress").value("AWAITING_GATE"))
                .andExpect(jsonPath("$.journey[0].lastExecution.id").value(executionId))
                .andExpect(jsonPath("$.journey[0].lastExecution.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.journey[0].lastExecution.initiative.id").value(initiativeId))
                .andExpect(jsonPath("$.stories", hasSize(2)))
                .andExpect(jsonPath("$.stories[0].refined.title").value("Consultar saldo de la cuenta"));

        // Con una ejecución por aprobar no se ejecuta de nuevo ni se tocan las historias.
        perform(post("/api/v1/initiatives/{id}/phases/REQUIREMENTS/executions", initiativeId), developerToken,
                null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHASE_AWAITING_GATE"));
        perform(delete("/api/v1/initiatives/{id}/stories/{storyId}", initiativeId, firstStoryId),
                developerToken, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHASE_AWAITING_GATE"));

        // 7. Bandeja de aprobaciones: la ve la Product Owner (squad del proyecto), no el desarrollador.
        perform(get("/api/v1/approvals/pending"), ownerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(executionId))
                .andExpect(jsonPath("$[0].initiative.id").value(initiativeId))
                .andExpect(jsonPath("$[0].project.id").value(projectId))
                .andExpect(jsonPath("$[0].refinedStories", hasSize(2)));
        perform(get("/api/v1/approvals/pending"), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        perform(get("/api/v1/executions").param("initiativeId", initiativeId)
                        .param("status", "PENDING_APPROVAL"), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(executionId));
        perform(get("/api/v1/executions").param("projectId", projectId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(executionId))
                .andExpect(jsonPath("$[0].initiative.name").value("Lote 1: apertura básica"))
                .andExpect(jsonPath("$[0].refinedStories").doesNotExist());
        perform(get("/api/v1/executions").param("initiativeId", initiativeId).param("status", "ACCEPTED"),
                developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        JsonNode dashboardPending = readJson(perform(get("/api/v1/dashboard/phases"), ownerToken, null)
                .andExpect(status().isOk()));
        assertThat(dashboardPending.get(0).get("initiativesInPhase").asLong()).isEqualTo(initiativesBefore + 1);
        assertThat(dashboardPending.get(0).get("pendingApprovals").asLong()).isEqualTo(pendingBefore + 1);

        // 8. Quien no es aprobador recibe 403.
        perform(post("/api/v1/executions/{id}/decision", executionId), developerToken,
                Map.of("decision", "APPROVE"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_GATE_APPROVER"));

        // Rechazar o pedir reproceso exige comentario.
        perform(post("/api/v1/executions/{id}/decision", executionId), ownerToken,
                Map.of("decision", "REQUEST_REWORK", "comment", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMENT_REQUIRED"));

        // 9. La Product Owner aprueba.
        perform(post("/api/v1/executions/{id}/decision", executionId), ownerToken,
                Map.of("decision", "APPROVE", "comment", "Listo para análisis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.initiative.id").value(initiativeId))
                .andExpect(jsonPath("$.decision.decision").value("APPROVE"))
                .andExpect(jsonPath("$.decision.decidedBy.id").value(ownerId))
                .andExpect(jsonPath("$.decision.decidedBy.fullName").value("Ana Product Owner"))
                .andExpect(jsonPath("$.decision.decidedAt", notNullValue()))
                .andExpect(jsonPath("$.decision.comment").value("Listo para análisis"));

        // Una ejecución ya decidida no se vuelve a decidir.
        perform(post("/api/v1/executions/{id}/decision", executionId), ownerToken,
                Map.of("decision", "REJECT", "comment", "Cambio de opinión"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXECUTION_ALREADY_DECIDED"));

        // 10. La iniciativa pasa a ANALYSIS; el proyecto la muestra en su resumen.
        perform(get("/api/v1/initiatives/{id}", initiativeId), ownerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPhase").value("ANALYSIS"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.journey[0].progress").value("APPROVED"))
                .andExpect(jsonPath("$.journey[0].lastExecution.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.journey[1].phaseCode").value("ANALYSIS"))
                .andExpect(jsonPath("$.journey[1].progress").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.stories[0].refined", notNullValue()));
        perform(get("/api/v1/projects/{id}", projectId), ownerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initiatives", hasSize(1)))
                .andExpect(jsonPath("$.initiatives[0].id").value(initiativeId))
                .andExpect(jsonPath("$.initiatives[0].name").value("Lote 1: apertura básica"))
                .andExpect(jsonPath("$.initiatives[0].project.id").value(projectId))
                .andExpect(jsonPath("$.initiatives[0].squad.name").value("Squad Cuentas"))
                .andExpect(jsonPath("$.initiatives[0].currentPhase").value("ANALYSIS"))
                .andExpect(jsonPath("$.initiatives[0].completed").value(false))
                .andExpect(jsonPath("$.initiatives[0].storyCount").value(2))
                .andExpect(jsonPath("$.initiatives[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.initiatives[0].stories").doesNotExist())
                .andExpect(jsonPath("$.initiatives[0].journey").doesNotExist());
        JsonNode projectSummary = findById(readJson(perform(get("/api/v1/projects"), ownerToken, null)
                .andExpect(status().isOk())), projectId);
        assertThat(projectSummary.get("name").asText()).isEqualTo("Apertura de cuenta digital");
        assertThat(projectSummary.get("description").asText()).isEqualTo("Piloto");
        assertThat(projectSummary.get("squad").get("name").asText()).isEqualTo("Squad Cuentas");
        assertThat(projectSummary.get("initiativeCount").asInt()).isEqualTo(1);
        assertThat(projectSummary.has("currentPhase")).isFalse();
        assertThat(projectSummary.has("storyCount")).isFalse();

        JsonNode dashboardAfter = readJson(perform(get("/api/v1/dashboard/phases"), ownerToken, null)
                .andExpect(status().isOk()));
        assertThat(dashboardAfter.get(0).get("initiativesInPhase").asLong()).isEqualTo(initiativesBefore);
        assertThat(dashboardAfter.get(0).get("pendingApprovals").asLong()).isEqualTo(pendingBefore);
        assertThat(dashboardAfter.get(1).get("initiativesInPhase").asLong()).isEqualTo(analysisBefore + 1);

        perform(get("/api/v1/approvals/pending"), ownerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        perform(post("/api/v1/initiatives/{id}/phases/REQUIREMENTS/executions", initiativeId), developerToken,
                null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHASE_ALREADY_APPROVED"));
        perform(post("/api/v1/initiatives/{id}/stories", initiativeId), developerToken,
                Map.of("title", "Historia tardía", "origin", "EXISTING"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHASE_ALREADY_APPROVED"));
    }

    @Test
    void initiativesOfAProjectAdvanceThroughPhasesIndependently() throws Exception {
        JsonNode owner = register("Olga Product Owner", "olga.po@example.com");
        JsonNode developer = register("Dario Developer", "dario.dev@example.com");
        String ownerToken = owner.get("token").asText();
        String developerToken = developer.get("token").asText();

        String squadId = readJson(perform(post("/api/v1/squads"), ownerToken, Map.of("name", "Squad Pagos"))
                .andExpect(status().isCreated()))
                .get("id").asText();
        perform(post("/api/v1/squads/{id}/members", squadId), ownerToken,
                Map.of("userId", owner.get("user").get("id").asText(), "role", "PRODUCT_OWNER",
                        "substitute", false))
                .andExpect(status().isCreated());
        String projectId = readJson(perform(post("/api/v1/projects"), developerToken,
                Map.of("name", "Pagos con QR", "squadId", squadId))
                .andExpect(status().isCreated()))
                .get("id").asText();

        // Dos iniciativas en el mismo proyecto: las dos arrancan en REQUIREMENTS.
        String firstId = createInitiative(projectId, "Lote 1", developerToken);
        String secondId = createInitiative(projectId, "Lote 2", developerToken);

        // Solo la primera tiene historias y se ejecuta.
        perform(post("/api/v1/initiatives/{id}/stories", firstId), developerToken,
                Map.of("title", "Pagar con QR", "description", "Como cliente quiero pagar con QR.",
                        "origin", "EXISTING"))
                .andExpect(status().isCreated());
        String executionId = readJson(
                perform(post("/api/v1/initiatives/{id}/phases/REQUIREMENTS/executions", firstId),
                        developerToken, null)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                        .andExpect(jsonPath("$.initiative.id").value(firstId))
                        .andExpect(jsonPath("$.initiative.name").value("Lote 1"))
                        .andExpect(jsonPath("$.project.id").value(projectId))
                        .andExpect(jsonPath("$.refinedStories", hasSize(1))))
                .get("id").asText();

        // La segunda no se ve afectada: sigue editable y sin historias no se ejecuta.
        perform(post("/api/v1/initiatives/{id}/phases/REQUIREMENTS/executions", secondId), developerToken,
                null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NO_STORIES"));
        perform(post("/api/v1/initiatives/{id}/stories", secondId), developerToken,
                Map.of("title", "Generar QR de cobro", "origin", "ASSISTED"))
                .andExpect(status().isCreated());
        perform(get("/api/v1/initiatives/{id}", secondId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPhase").value("REQUIREMENTS"))
                .andExpect(jsonPath("$.journey[0].progress").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.journey[0].lastExecution", nullValue()))
                .andExpect(jsonPath("$.stories", hasSize(1)));

        // Se aprueba la primera: pasa a ANALYSIS; la segunda sigue en REQUIREMENTS.
        perform(post("/api/v1/executions/{id}/decision", executionId), ownerToken,
                Map.of("decision", "APPROVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
        perform(get("/api/v1/initiatives/{id}", firstId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPhase").value("ANALYSIS"))
                .andExpect(jsonPath("$.journey[0].progress").value("APPROVED"))
                .andExpect(jsonPath("$.journey[1].progress").value("IN_PROGRESS"));
        perform(get("/api/v1/initiatives/{id}", secondId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPhase").value("REQUIREMENTS"))
                .andExpect(jsonPath("$.journey[0].progress").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.journey[1].progress").value("PENDING"));

        // El proyecto lista sus dos iniciativas, cada una en su fase.
        JsonNode project = readJson(perform(get("/api/v1/projects/{id}", projectId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initiatives", hasSize(2))));
        assertThat(findById(project.get("initiatives"), firstId).get("currentPhase").asText())
                .isEqualTo("ANALYSIS");
        assertThat(findById(project.get("initiatives"), secondId).get("currentPhase").asText())
                .isEqualTo("REQUIREMENTS");
        assertThat(findById(readJson(perform(get("/api/v1/projects"), developerToken, null)
                .andExpect(status().isOk())), projectId).get("initiativeCount").asInt()).isEqualTo(2);

        // GET /initiatives filtra por proyecto y por fase actual.
        perform(get("/api/v1/initiatives").param("projectId", projectId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        perform(get("/api/v1/initiatives").param("projectId", projectId).param("phase", "ANALYSIS"),
                developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(firstId))
                .andExpect(jsonPath("$[0].name").value("Lote 1"))
                .andExpect(jsonPath("$[0].project.id").value(projectId))
                .andExpect(jsonPath("$[0].project.name").value("Pagos con QR"))
                .andExpect(jsonPath("$[0].squad.name").value("Squad Pagos"))
                .andExpect(jsonPath("$[0].currentPhase").value("ANALYSIS"))
                .andExpect(jsonPath("$[0].completed").value(false))
                .andExpect(jsonPath("$[0].storyCount").value(1))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()));
        perform(get("/api/v1/initiatives").param("projectId", projectId).param("phase", "REQUIREMENTS"),
                developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(secondId));
        JsonNode allInitiatives = readJson(perform(get("/api/v1/initiatives"), developerToken, null)
                .andExpect(status().isOk()));
        assertThat(findById(allInitiatives, firstId)).isNotNull();
        assertThat(findById(allInitiatives, secondId)).isNotNull();

        // Las ejecuciones se filtran por iniciativa o por proyecto.
        perform(get("/api/v1/executions").param("initiativeId", secondId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        perform(get("/api/v1/executions").param("projectId", projectId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(executionId));

        // La segunda iniciativa recorre su propia fase 1 y llega a la bandeja de la misma Product Owner.
        String secondExecutionId = readJson(
                perform(post("/api/v1/initiatives/{id}/phases/REQUIREMENTS/executions", secondId),
                        developerToken, null)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.initiative.id").value(secondId)))
                .get("id").asText();
        perform(get("/api/v1/approvals/pending"), ownerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(secondExecutionId))
                .andExpect(jsonPath("$[0].initiative.name").value("Lote 2"));
        perform(get("/api/v1/executions").param("projectId", projectId), developerToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void initiativeNameIsUniqueWithinItsProject() throws Exception {
        String token = register("Nora Nombres", "nora.nombres@example.com").get("token").asText();
        String squadId = readJson(perform(post("/api/v1/squads"), token, Map.of("name", "Squad Nombres"))
                .andExpect(status().isCreated()))
                .get("id").asText();
        String projectId = readJson(perform(post("/api/v1/projects"), token,
                Map.of("name", "Proyecto nombres A", "squadId", squadId))
                .andExpect(status().isCreated()))
                .get("id").asText();
        String otherProjectId = readJson(perform(post("/api/v1/projects"), token,
                Map.of("name", "Proyecto nombres B", "squadId", squadId))
                .andExpect(status().isCreated()))
                .get("id").asText();

        createInitiative(projectId, "Migración de saldos", token);

        // Mismo nombre en el mismo proyecto (aunque cambien mayúsculas o espacios): 409.
        perform(post("/api/v1/projects/{id}/initiatives", projectId), token,
                Map.of("name", "Migración de saldos"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_NAME"));
        perform(post("/api/v1/projects/{id}/initiatives", projectId), token,
                Map.of("name", "  MIGRACIÓN DE SALDOS "))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_NAME"));
        perform(get("/api/v1/initiatives").param("projectId", projectId), token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // El mismo nombre en otro proyecto sí se permite.
        createInitiative(otherProjectId, "Migración de saldos", token);

        // Nombre vacío: 400; proyecto o iniciativa inexistentes: 404.
        perform(post("/api/v1/projects/{id}/initiatives", projectId), token, Map.of("name", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        perform(post("/api/v1/projects/{id}/initiatives", "00000000-0000-0000-0000-000000000000"), token,
                Map.of("name", "Sin proyecto"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        perform(get("/api/v1/initiatives/{id}", "00000000-0000-0000-0000-000000000000"), token, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        perform(post("/api/v1/initiatives/{id}/stories", "00000000-0000-0000-0000-000000000000"), token,
                Map.of("title", "Historia", "origin", "EXISTING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        perform(get("/api/v1/initiatives").param("phase", "NO_EXISTE"), token, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void seedDataAndErrorShapes() throws Exception {
        // Sin token: 401 con la forma de error del contrato.
        mockMvc.perform(get("/api/v1/phases"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.details", hasSize(0)));

        String token = register("María Semilla", "maria.semilla@example.com").get("token").asText();

        // Correo repetido y credenciales inválidas.
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("fullName", "Otra",
                                "email", "MARIA.SEMILLA@example.com", "password", "secreto123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "maria.semilla@example.com",
                                "password", "incorrecta"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "maria.semilla@example.com",
                                "password", "secreto123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email").value("maria.semilla@example.com"));

        // Datos inválidos: 400 VALIDATION_ERROR con detalle por campo.
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("fullName", "",
                                "email", "no-es-correo", "password", "corta"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(3)));

        // Las 5 fases semilla en orden, con el agente de refinamiento en la primera.
        perform(get("/api/v1/phases"), token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].code").value("REQUIREMENTS"))
                .andExpect(jsonPath("$[0].order").value(1))
                .andExpect(jsonPath("$[0].name").value("Requerimientos"))
                .andExpect(jsonPath("$[0].approverRole").value("PRODUCT_OWNER"))
                .andExpect(jsonPath("$[0].agents", hasSize(1)))
                .andExpect(jsonPath("$[0].agents[0].name").value("Agente de refinamiento"))
                .andExpect(jsonPath("$[0].agents[0].status").value("PILOT"))
                .andExpect(jsonPath("$[0].agents[0].currentVersion").value("0.1.0"))
                .andExpect(jsonPath("$[1].code").value("ANALYSIS"))
                .andExpect(jsonPath("$[1].approverRole").value("TECH_LEAD"))
                .andExpect(jsonPath("$[4].code").value("TESTING"))
                .andExpect(jsonPath("$[4].name").value("Pruebas"));

        // Catálogo de agentes: el listado no trae instrucciones; el detalle sí.
        JsonNode agents = readJson(perform(get("/api/v1/agents").param("phase", "REQUIREMENTS"), token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].versions[0].version").value("0.1.0"))
                .andExpect(jsonPath("$[0].versions[0].instructions").doesNotExist()));
        String seedAgentId = agents.get(0).get("id").asText();
        perform(get("/api/v1/agents/{id}", seedAgentId), token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versions[0].instructions", notNullValue()));

        // Un agente nuevo queda PLANNED sin versión; al registrar una versión pasa a ser la vigente.
        String agentId = readJson(perform(post("/api/v1/agents"), token,
                Map.of("name", "Agente de análisis", "phaseCode", "ANALYSIS", "description", "Borrador"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.currentVersion", nullValue()))
                .andExpect(jsonPath("$.owner.fullName").value("María Semilla"))
                .andExpect(jsonPath("$.versions", hasSize(0))))
                .get("id").asText();
        perform(post("/api/v1/agents/{id}/versions", agentId), token,
                Map.of("version", "1.0.0", "commitSha", "abc1234", "instructions", "# Agente",
                        "changeNotes", "Primera versión"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentVersion").value("1.0.0"))
                .andExpect(jsonPath("$.versions", hasSize(1)));
        perform(post("/api/v1/agents"), token, Map.of("name", "Agente de análisis", "phaseCode", "ANALYSIS"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_NAME"));

        // Recurso inexistente.
        perform(get("/api/v1/projects/{id}", "00000000-0000-0000-0000-000000000000"), token, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // --- Apoyo --------------------------------------------------------------------------------

    private JsonNode register(String fullName, String email) throws Exception {
        return readJson(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("fullName", fullName, "email", email, "password", "secreto123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.fullName").value(fullName))
                .andExpect(jsonPath("$.user.memberships", hasSize(0))));
    }

    /** Crea una iniciativa y verifica que arranca en REQUIREMENTS; devuelve su id. */
    private String createInitiative(String projectId, String name, String token) throws Exception {
        return readJson(perform(post("/api/v1/projects/{id}/initiatives", projectId), token,
                Map.of("name", name))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.project.id").value(projectId))
                .andExpect(jsonPath("$.currentPhase").value("REQUIREMENTS"))
                .andExpect(jsonPath("$.journey[0].progress").value("IN_PROGRESS")))
                .get("id").asText();
    }

    /** Busca en un arreglo JSON el elemento con ese id; falla si no está. */
    private static JsonNode findById(JsonNode array, String id) {
        for (JsonNode node : array) {
            if (id.equals(node.get("id").asText())) {
                return node;
            }
        }
        throw new AssertionError("No se encontró el elemento con id " + id);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, String token, Object body)
            throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        }
        return mockMvc.perform(request);
    }

    private JsonNode readJson(ResultActions actions) throws Exception {
        return objectMapper.readTree(actions.andReturn().getResponse().getContentAsByteArray());
    }
}
