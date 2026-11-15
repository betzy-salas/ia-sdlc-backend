package com.iasdlc.infrastructure.persistence.adapter;

import com.iasdlc.application.port.out.AgentRepository;
import com.iasdlc.domain.model.Agent;
import com.iasdlc.infrastructure.persistence.entity.PersistenceMapper;
import com.iasdlc.infrastructure.persistence.repository.AgentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AgentPersistenceAdapter implements AgentRepository {

    private final AgentJpaRepository repository;
    private final PersistenceMapper mapper;

    public AgentPersistenceAdapter(AgentJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Agent save(Agent agent) {
        return mapper.toDomain(repository.save(mapper.toEntity(agent)));
    }

    @Override
    public Optional<Agent> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Agent> findAll() {
        return repository.findAllByOrderByNameAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }
}
