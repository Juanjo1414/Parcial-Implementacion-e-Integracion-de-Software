package com.eia.camelracing.security.jwt;

import com.eia.camelracing.security.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no viene el header, o no empieza con "Bearer ", dejamos pasar
        // la petición sin autenticar (Spring Security decidirá más adelante
        // si esa ruta requiere login o no).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // le quita el "Bearer " (7 caracteres)

        try {
            String username = jwtService.extractUsername(token);

            // Solo intentamos autenticar si hay username Y todavía no hay nadie
            // autenticado en este contexto (evita reprocesar en cada filtro).
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Esta línea es la que efectivamente "loguea" al usuario
                    // para el resto de esta petición HTTP.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // Token expirado, mal formado o con firma inválida: se ignora y
            // la petición sigue sin autenticar, para que la cadena de
            // seguridad responda 401 en lugar de dejar escapar un 500.
            log.debug("Token JWT inválido recibido: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}