package com.iasdlc.infrastructure.persistence.repository;

import com.iasdlc.infrastructure.persistence.entity.InitiativeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InitiativeJpaRepository extends JpaRepository<InitiativeEntity, UUID> {
    List<InitiativeEntity> findAllByOrderByCreatedAtDesc();
    List<InitiativeEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
    boolean existsByProjectIdAndNameIgnoreCase(UUID projectId, String name);
}
