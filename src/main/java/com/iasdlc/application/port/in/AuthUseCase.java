package com.iasdlc.application.port.in;

import com.iasdlc.application.view.AuthResult;
import com.iasdlc.application.view.UserProfile;

import java.util.UUID;

public interface AuthUseCase {
    AuthResult register(String fullName, String email, String password);
    AuthResult login(String email, String password);
    UserProfile me(UUID userId);
}
