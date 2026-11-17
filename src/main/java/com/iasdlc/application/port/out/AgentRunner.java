package com.iasdlc.application.port.out;

/**
 * Puerto de salida hacia el motor que ejecuta los agentes.
 *
 * <p>Los casos de uso solo conocen esta interfaz. Hoy la implementa un adaptador simulado
 * (SimulatedAgentRunner); más adelante se puede conectar un motor real (por ejemplo el
 * GitHub Copilot SDK para Java) agregando otro adaptador en infrastructure.agent, sin tocar
 * el dominio ni los casos de uso.</p>
 */
public interface AgentRunner {

    /**
     * Ejecuta el agente sobre la entrada y devuelve su propuesta.
     *
     * @throws AgentRunnerException si el motor no pudo producir un resultado; la ejecución queda FAILED
     */
    AgentOutput run(AgentDefinition agent, AgentInput input);
}
