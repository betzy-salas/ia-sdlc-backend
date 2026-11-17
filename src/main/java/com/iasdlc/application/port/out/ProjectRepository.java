package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    Project save(Project project);
    Optional<Project> findById(UUID id);
    /** Todos los proyectos, del más reciente al más antiguo. */
    List<Project> findAll();
    boolean existsByName(String name);
}
