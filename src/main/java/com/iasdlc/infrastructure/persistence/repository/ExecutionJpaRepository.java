package com.iasdlc.infrastructure.persistence.repository;

import com.iasdlc.infrastructure.persistence.entity.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExecutionJpaRepository extends JpaRepository<ExecutionEntity, UUID> {
    List<ExecutionEntity> findAllByOrderByCreatedAtDesc();
    List<ExecutionEntity> findByInitiativeIdOrderByCreatedAtDesc(UUID initiativeId);
}
