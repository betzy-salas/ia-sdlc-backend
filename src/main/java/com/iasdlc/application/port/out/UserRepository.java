package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsById(UUID id);
    /** Todos los usuarios ordenados por nombre. */
    List<User> findAll();
    List<User> findAllById(Collection<UUID> ids);
}
