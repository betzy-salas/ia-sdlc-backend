package com.iasdlc.infrastructure.agent;

import com.iasdlc.application.port.out.AgentDefinition;
import com.iasdlc.application.port.out.AgentInput;
import com.iasdlc.application.port.out.AgentOutput;
import com.iasdlc.application.port.out.AgentRunner;
import com.iasdlc.domain.model.RefinedStory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adaptador simulado del motor de agentes: determinista y sin red.
 *
 * <p>A partir del título y la descripción de cada historia arma una historia refinada plausible
 * (como / quiero / para, tres criterios Dado/Cuando/Entonces y una lista de ambigüedades).
 * No interpreta las instrucciones del agente; sirve para recorrer el flujo completo mientras
 * se conecta un motor real. Se activa con {@code iasdlc.agent.runner=simulated} (valor por defecto).</p>
 */
@Component
@ConditionalOnProperty(name = "iasdlc.agent.runner", havingValue = "simulated", matchIfMissing = true)
public class SimulatedAgentRunner implements AgentRunner {

    private static final int SHORT_DESCRIPTION_LENGTH = 60;
    private static final String DEFAULT_ACTOR = "usuario del sistema";

    private static final String WANT_VERBS = "(?:quiero|necesito|deseo|quisiera|requiero)";
    private static final Pattern ACTOR = Pattern.compile(
            "(?iu)\\bcomo\\s+(?:un\\s+|una\\s+|el\\s+|la\\s+)?(.+?)(?=\\s*,|\\s+" + WANT_VERBS + "\\b|[.;]|$)");
    private static final Pattern WANT = Pattern.compile(
            "(?iu)\\b" + WANT_VERBS + "\\s+(.+?)(?=\\s*,?\\s+para\\b|[.;]|$)");
    private static final Pattern BENEFIT = Pattern.compile("(?iu)\\bpara\\s+(?:que\\s+)?(.+?)(?=[.;]|$)");

    private static final List<String> KNOWN_ACTORS = List.of("cliente", "usuario", "administrador", "analista",
            "asesor", "operador", "supervisor", "gerente", "vendedor", "comprador", "auditor");
    private static final List<String> VAGUE_TERMS = List.of("rápido", "rápida", "rápidamente", "fácil",
            "fácilmente", "intuitivo", "intuitiva", "amigable", "eficiente", "óptimo", "óptima", "moderno",
            "moderna", "flexible", "etc");

    @Override
    public AgentOutput run(AgentDefinition agent, AgentInput input) {
        List<AgentOutput.RefinedStoryOutput> refined = input.stories().stream()
                .map(story -> new AgentOutput.RefinedStoryOutput(story.storyId(),
                        refine(story.title(), story.description())))
                .toList();
        return new AgentOutput(refined);
    }

    RefinedStory refine(String rawTitle, String rawDescription) {
        String title = cleanTitle(rawTitle);
        String description = rawDescription == null ? "" : collapseSpaces(rawDescription);
        List<String> ambiguities = new ArrayList<>();

        if (description.isEmpty()) {
            ambiguities.add("La historia no tiene descripción; el refinamiento se basó solo en el título.");
        } else if (description.length() < SHORT_DESCRIPTION_LENGTH) {
            ambiguities.add("La descripción es muy corta (" + description.length()
                    + " caracteres); falta contexto sobre el flujo y las reglas de negocio.");
        }

        // Actor: "como <actor>" en la descripción; si no, un actor conocido mencionado en el texto.
        String asA = firstGroup(ACTOR, description, 0);
        if (asA == null) {
            asA = findKnownActor(title + " " + description);
        }
        if (asA == null) {
            asA = DEFAULT_ACTOR;
            ambiguities.add("No se menciona el actor o rol que usa la funcionalidad; se asumió \""
                    + DEFAULT_ACTOR + "\".");
        }

        // Necesidad: "quiero <algo>"; si no, se deriva del título.
        Matcher wantMatcher = WANT.matcher(description);
        String iWant;
        int benefitFrom = 0;
        if (wantMatcher.find()) {
            iWant = stripTrailingPunctuation(wantMatcher.group(1));
            benefitFrom = wantMatcher.end(1);
        } else {
            iWant = lowerFirst(title);
        }

        // Beneficio: "para <algo>" después de la necesidad.
        String soThat = firstGroup(BENEFIT, description, benefitFrom);
        if (soThat == null) {
            soThat = "lograr el objetivo descrito en la historia";
            ambiguities.add("No se indica el beneficio o valor de negocio (el \"para qué\").");
        }

        List<String> vague = findVagueTerms(title + " " + description);
        if (!vague.isEmpty()) {
            ambiguities.add("Contiene términos subjetivos sin un criterio medible: "
                    + String.join(", ", vague) + ".");
        }

        List<String> criteria = List.of(
                "Dado que el " + asA + " tiene acceso a la funcionalidad, Cuando solicita \"" + title
                        + "\" con datos válidos, Entonces el sistema completa la operación y muestra una"
                        + " confirmación.",
                "Dado que el " + asA + " ingresa datos incompletos o inválidos, Cuando intenta \"" + title
                        + "\", Entonces el sistema rechaza la operación e indica qué dato debe corregir.",
                "Dado que la operación \"" + title + "\" terminó correctamente, Cuando el " + asA
                        + " consulta el resultado, Entonces la información aparece actualizada y queda"
                        + " registro de quién la realizó y cuándo.");

        return new RefinedStory(title, asA, iWant, soThat, criteria, ambiguities);
    }

    private static String cleanTitle(String rawTitle) {
        String title = stripTrailingPunctuation(collapseSpaces(rawTitle == null ? "" : rawTitle));
        if (title.isEmpty()) {
            return "Historia sin título";
        }
        return title.substring(0, 1).toUpperCase(Locale.ROOT) + title.substring(1);
    }

    private static String collapseSpaces(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    private static String stripTrailingPunctuation(String text) {
        return text.replaceAll("[\\s.,;:!]+$", "").trim();
    }

    private static String lowerFirst(String text) {
        return text.isEmpty() ? text : text.substring(0, 1).toLowerCase(Locale.ROOT) + text.substring(1);
    }

    private static String firstGroup(Pattern pattern, String text, int from) {
        Matcher matcher = pattern.matcher(text);
        if (from <= text.length() && matcher.find(from)) {
            String value = stripTrailingPunctuation(matcher.group(1));
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    private static String findKnownActor(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        return KNOWN_ACTORS.stream()
                .filter(actor -> Pattern.compile("(?U)\\b" + actor + "\\b").matcher(lower).find())
                .findFirst()
                .orElse(null);
    }

    private static List<String> findVagueTerms(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        return VAGUE_TERMS.stream()
                .filter(term -> Pattern.compile("(?U)\\b" + Pattern.quote(term) + "\\b").matcher(lower).find())
                .toList();
    }
}
