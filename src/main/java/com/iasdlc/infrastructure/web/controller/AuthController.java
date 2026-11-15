package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.AuthUseCase;
import com.iasdlc.infrastructure.web.dto.AuthDtos.AuthResponse;
import com.iasdlc.infrastructure.web.dto.AuthDtos.LoginRequest;
import com.iasdlc.infrastructure.web.dto.AuthDtos.RegisterRequest;
import com.iasdlc.infrastructure.web.dto.AuthDtos.UserResponse;
import com.iasdlc.infrastructure.web.mapper.AuthWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthUseCase auth;

    public AuthController(AuthUseCase auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return AuthWebMapper.toResponse(auth.register(request.fullName(), request.email(), request.password()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthWebMapper.toResponse(auth.login(request.email(), request.password()));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UUID userId) {
        return AuthWebMapper.toResponse(auth.me(userId));
    }
}
