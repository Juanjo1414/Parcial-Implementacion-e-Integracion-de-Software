package com.eia.camelracing.security.config;

import com.eia.camelracing.security.entity.Role;
import com.eia.camelracing.security.entity.User;
import com.eia.camelracing.security.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea las cuentas de acceso iniciales (una por rol) la primera vez que la
 * aplicación arranca contra una base de datos vacía. Se ejecuta antes que
 * {@code DemoDataSeeder} porque las carreras de demostración registran
 * quién las organiza y quién carga resultados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Solo siembra si la tabla está vacía -> evita crear usuarios
        // duplicados cada vez que reinicias la app o el contenedor.
        if (userRepository.count() > 0) {
            log.info("Ya existen usuarios registrados; se omite la siembra inicial.");
            return;
        }

        createUser("admin", "Admin123!", Role.ADMIN);
        createUser("organizador", "Organizer123!", Role.ORGANIZER);
        createUser("viewer", "Viewer123!", Role.VIEWER);

        log.info("Usuarios iniciales creados: admin / organizador / viewer");
    }

    private void createUser(String username, String rawPassword, Role role) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();
        userRepository.save(user);
    }
}