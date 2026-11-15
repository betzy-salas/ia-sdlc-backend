-- Datos semilla: las 5 fases y el agente de refinamiento (fase REQUIREMENTS, PILOT, versión 0.1.0).

INSERT INTO phases (code, sort_order, name, approver_role) VALUES ('REQUIREMENTS', 1, 'Requerimientos', 'PRODUCT_OWNER');
INSERT INTO phases (code, sort_order, name, approver_role) VALUES ('ANALYSIS', 2, 'Análisis', 'TECH_LEAD');
INSERT INTO phases (code, sort_order, name, approver_role) VALUES ('DESIGN', 3, 'Diseño', 'ARCHITECT');
INSERT INTO phases (code, sort_order, name, approver_role) VALUES ('CONSTRUCTION', 4, 'Construcción', 'DEVELOPER');
INSERT INTO phases (code, sort_order, name, approver_role) VALUES ('TESTING', 5, 'Pruebas', 'QA');

-- El agente semilla no tiene responsable (owner_user_id NULL) porque todavía no existen usuarios.
INSERT INTO agents (id, name, phase_code, status, current_version, description, repository_url, repository_path, owner_user_id, created_at)
VALUES ('a0000000-0000-4000-8000-000000000001',
        'Agente de refinamiento',
        'REQUIREMENTS',
        'PILOT',
        '0.1.0',
        'Refina las historias de usuario del proyecto: las lleva al formato como/quiero/para, propone criterios de aceptación y señala ambigüedades.',
        NULL,
        'agents/refinamiento/agent.md',
        NULL,
        CURRENT_TIMESTAMP);

INSERT INTO agent_versions (id, agent_id, version, commit_sha, instructions, change_notes, created_at, sort_index)
VALUES ('a0000000-0000-4000-8000-000000000002',
        'a0000000-0000-4000-8000-000000000001',
        '0.1.0',
        NULL,
        '# Agente de refinamiento

Eres un analista de producto que ayuda a un squad a refinar sus historias de usuario antes de pasar a la fase de análisis.

## Qué recibes
Una lista de historias de usuario con título y descripción, tal como las escribió el equipo.

## Qué debes entregar por cada historia
1. Un título corto y claro, que empiece con un verbo.
2. La historia en formato: Como <rol>, quiero <necesidad>, para <beneficio>.
3. Tres criterios de aceptación en formato Dado / Cuando / Entonces: el camino feliz, un caso de error y una regla de trazabilidad.
4. Una lista de ambigüedades: actor no identificado, beneficio no explícito, descripción insuficiente o términos subjetivos sin criterio medible.

## Reglas
- No inventes reglas de negocio: lo que no esté en la historia va como ambigüedad.
- Escribe en español, en tono neutro y sin tecnicismos innecesarios.
- No cambies el alcance de la historia; si es demasiado grande, sugiere dividirla dentro de las ambigüedades.
- El Product Owner del squad revisa y aprueba tu propuesta: redacta para que pueda decidir rápido.',
        'Versión inicial para el piloto.',
        CURRENT_TIMESTAMP,
        0);
