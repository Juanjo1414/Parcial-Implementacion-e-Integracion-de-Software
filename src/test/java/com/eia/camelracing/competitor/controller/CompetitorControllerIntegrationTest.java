package com.eia.camelracing.competitor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompetitorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Caso 15 de la guía: "Return 404 for a missing resource."
    @Test
    @WithMockUser(roles = "VIEWER") // cualquier rol autenticado puede leer
    void findById_withMissingId_shouldReturn404() throws Exception {
        UUID randomId = UUID.randomUUID(); // válido en formato, no existe en la BD

        mockMvc.perform(get("/api/competitors/" + randomId))
                .andExpect(status().isNotFound());
    }
}