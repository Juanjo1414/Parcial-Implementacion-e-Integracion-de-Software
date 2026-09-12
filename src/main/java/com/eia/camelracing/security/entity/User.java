package com.eia.camelracing.security.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implementa UserDetails directamente para que Spring Security pueda usar
 * esta misma entidad como el "usuario autenticado", sin necesitar una clase
 * adaptadora intermedia.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    // El hash nunca debe salir en una respuesta: los DTOs de la API jamás
    // exponen esta entidad directamente.
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    private boolean enabled = true;

    // --- Métodos que exige la interfaz UserDetails de Spring Security ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security espera el prefijo "ROLE_" para que hasRole("ADMIN")
        // funcione correctamente en las reglas de autorización.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}