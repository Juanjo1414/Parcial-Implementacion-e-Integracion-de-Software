package com.eia.camelracing.race.dto;

import com.eia.camelracing.race.entity.RaceType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RaceRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // Caso 5 de la guía: "Reject a race scheduled in the past."
    @Test
    void request_withPastScheduledDate_shouldViolateFutureConstraint() {
        RaceRequest invalid = new RaceRequest(
                "Carrera inválida", "desc",
                LocalDateTime.now().minusDays(5), // fecha en el pasado
                "inicio", "fin", 1000.0, 6, RaceType.INDIVIDUAL,
                "organizador", LocalDateTime.now().minusDays(10));

        Set<ConstraintViolation<RaceRequest>> violations = validator.validate(invalid);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("scheduledAt"));
    }
}