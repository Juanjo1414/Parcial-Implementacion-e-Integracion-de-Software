package com.eia.camelracing.registration.dto;

import java.util.UUID;

// raceId NO va aquí: viaja en la URL (/api/races/{raceId}/registrations),
// no tiene sentido repetirlo en el body.
public record RegistrationRequest(
        UUID competitorId,  // uno de los dos...
        UUID teamId,        // ...pero no ambos (se valida en el service)
        Integer startingPosition // opcional: si no viene, se asigna automáticamente
) {}