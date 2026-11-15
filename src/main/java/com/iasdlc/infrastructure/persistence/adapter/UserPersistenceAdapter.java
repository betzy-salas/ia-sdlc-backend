package com.iasdlc.infrastructure.persistence.adapter;

import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.domain.model.User;
import com.iasdlc.infrastructure.persistence.entity.PersistenceMapper;
import com.iasdlc.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository repository;
    private final PersistenceMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public List<User> findAll() {
        return repository.findAllByOrderByFullNameAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<User> findAllById(Collection<UUID> ids) {
        return repository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }
}
