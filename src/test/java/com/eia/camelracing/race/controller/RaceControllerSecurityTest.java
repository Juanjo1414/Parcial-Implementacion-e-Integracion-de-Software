package com.eia.camelracing.race.controller;

import com.eia.camelracing.race.dto.RaceRequest;
import com.eia.camelracing.race.entity.RaceType;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de INTEGRACIÓN: levanta el contexto completo de Spring (incluida
 * la cadena de filtros de seguridad real) y usa H2 en memoria como base de
 * datos, gracias al perfil "test" (application-test.yml).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RaceControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RaceRequest validRaceRequest() {
        return new RaceRequest(
                "Carrera de prueba", "descripción",
                LocalDateTime.now().plusDays(10),
                "inicio", "fin", 1000.0, 6, RaceType.INDIVIDUAL,
                "organizador", LocalDateTime.now().plusDays(5));
    }

    // Caso 12 de la guía: "Prevent a viewer from creating a race."
    // @WithMockUser simula un usuario YA autenticado con ese rol, sin
    // necesitar generar un JWT real -> perfecto para probar solo la regla
    // de autorización (@PreAuthorize), no el mecanismo de login en sí.
    @Test
    @WithMockUser(roles = "VIEWER")
    void createRace_asViewer_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRaceRequest())))
                .andExpect(status().isForbidden()); // 403: autenticado, pero sin permiso
    }

    // Caso 13 de la guía: "Allow an administrator to create a race."
    @Test
    @WithMockUser(roles = "ADMIN")
    void createRace_asAdmin_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRaceRequest())))
                .andExpect(status().isCreated());
    }

    // Caso 14 de la guía: "Return 401 without a valid token."
    // Sin @WithMockUser: la petición llega SIN autenticar en absoluto.
    @Test
    void findAll_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/races"))
                .andExpect(status().isUnauthorized()); // 401: ni siquiera sabemos quién eres
    }
}