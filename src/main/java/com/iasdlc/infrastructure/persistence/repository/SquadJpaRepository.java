package com.iasdlc.infrastructure.persistence.repository;

import com.iasdlc.infrastructure.persistence.entity.SquadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SquadJpaRepository extends JpaRepository<SquadEntity, UUID> {
    List<SquadEntity> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);

    @Query("select distinct s from SquadEntity s join s.members m where m.userId = :userId order by s.name")
    List<SquadEntity> findByMemberUserId(@Param("userId") UUID userId);
}
