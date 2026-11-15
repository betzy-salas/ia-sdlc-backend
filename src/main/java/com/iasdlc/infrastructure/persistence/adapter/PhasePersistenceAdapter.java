package com.iasdlc.infrastructure.persistence.adapter;

import com.iasdlc.application.port.out.PhaseRepository;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.infrastructure.persistence.entity.PersistenceMapper;
import com.iasdlc.infrastructure.persistence.repository.PhaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PhasePersistenceAdapter implements PhaseRepository {

    private final PhaseJpaRepository repository;
    private final PersistenceMapper mapper;

    public PhasePersistenceAdapter(PhaseJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Phase> findAll() {
        return repository.findAllByOrderBySortOrderAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Phase> findByCode(PhaseCode code) {
        return repository.findById(code).map(mapper::toDomain);
    }
}
