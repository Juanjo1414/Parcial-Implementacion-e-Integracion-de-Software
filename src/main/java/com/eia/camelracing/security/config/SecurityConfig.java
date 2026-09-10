package com.eia.camelracing.security.config;

import com.eia.camelracing.security.jwt.JwtAuthenticationFilter;
import com.eia.camelracing.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración DEFINITIVA de seguridad. Reemplaza por completo la clase
 * temporal de la Etapa 0 (bórrala o sobrescríbela).
 *
 * @EnableMethodSecurity activa las anotaciones @PreAuthorize que vamos a
 * poner directamente en cada controller — así cada endpoint declara su
 * propio requisito de rol, en vez de tener una lista gigante de reglas
 * centralizadas y difíciles de mantener aquí.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt: exactamente lo que pide la guía ("BCrypt or another secure
        // password-hashing algorithm"). Nunca se guarda la contraseña en texto plano.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // AHORA: el UserDetailsService se pasa directo al constructor.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API stateless con JSON, no aplica
                .sessionManagement(session ->
                        // STATELESS: el servidor NO guarda sesiones. Cada request
                        // se autentica desde cero con su propio token JWT.
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // login/registro son públicos
                        .anyRequest().authenticated() // todo lo demás exige un token válido
                )
                .authenticationProvider(authenticationProvider())
                // Nuestro filtro se ejecuta ANTES del filtro estándar de Spring,
                // para que el usuario ya quede autenticado cuando Spring evalúe
                // las reglas de arriba.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}