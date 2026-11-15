package com.iasdlc.infrastructure.persistence.repository;

import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.infrastructure.persistence.entity.PhaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseJpaRepository extends JpaRepository<PhaseEntity, PhaseCode> {
    List<PhaseEntity> findAllByOrderBySortOrderAsc();
}
