package com.eia.camelracing.result.service;

import com.eia.camelracing.common.audit.AuditPublisher;
import com.eia.camelracing.common.exception.BusinessRuleException;
import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.race.entity.Race;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.registration.entity.RaceRegistration;
import com.eia.camelracing.registration.entity.RegistrationStatus;
import com.eia.camelracing.registration.repository.IRaceRegistrationRepository;
import com.eia.camelracing.result.dto.ResultRequest;
import com.eia.camelracing.result.dto.ResultResponse;
import com.eia.camelracing.result.entity.RaceResult;
import com.eia.camelracing.result.entity.ResultStatus;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import com.eia.camelracing.team.repository.ITeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock private IRaceResultRepository resultRepository;
    @Mock private IRaceRegistrationRepository registrationRepository;
    @Mock private ICompetitorRepository competitorRepository;
    @Mock private ITeamRepository teamRepository;
    @Mock private AuditPublisher auditPublisher;

    @InjectMocks
    private ResultService service;

    private RaceRegistration approvedRegistration(UUID raceId, UUID registrationId) {
        Race race = Race.builder().id(raceId).status(RaceStatus.IN_PROGRESS).build();
        Competitor competitor = Competitor.builder()
                .id(UUID.randomUUID()).nickname("byte").type(CompetitorType.CAMEL)
                .status(CompetitorStatus.ACTIVE)
                .wins(0).losses(0).racesCompleted(0)
                .build();
        return RaceRegistration.builder()
                .id(registrationId).race(race).competitor(competitor)
                .status(RegistrationStatus.APPROVED)
                .build();
    }

    // Caso 10 de la guía: "Record a valid result."
    @Test
    void record_shouldSaveValidResult() {
        UUID raceId = UUID.randomUUID();
        UUID registrationId = UUID.randomUUID();
        RaceRegistration registration = approvedRegistration(raceId, registrationId);

        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));
        when(resultRepository.existsByRegistration_Id(registrationId)).thenReturn(false);
        when(resultRepository.existsByRegistration_Race_IdAndFinalPosition(raceId, 1)).thenReturn(false);
        when(resultRepository.save(any(RaceResult.class))).thenAnswer(invocation -> {
            RaceResult r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });
        when(competitorRepository.save(any(Competitor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResultResponse response = service.record(raceId, new ResultRequest(
                registrationId, 1, 245.8, 0.0, ResultStatus.FINISHED, "Ganador"));

        assertThat(response.finalPosition()).isEqualTo(1);
        assertThat(response.points()).isEqualTo(10); // primer lugar = 10 puntos, tabla de la guía
    }

    // Caso 11 de la guía: "Reject two winners in one race."
    @Test
    void record_shouldRejectDuplicateWinnerPosition() {
        UUID raceId = UUID.randomUUID();
        UUID registrationId = UUID.randomUUID();
        RaceRegistration registration = approvedRegistration(raceId, registrationId);

        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));
        when(resultRepository.existsByRegistration_Id(registrationId)).thenReturn(false);
        // Ya existe alguien más en posición 1 en esta misma carrera.
        when(resultRepository.existsByRegistration_Race_IdAndFinalPosition(raceId, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.record(raceId, new ResultRequest(
                registrationId, 1, 250.0, 0.0, ResultStatus.FINISHED, "Segundo ganador (inválido)")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya fue registrada");
    }
}