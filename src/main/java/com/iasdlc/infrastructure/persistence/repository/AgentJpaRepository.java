package com.iasdlc.infrastructure.persistence.repository;

import com.iasdlc.infrastructure.persistence.entity.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentJpaRepository extends JpaRepository<AgentEntity, UUID> {
    List<AgentEntity> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}
