-- Esquema de la entrega 1. SQL compatible con PostgreSQL y con H2 en modo PostgreSQL.

CREATE TABLE users (
    id            UUID PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(200) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE squads (
    id         UUID PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_squads_name UNIQUE (name)
);

-- Un usuario puede tener varios roles en un squad, pero cada rol una sola vez.
CREATE TABLE squad_members (
    id         UUID PRIMARY KEY,
    squad_id   UUID NOT NULL,
    user_id    UUID NOT NULL,
    role       VARCHAR(30) NOT NULL,
    substitute BOOLEAN NOT NULL,
    sort_index INTEGER NOT NULL,
    CONSTRAINT fk_squad_members_squad FOREIGN KEY (squad_id) REFERENCES squads (id),
    CONSTRAINT fk_squad_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_squad_members_role UNIQUE (squad_id, user_id, role)
);

CREATE TABLE phases (
    code          VARCHAR(30) PRIMARY KEY,
    sort_order    INTEGER NOT NULL,
    name          VARCHAR(80) NOT NULL,
    approver_role VARCHAR(30) NOT NULL
);

CREATE TABLE agents (
    id              UUID PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    phase_code      VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    current_version VARCHAR(30),
    description     TEXT,
    repository_url  VARCHAR(500),
    repository_path VARCHAR(500),
    owner_user_id   UUID,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_agents_phase FOREIGN KEY (phase_code) REFERENCES phases (code),
    CONSTRAINT fk_agents_owner FOREIGN KEY (owner_user_id) REFERENCES users (id),
    CONSTRAINT uk_agents_name UNIQUE (name)
);

CREATE TABLE agent_versions (
    id           UUID PRIMARY KEY,
    agent_id     UUID NOT NULL,
    version      VARCHAR(30) NOT NULL,
    commit_sha   VARCHAR(64),
    instructions TEXT NOT NULL,
    change_notes TEXT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    sort_index   INTEGER NOT NULL,
    CONSTRAINT fk_agent_versions_agent FOREIGN KEY (agent_id) REFERENCES agents (id),
    CONSTRAINT uk_agent_versions UNIQUE (agent_id, version)
);

CREATE TABLE projects (
    id            UUID PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    description   TEXT,
    squad_id      UUID NOT NULL,
    current_phase VARCHAR(30) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_projects_squad FOREIGN KEY (squad_id) REFERENCES squads (id),
    CONSTRAINT fk_projects_phase FOREIGN KEY (current_phase) REFERENCES phases (code),
    CONSTRAINT uk_projects_name UNIQUE (name)
);

-- Avance de cada fase dentro de un proyecto.
CREATE TABLE project_phases (
    project_id UUID NOT NULL,
    phase_code VARCHAR(30) NOT NULL,
    progress   VARCHAR(20) NOT NULL,
    CONSTRAINT pk_project_phases PRIMARY KEY (project_id, phase_code),
    CONSTRAINT fk_project_phases_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_phases_phase FOREIGN KEY (phase_code) REFERENCES phases (code)
);

-- refined_json guarda la historia refinada vigente (JSON) o NULL.
CREATE TABLE stories (
    id           UUID PRIMARY KEY,
    project_id   UUID NOT NULL,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    origin       VARCHAR(20) NOT NULL,
    refined_json TEXT,
    sort_index   INTEGER NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_stories_project FOREIGN KEY (project_id) REFERENCES projects (id)
);

-- Registro histórico: las ejecuciones y sus decisiones no se borran.
-- refined_stories_json guarda el resultado del agente (JSON); no referencia a stories
-- para conservar el resultado aunque luego se borre una historia.
CREATE TABLE executions (
    id                   UUID PRIMARY KEY,
    project_id           UUID NOT NULL,
    phase_code           VARCHAR(30) NOT NULL,
    agent_id             UUID NOT NULL,
    agent_name           VARCHAR(120) NOT NULL,
    agent_version        VARCHAR(30) NOT NULL,
    launched_by          UUID NOT NULL,
    status               VARCHAR(20) NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    refined_stories_json TEXT NOT NULL,
    failure_reason       TEXT,
    decision             VARCHAR(20),
    decided_by           UUID,
    decided_at           TIMESTAMP WITH TIME ZONE,
    decision_comment     TEXT,
    CONSTRAINT fk_executions_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_executions_phase FOREIGN KEY (phase_code) REFERENCES phases (code),
    CONSTRAINT fk_executions_agent FOREIGN KEY (agent_id) REFERENCES agents (id),
    CONSTRAINT fk_executions_launched_by FOREIGN KEY (launched_by) REFERENCES users (id),
    CONSTRAINT fk_executions_decided_by FOREIGN KEY (decided_by) REFERENCES users (id)
);

CREATE INDEX ix_executions_project ON executions (project_id, created_at);
CREATE INDEX ix_executions_status ON executions (status);
CREATE INDEX ix_stories_project ON stories (project_id);
CREATE INDEX ix_squad_members_user ON squad_members (user_id);
