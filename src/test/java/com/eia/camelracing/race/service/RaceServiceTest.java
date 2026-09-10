package com.eia.camelracing.race.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.race.dto.RaceRequest;
import com.eia.camelracing.race.dto.RaceResponse;
import com.eia.camelracing.race.entity.Race;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.entity.RaceType;
import com.eia.camelracing.race.repository.IRaceRepository;
import com.eia.camelracing.registration.repository.IRaceRegistrationRepository;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaceServiceTest {

    @Mock private IRaceRepository repository;
    @Mock private IRaceRegistrationRepository registrationRepository;
    @Mock private IRaceResultRepository resultRepository;
    @Mock private AuditPublisher auditPublisher;

    @InjectMocks
    private RaceService service;

    // Caso 4 de la guía: "Create a valid race."
    @Test
    void create_shouldSaveValidRace() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(10);
        LocalDateTime deadline = LocalDateTime.now().plusDays(5); // antes de la carrera: válido

        RaceRequest request = new RaceRequest(
                "The Great EIA Mixed Race", "Carrera de prueba", scheduledAt,
                "Entrada", "Coliseo", 1000.0, 10, RaceType.MIXED,
                "Mr. Abandonado", deadline);

        Race savedEntity = Race.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .status(RaceStatus.DRAFT)
                .scheduledAt(scheduledAt)
                .registrationDeadline(deadline)
                .distanceMeters(1000.0)
                .maxParticipants(10)
                .type(RaceType.MIXED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.save(any(Race.class))).thenReturn(savedEntity);

        RaceResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("The Great EIA Mixed Race");
        assertThat(response.status()).isEqualTo(RaceStatus.DRAFT);
    }
}