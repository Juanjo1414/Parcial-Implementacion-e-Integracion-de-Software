package com.eia.camelracing.security.controller;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.security.dto.AuthResponse;
import com.eia.camelracing.security.dto.LoginRequest;
import com.eia.camelracing.security.dto.RegisterRequest;
import com.eia.camelracing.security.entity.Role;
import com.eia.camelracing.security.entity.User;
import com.eia.camelracing.security.jwt.JwtService;
import com.eia.camelracing.security.repository.IUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    // Campo nuevo de la Etapa 7: @RequiredArgsConstructor de Lombok genera
    // automáticamente el constructor con este campo agregado, no hace falta
    // escribirlo a mano.
    private final AuditPublisher auditPublisher;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalStateException("El username '" + request.username() + "' ya está en uso");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password())) // nunca en texto plano
                .role(Role.VIEWER)
                .build();
        userRepository.save(user);

        auditPublisher.publish("CREATE", "User", user.getId().toString(),
                "Nuevo usuario registrado: " + user.getUsername());

        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username()).orElseThrow();

        auditPublisher.publish("LOGIN", "User", null, "Login exitoso: " + request.username());

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> profile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new AuthResponse(null, user.getUsername(), user.getRole().name()));
    }
}