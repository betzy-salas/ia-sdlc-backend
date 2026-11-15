package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.AuthUseCase;
import com.iasdlc.application.port.out.PasswordHasher;
import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.port.out.TokenIssuer;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.AuthResult;
import com.iasdlc.application.view.UserProfile;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@UseCase
@Transactional
public class AuthService implements AuthUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;
    private final ViewAssembler views;

    public AuthService(UserRepository users, SquadRepository squads, PasswordHasher passwordHasher,
                       TokenIssuer tokenIssuer, Clock clock) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
        this.views = new ViewAssembler(users, squads);
    }

    @Override
    public AuthResult register(String fullName, String email, String password) {
        String normalizedEmail = User.normalizeEmail(email);
        if (users.existsByEmail(normalizedEmail)) {
            throw new DomainException(ErrorCode.EMAIL_ALREADY_REGISTERED, "El correo ya está registrado");
        }
        User user = users.save(User.register(fullName, normalizedEmail, passwordHasher.hash(password),
                clock.instant()));
        return new AuthResult(tokenIssuer.issue(user), views.profile(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(String email, String password) {
        User user = users.findByEmail(User.normalizeEmail(email))
                .filter(u -> passwordHasher.matches(password, u.passwordHash()))
                .orElseThrow(() -> new DomainException(ErrorCode.INVALID_CREDENTIALS,
                        "Correo o contraseña incorrectos"));
        return new AuthResult(tokenIssuer.issue(user), views.profile(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile me(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.UNAUTHORIZED, "La sesión ya no es válida"));
        return views.profile(user);
    }
}
