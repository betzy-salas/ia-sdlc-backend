package com.iasdlc.application.port.out;

/** Falla del motor de agentes (sin resultado utilizable). */
public class AgentRunnerException extends RuntimeException {

    public AgentRunnerException(String message) {
        super(message);
    }

    public AgentRunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
