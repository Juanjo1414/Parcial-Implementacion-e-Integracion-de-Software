package com.eia.camelracing.team.dto;

import java.util.UUID;

// Un resumen pequeño del competidor, NO el CompetitorResponse completo.
// Evita que la respuesta de Team se vuelva gigante o entre en referencias
// cruzadas (Team -> Competitor -> Team -> ...).
public record TeamMemberSummary(
        UUID id,
        String name,
        String nickname
) {}