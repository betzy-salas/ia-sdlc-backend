package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.UserQueryUseCase;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUseCase {

    private final UserRepository users;

    public UserQueryService(UserRepository users) {
        this.users = users;
    }

    @Override
    public List<User> listUsers() {
        return users.findAll();
    }
}
