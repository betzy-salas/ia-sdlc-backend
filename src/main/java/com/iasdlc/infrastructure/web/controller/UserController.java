package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.UserQueryUseCase;
import com.iasdlc.infrastructure.web.dto.AuthDtos.UserSummaryResponse;
import com.iasdlc.infrastructure.web.mapper.AuthWebMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserQueryUseCase users;

    public UserController(UserQueryUseCase users) {
        this.users = users;
    }

    @GetMapping
    public List<UserSummaryResponse> list() {
        return users.listUsers().stream().map(AuthWebMapper::toSummary).toList();
    }
}
