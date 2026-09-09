package com.eia.camelracing.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ATENCIÓN: esta configuración es TEMPORAL, mientras construimos los módulos
 * de negocio (Competitor, Team, Race, etc.) sin preocuparnos por login.
 *
 * En la Etapa 6 esta clase se reemplaza por completo: ahí sí exigiremos JWT
 * y reglas por rol (Admin / Organizer / Viewer) en cada endpoint.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desactivamos CSRF: es una protección pensada para formularios
                // HTML con cookies de sesión; nuestra API es stateless (JSON + token),
                // así que no aplica y solo estorbaría en pruebas con Postman.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // TEMPORAL: todo abierto por ahora
                );
        return http.build();
    }
}