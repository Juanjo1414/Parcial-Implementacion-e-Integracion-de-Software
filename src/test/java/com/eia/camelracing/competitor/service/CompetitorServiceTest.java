package com.eia.camelracing.competitor.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.common.exception.BusinessRuleException;
import com.eia.camelracing.competitor.dto.CompetitorRequest;
import com.eia.camelracing.competitor.dto.CompetitorResponse;
import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Prueba UNITARIA: no levanta Spring, no toca base de datos. Los repository
 * son "dobles de prueba" (mocks) que devuelven exactamente lo que le decimos
 * con when(...).thenReturn(...) -> así controlamos el escenario con precisión.
 */
@ExtendWith(MockitoExtension.class)
class CompetitorServiceTest {

    @Mock
    private ICompetitorRepository repository;

    @Mock
    private IRaceResultRepository resultRepository;

    @Mock
    private AuditPublisher auditPublisher;

    // Mockito inyecta automáticamente los 3 @Mock de arriba en este service,
    // en el mismo orden del constructor generado por @RequiredArgsConstructor.
    @InjectMocks
    private CompetitorService service;

    private CompetitorRequest validRequest() {
        return new CompetitorRequest(
                "Byte", "byte", CompetitorType.CAMEL,
                LocalDate.of(2020, 5, 14), 450.5, 1.9, "Colombia");
    }

    // Caso 1 de la guía: "Create a valid competitor."
    @Test
    void create_shouldSaveValidCompetitor() {
        CompetitorRequest request = validRequest();

        // "Cuando llamen a existsByNicknameIgnoreCase con 'byte', responde false"
        when(repository.existsByNicknameIgnoreCase("byte")).thenReturn(false);

        Competitor savedEntity = Competitor.builder()
                .id(UUID.randomUUID())
                .name("Byte").nickname("byte").type(CompetitorType.CAMEL)
                .weight(450.5).height(1.9).originCountry("Colombia")
                .status(CompetitorStatus.ACTIVE)
                .registeredAt(LocalDateTime.now())
                .wins(0).losses(0).racesCompleted(0)
                .build();
        // "Cuando llamen a save con CUALQUIER Competitor, devuelve savedEntity"
        when(repository.save(any(Competitor.class))).thenReturn(savedEntity);

        CompetitorResponse response = service.create(request);

        assertThat(response.nickname()).isEqualTo("byte");
        assertThat(response.status()).isEqualTo(CompetitorStatus.ACTIVE);
        // verify() confirma que el método REALMENTE se llamó, no solo que no falló.
        verify(repository).save(any(Competitor.class));
        verify(auditPublisher).publish(eq("CREATE"), eq("Competitor"), any(), anyString());
    }

    // Caso 3 de la guía: "Reject a duplicated nickname."
    @Test
    void create_shouldThrowWhenNicknameDuplicated() {
        CompetitorRequest request = validRequest();
        when(repository.existsByNicknameIgnoreCase("byte")).thenReturn(true);

        // assertThatThrownBy: verifica que se lanzó la excepción esperada
        // Y que su mensaje contiene el texto que esperamos.
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("byte");

        // never(): confirma que save() NO se llamó, porque el error debe
        // detener el flujo antes de intentar guardar.
        verify(repository, never()).save(any());
    }
}