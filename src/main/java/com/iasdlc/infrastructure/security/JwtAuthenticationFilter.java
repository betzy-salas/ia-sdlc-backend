package com.iasdlc.infrastructure.security;

import com.iasdlc.application.port.out.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lee el encabezado "Authorization: Bearer ..." y, si el token es válido y el usuario existe,
 * deja al usuario autenticado. El "principal" es el id del usuario (UUID).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService tokenService;
    private final UserRepository users;

    public JwtAuthenticationFilter(JwtTokenService tokenService, UserRepository users) {
        this.tokenService = tokenService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            Optional<UUID> userId = tokenService.parseUserId(header.substring(BEARER_PREFIX.length()).trim());
            // Se comprueba que el usuario exista: con H2 en memoria los datos se pierden al reiniciar
            // y un token viejo no debe pasar como válido.
            if (userId.isPresent() && users.existsById(userId.get())) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId.get(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
