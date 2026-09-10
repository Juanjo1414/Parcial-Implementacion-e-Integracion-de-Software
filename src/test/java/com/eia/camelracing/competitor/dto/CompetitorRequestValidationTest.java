package com.eia.camelracing.competitor.dto;

import com.eia.camelracing.competitor.entity.CompetitorType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Esta prueba NO usa Mockito ni Spring: valida directamente el record contra
 * las anotaciones (@Positive, @NotBlank, etc.) usando el motor de validación
 * de Jakarta Bean Validation, el mismo que Spring usa internamente cuando
 * ve @Valid en un controller.
 */
class CompetitorRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // Caso 2 de la guía: "Reject a competitor with invalid weight."
    @Test
    void request_withNegativeWeight_shouldViolatePositiveConstraint() {
        CompetitorRequest invalid = new CompetitorRequest(
                "Byte", "byte", CompetitorType.CAMEL,
                LocalDate.of(2020, 5, 14),
                -10.0, // peso inválido: negativo
                1.9, "Colombia");

        Set<ConstraintViolation<CompetitorRequest>> violations = validator.validate(invalid);

        assertThat(violations).isNotEmpty();
        // Confirma que el error específico está en el campo "weight", no en otro.
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("weight"));
    }
}