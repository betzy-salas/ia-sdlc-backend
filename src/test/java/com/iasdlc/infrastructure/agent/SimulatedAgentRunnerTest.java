package com.iasdlc.infrastructure.agent;

import com.iasdlc.application.port.out.AgentDefinition;
import com.iasdlc.application.port.out.AgentInput;
import com.iasdlc.application.port.out.AgentOutput;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.RefinedStory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedAgentRunnerTest {

    private final SimulatedAgentRunner runner = new SimulatedAgentRunner();
    private final AgentDefinition agent = new AgentDefinition(UUID.randomUUID(), "Agente de refinamiento",
            PhaseCode.REQUIREMENTS, "0.1.0", "instrucciones", null, null);

    @Test
    void refinesAWellWrittenStory() {
        RefinedStory refined = runner.refine("  consultar saldo de la cuenta. ",
                "Como cliente del banco, quiero consultar el saldo de mi cuenta de ahorros "
                        + "para saber cuánto dinero tengo disponible.");

        assertThat(refined.title()).isEqualTo("Consultar saldo de la cuenta");
        assertThat(refined.asA()).isEqualTo("cliente del banco");
        assertThat(refined.iWant()).isEqualTo("consultar el saldo de mi cuenta de ahorros");
        assertThat(refined.soThat()).isEqualTo("saber cuánto dinero tengo disponible");
        assertThat(refined.acceptanceCriteria()).hasSize(3).allSatisfy(criterion ->
                assertThat(criterion).startsWith("Dado").contains("Cuando").contains("Entonces"));
        assertThat(refined.ambiguities()).isEmpty();
    }

    @Test
    void flagsAmbiguitiesOnAShortStoryWithoutActor() {
        RefinedStory refined = runner.refine("Exportar reporte", "Debe ser rápido");

        assertThat(refined.asA()).isEqualTo("usuario del sistema");
        assertThat(refined.iWant()).isEqualTo("exportar reporte");
        assertThat(refined.acceptanceCriteria()).hasSize(3);
        assertThat(refined.ambiguities()).hasSize(4);
        assertThat(refined.ambiguities()).anySatisfy(a -> assertThat(a).contains("muy corta"));
        assertThat(refined.ambiguities()).anySatisfy(a -> assertThat(a).contains("actor"));
        assertThat(refined.ambiguities()).anySatisfy(a -> assertThat(a).contains("beneficio"));
        assertThat(refined.ambiguities()).anySatisfy(a -> assertThat(a).contains("rápido"));
    }

    @Test
    void handlesAStoryWithoutDescription() {
        RefinedStory refined = runner.refine("Bloquear tarjeta", null);

        assertThat(refined.title()).isEqualTo("Bloquear tarjeta");
        assertThat(refined.ambiguities()).anySatisfy(a -> assertThat(a).contains("no tiene descripción"));
    }

    @Test
    void returnsOneResultPerStoryAndIsDeterministic() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        AgentInput input = new AgentInput(UUID.randomUUID(), "Proyecto", null, List.of(
                new AgentInput.StoryInput(first, "Pagar servicio", "Como cliente quiero pagar un servicio."),
                new AgentInput.StoryInput(second, "Ver movimientos", "")));

        AgentOutput output = runner.run(agent, input);

        assertThat(output.refinedStories()).extracting(AgentOutput.RefinedStoryOutput::storyId)
                .containsExactly(first, second);
        assertThat(runner.run(agent, input)).isEqualTo(output);
    }
}
