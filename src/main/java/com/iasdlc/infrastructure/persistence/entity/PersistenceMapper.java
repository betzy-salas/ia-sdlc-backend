package com.iasdlc.infrastructure.persistence.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iasdlc.domain.model.Agent;
import com.iasdlc.domain.model.AgentVersion;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.GateDecision;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.RefinedStory;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.domain.model.SquadMember;
import com.iasdlc.domain.model.Story;
import com.iasdlc.domain.model.StoryRefinement;
import com.iasdlc.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Convierte entre el modelo de dominio y las entidades JPA. Vive en el mismo paquete que las
 * entidades para usar sus campos directamente (las entidades no exponen getters ni setters).
 */
@Component
public class PersistenceMapper {

    private static final TypeReference<List<StoryRefinement>> REFINEMENTS = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public PersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // --- Usuarios ---------------------------------------------------------------------------

    public UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.id = user.id();
        entity.fullName = user.fullName();
        entity.email = user.email();
        entity.passwordHash = user.passwordHash();
        entity.createdAt = user.createdAt();
        return entity;
    }

    public User toDomain(UserEntity entity) {
        return new User(entity.id, entity.fullName, entity.email, entity.passwordHash, entity.createdAt);
    }

    // --- Squads -----------------------------------------------------------------------------

    public SquadEntity toEntity(Squad squad) {
        SquadEntity entity = new SquadEntity();
        entity.id = squad.id();
        entity.name = squad.name();
        entity.createdAt = squad.createdAt();
        int index = 0;
        for (SquadMember member : squad.members()) {
            SquadMemberEntity memberEntity = new SquadMemberEntity();
            memberEntity.id = member.id();
            memberEntity.squad = entity;
            memberEntity.userId = member.userId();
            memberEntity.role = member.role();
            memberEntity.substitute = member.substitute();
            memberEntity.sortIndex = index++;
            entity.members.add(memberEntity);
        }
        return entity;
    }

    public Squad toDomain(SquadEntity entity) {
        List<SquadMember> members = entity.members.stream()
                .map(m -> new SquadMember(m.id, m.userId, m.role, m.substitute))
                .toList();
        return new Squad(entity.id, entity.name, entity.createdAt, members);
    }

    // --- Fases ------------------------------------------------------------------------------

    public Phase toDomain(PhaseEntity entity) {
        return new Phase(entity.code, entity.sortOrder, entity.name, entity.approverRole);
    }

    // --- Agentes ----------------------------------------------------------------------------

    public AgentEntity toEntity(Agent agent) {
        AgentEntity entity = new AgentEntity();
        entity.id = agent.id();
        entity.name = agent.name();
        entity.phaseCode = agent.phaseCode();
        entity.status = agent.status();
        entity.currentVersion = agent.currentVersion();
        entity.description = agent.description();
        entity.repositoryUrl = agent.repositoryUrl();
        entity.repositoryPath = agent.repositoryPath();
        entity.ownerUserId = agent.ownerUserId();
        entity.createdAt = agent.createdAt();
        int index = 0;
        for (AgentVersion version : agent.versions()) {
            AgentVersionEntity versionEntity = new AgentVersionEntity();
            versionEntity.id = version.id();
            versionEntity.agent = entity;
            versionEntity.version = version.version();
            versionEntity.commitSha = version.commitSha();
            versionEntity.instructions = version.instructions();
            versionEntity.changeNotes = version.changeNotes();
            versionEntity.createdAt = version.createdAt();
            versionEntity.sortIndex = index++;
            entity.versions.add(versionEntity);
        }
        return entity;
    }

    public Agent toDomain(AgentEntity entity) {
        List<AgentVersion> versions = entity.versions.stream()
                .map(v -> new AgentVersion(v.id, v.version, v.commitSha, v.instructions, v.changeNotes,
                        v.createdAt))
                .toList();
        return new Agent(entity.id, entity.name, entity.phaseCode, entity.status, entity.currentVersion,
                entity.description, entity.repositoryUrl, entity.repositoryPath, entity.ownerUserId,
                entity.createdAt, versions);
    }

    // --- Proyectos --------------------------------------------------------------------------

    public ProjectEntity toEntity(Project project) {
        ProjectEntity entity = new ProjectEntity();
        entity.id = project.id();
        entity.name = project.name();
        entity.description = project.description();
        entity.squadId = project.squadId();
        entity.createdAt = project.createdAt();
        return entity;
    }

    public Project toDomain(ProjectEntity entity) {
        return new Project(entity.id, entity.name, entity.description, entity.squadId, entity.createdAt);
    }

    // --- Iniciativas ------------------------------------------------------------------------

    public InitiativeEntity toEntity(Initiative initiative) {
        InitiativeEntity entity = new InitiativeEntity();
        entity.id = initiative.id();
        entity.projectId = initiative.projectId();
        entity.name = initiative.name();
        entity.description = initiative.description();
        entity.currentPhase = initiative.currentPhase();
        entity.createdAt = initiative.createdAt();
        entity.phases = new HashMap<>(initiative.progress());
        int index = 0;
        for (Story story : initiative.stories()) {
            StoryEntity storyEntity = new StoryEntity();
            storyEntity.id = story.id();
            storyEntity.initiative = entity;
            storyEntity.title = story.title();
            storyEntity.description = story.description();
            storyEntity.origin = story.origin();
            storyEntity.refinedJson = story.refined() == null ? null : writeJson(story.refined());
            storyEntity.sortIndex = index++;
            storyEntity.createdAt = story.createdAt();
            entity.stories.add(storyEntity);
        }
        return entity;
    }

    public Initiative toDomain(InitiativeEntity entity) {
        List<Story> stories = entity.stories.stream()
                .map(s -> new Story(s.id, s.title, s.description, s.origin,
                        s.refinedJson == null ? null : readJson(s.refinedJson, RefinedStory.class),
                        s.createdAt))
                .toList();
        return new Initiative(entity.id, entity.projectId, entity.name, entity.description, entity.currentPhase,
                entity.createdAt, stories, new HashMap<>(entity.phases));
    }

    // --- Ejecuciones ------------------------------------------------------------------------

    public ExecutionEntity toEntity(Execution execution) {
        ExecutionEntity entity = new ExecutionEntity();
        entity.id = execution.id();
        entity.initiativeId = execution.initiativeId();
        entity.phaseCode = execution.phaseCode();
        entity.agentId = execution.agentId();
        entity.agentName = execution.agentName();
        entity.agentVersion = execution.agentVersion();
        entity.launchedBy = execution.launchedBy();
        entity.status = execution.status();
        entity.createdAt = execution.createdAt();
        entity.refinedStoriesJson = writeJson(execution.refinedStories());
        entity.failureReason = execution.failureReason();
        GateDecision decision = execution.decision();
        if (decision != null) {
            entity.decision = decision.decision();
            entity.decidedBy = decision.decidedBy();
            entity.decidedAt = decision.decidedAt();
            entity.decisionComment = decision.comment();
        }
        return entity;
    }

    public Execution toDomain(ExecutionEntity entity) {
        GateDecision decision = entity.decision == null ? null
                : new GateDecision(entity.decision, entity.decidedBy, entity.decidedAt, entity.decisionComment);
        List<StoryRefinement> refinements;
        try {
            refinements = objectMapper.readValue(entity.refinedStoriesJson, REFINEMENTS);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo leer el resultado guardado de la ejecución", e);
        }
        return new Execution(entity.id, entity.initiativeId, entity.phaseCode, entity.agentId, entity.agentName,
                entity.agentVersion, entity.launchedBy, entity.status, entity.createdAt, refinements, decision,
                entity.failureReason);
    }

    // --- JSON -------------------------------------------------------------------------------

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el resultado del agente", e);
        }
    }

    private <T> T readJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo leer el resultado guardado del agente", e);
        }
    }
}
