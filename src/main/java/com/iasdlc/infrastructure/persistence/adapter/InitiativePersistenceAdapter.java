package com.iasdlc.infrastructure.persistence.adapter;

import com.iasdlc.application.port.out.InitiativeRepository;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.infrastructure.persistence.entity.PersistenceMapper;
import com.iasdlc.infrastructure.persistence.repository.InitiativeJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InitiativePersistenceAdapter implements InitiativeRepository {

    private final InitiativeJpaRepository repository;
    private final PersistenceMapper mapper;

    public InitiativePersistenceAdapter(InitiativeJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Initiative save(Initiative initiative) {
        // save() hace merge del agregado completo: historias y avance por fase incluidos.
        return mapper.toDomain(repository.save(mapper.toEntity(initiative)));
    }

    @Override
    public Optional<Initiative> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Initiative> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Initiative> findByProjectId(UUID projectId) {
        return repository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByProjectIdAndName(UUID projectId, String name) {
        return repository.existsByProjectIdAndNameIgnoreCase(projectId, name);
    }
}
