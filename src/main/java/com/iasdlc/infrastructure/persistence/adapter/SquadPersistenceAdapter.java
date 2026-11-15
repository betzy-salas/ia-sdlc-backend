package com.iasdlc.infrastructure.persistence.adapter;

import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.infrastructure.persistence.entity.PersistenceMapper;
import com.iasdlc.infrastructure.persistence.repository.SquadJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SquadPersistenceAdapter implements SquadRepository {

    private final SquadJpaRepository repository;
    private final PersistenceMapper mapper;

    public SquadPersistenceAdapter(SquadJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Squad save(Squad squad) {
        // save() hace merge del agregado completo: inserta, actualiza y elimina integrantes según la lista.
        return mapper.toDomain(repository.save(mapper.toEntity(squad)));
    }

    @Override
    public Optional<Squad> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Squad> findAll() {
        return repository.findAllByOrderByNameAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public List<Squad> findByMemberUserId(UUID userId) {
        return repository.findByMemberUserId(userId).stream().map(mapper::toDomain).toList();
    }
}
