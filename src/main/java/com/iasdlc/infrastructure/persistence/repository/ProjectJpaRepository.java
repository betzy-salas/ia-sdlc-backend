package com.iasdlc.infrastructure.persistence.repository;

import com.iasdlc.infrastructure.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {
    List<ProjectEntity> findAllByOrderByCreatedAtDesc();
    boolean existsByNameIgnoreCase(String name);
}
