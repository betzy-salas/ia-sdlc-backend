package com.iasdlc.application.port.in;

import com.iasdlc.domain.model.User;

import java.util.List;

public interface UserQueryUseCase {
    List<User> listUsers();
}
