package com.eia.camelracing.registration.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.race.entity.Race;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.entity.RaceType;
import com.eia.camelracing.race.repository.IRaceRepository;
import com.eia.camelracing.registration.dto.RegistrationRequest;
import com.eia.camelracing.registration.dto.RegistrationResponse;
import com.eia.camelracing.registration.entity.RaceRegistration;
import com.eia.camelracing.registration.repository.IRaceRegistrationRepository;
import com.eia.camelracing.team.repository.ITeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private IRaceRegistrationRepository registrationRepository;
    @Mock private IRaceRepository raceRepository;
    @Mock private ICompetitorRepository competitorRepository;
    @Mock private ITeamRepository teamRepository;
    @Mock private AuditPublisher auditPublisher;

    @InjectMocks
    private RegistrationService service;

    private Race openRace(UUID raceId, LocalDateTime deadline) {
        return Race.builder()
                .id(raceId).name("Carrera de prueba")
                .status(RaceStatus.OPEN_FOR_REGISTRATION)
                .type(RaceType.MIXED)
                .registrationDeadline(deadline)
                .build();
    }

    private Competitor competitor(UUID id, CompetitorStatus status) {
        return Competitor.builder()
                .id(id).name("Byte").nickname("byte").type(CompetitorType.CAMEL)
                .weight(450.5).height(1.9).status(status)
                .build();
    }

    // Caso 6 de la guía: "Register an active competitor successfully."
    @Test
    void register_shouldSucceedForActiveCompetitor() {
        UUID raceId = UUID.randomUUID();
        UUID competitorId = UUID.randomUUID();
        Race race = openRace(raceId, LocalDateTime.now().plusDays(1));
        Competitor comp = competitor(competitorId, CompetitorStatus.ACTIVE);

        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));
        when(competitorRepository.findById(competitorId)).thenReturn(Optional.of(comp));
        when(registrationRepository.existsByRaceIdAndCompetitorId(raceId, competitorId)).thenReturn(false);
        when(registrationRepository.findByRaceId(raceId)).thenReturn(List.of()); // sin inscripciones previas
        // thenAnswer: en vez de un valor fijo, ejecuta lógica -> aquí simulamos
        // que la BD le asigna un id al guardar, tal como pasaría en la realidad.
        when(registrationRepository.save(any(RaceRegistration.class))).thenAnswer(invocation -> {
            RaceRegistration r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        RegistrationResponse response = service.register(
                raceId, new RegistrationRequest(competitorId, null, null));

        assertThat(response.competitorNickname()).isEqualTo("byte");
        assertThat(response.startingPosition()).isEqualTo(1); // auto-asignado, sin inscripciones previas
    }

    // Caso 7 de la guía: "Reject a suspended competitor."
    @Test
    void register_shouldRejectSuspendedCompetitor() {
        UUID raceId = UUID.randomUUID();
        UUID competitorId = UUID.randomUUID();
        Race race = openRace(raceId, LocalDateTime.now().plusDays(1));
        Competitor comp = competitor(competitorId, CompetitorStatus.SUSPENDED);

        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));
        when(competitorRepository.findById(competitorId)).thenReturn(Optional.of(comp));

        assertThatThrownBy(() -> service.register(raceId, new RegistrationRequest(competitorId, null, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACTIVO");
    }

    // Caso 8 de la guía: "Reject a duplicated registration."
    @Test
    void register_shouldRejectDuplicateRegistration() {
        UUID raceId = UUID.randomUUID();
        UUID competitorId = UUID.randomUUID();
        Race race = openRace(raceId, LocalDateTime.now().plusDays(1));
        Competitor comp = competitor(competitorId, CompetitorStatus.ACTIVE);

        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));
        when(competitorRepository.findById(competitorId)).thenReturn(Optional.of(comp));
        when(registrationRepository.existsByRaceIdAndCompetitorId(raceId, competitorId)).thenReturn(true);

        assertThatThrownBy(() -> service.register(raceId, new RegistrationRequest(competitorId, null, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está inscrito");
    }

    // Caso 9 de la guía: "Reject registration after the deadline."
    @Test
    void register_shouldRejectAfterDeadline() {
        UUID raceId = UUID.randomUUID();
        UUID competitorId = UUID.randomUUID();
        // Fecha límite ya pasada, aunque el estado siga OPEN_FOR_REGISTRATION.
        Race race = openRace(raceId, LocalDateTime.now().minusDays(1));

        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));

        assertThatThrownBy(() -> service.register(raceId, new RegistrationRequest(competitorId, null, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya pasó");
    }
}