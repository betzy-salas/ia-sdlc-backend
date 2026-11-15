package com.iasdlc.infrastructure.persistence.adapter;

import com.iasdlc.application.port.out.ProjectRepository;
import com.iasdlc.domain.model.Project;
import com.iasdlc.infrastructure.persistence.entity.PersistenceMapper;
import com.iasdlc.infrastructure.persistence.repository.ProjectJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProjectPersistenceAdapter implements ProjectRepository {

    private final ProjectJpaRepository repository;
    private final PersistenceMapper mapper;

    public ProjectPersistenceAdapter(ProjectJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Project save(Project project) {
        // save() hace merge del agregado completo: historias y avance por fase incluidos.
        return mapper.toDomain(repository.save(mapper.toEntity(project)));
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Project> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }
}
