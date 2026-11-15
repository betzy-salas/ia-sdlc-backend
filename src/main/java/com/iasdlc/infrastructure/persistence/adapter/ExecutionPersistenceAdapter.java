package com.iasdlc.infrastructure.persistence.adapter;

import com.iasdlc.application.port.out.ExecutionRepository;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.infrastructure.persistence.entity.PersistenceMapper;
import com.iasdlc.infrastructure.persistence.repository.ExecutionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** No expone borrado: las ejecuciones son un registro histórico. */
@Repository
public class ExecutionPersistenceAdapter implements ExecutionRepository {

    private final ExecutionJpaRepository repository;
    private final PersistenceMapper mapper;

    public ExecutionPersistenceAdapter(ExecutionJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Execution save(Execution execution) {
        return mapper.toDomain(repository.save(mapper.toEntity(execution)));
    }

    @Override
    public Optional<Execution> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Execution> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Execution> findByProjectId(UUID projectId) {
        return repository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(mapper::toDomain).toList();
    }
}
