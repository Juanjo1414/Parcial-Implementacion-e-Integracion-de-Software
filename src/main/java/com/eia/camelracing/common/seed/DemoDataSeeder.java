package com.eia.camelracing.common.seed;

import com.eia.camelracing.competitor.entity.Competitor;
import com.eia.camelracing.competitor.entity.CompetitorStatus;
import com.eia.camelracing.competitor.entity.CompetitorType;
import com.eia.camelracing.competitor.repository.ICompetitorRepository;
import com.eia.camelracing.race.entity.Race;
import com.eia.camelracing.race.entity.RaceStatus;
import com.eia.camelracing.race.entity.RaceType;
import com.eia.camelracing.race.repository.IRaceRepository;
import com.eia.camelracing.registration.entity.RaceRegistration;
import com.eia.camelracing.registration.entity.RegistrationStatus;
import com.eia.camelracing.registration.repository.IRaceRegistrationRepository;
import com.eia.camelracing.result.entity.RaceResult;
import com.eia.camelracing.result.entity.ResultStatus;
import com.eia.camelracing.result.repository.IRaceResultRepository;
import com.eia.camelracing.team.entity.Team;
import com.eia.camelracing.team.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Carga un catálogo de ejemplo (competidores, equipos y carreras en distintos
 * estados) para que la aplicación no arranque con pantallas vacías y para
 * cubrir los datos mínimos que exige la guía del proyecto. Solo se ejecuta
 * si la tabla de competidores está vacía, así que es seguro reiniciar el
 * contenedor de la aplicación sin duplicar el catálogo.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class DemoDataSeeder implements CommandLineRunner {

    private final ICompetitorRepository competitorRepository;
    private final ITeamRepository teamRepository;
    private final IRaceRepository raceRepository;
    private final IRaceRegistrationRepository registrationRepository;
    private final IRaceResultRepository resultRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (competitorRepository.count() > 0) {
            log.info("Ya existen competidores registrados; se omite la siembra de datos de ejemplo.");
            return;
        }

        Competitor nullPointer = competitor("Null Pointer", "null-pointer", CompetitorType.DWARF, "Colombia", 42.0, 0.95);
        Competitor stackOverflow = competitor("Stack Overflow", "stack-overflow", CompetitorType.DWARF, "Colombia", 45.5, 0.98);
        Competitor littleLambda = competitor("Little Lambda", "little-lambda", CompetitorType.DWARF, "Colombia", 38.0, 0.90);
        Competitor captainCache = competitor("Captain Cache", "captain-cache", CompetitorType.DWARF, "Colombia", 40.2, 0.93);
        Competitor tinyDocker = competitor("Tiny Docker", "tiny-docker", CompetitorType.DWARF, "Colombia", 39.7, 0.91);
        Competitor byte_ = competitor("Byte", "byte", CompetitorType.CAMEL, "Colombia", 480.0, 2.1);
        Competitor kernel = competitor("Kernel", "kernel", CompetitorType.CAMEL, "Colombia", 495.5, 2.15);
        Competitor garbageCollector = competitor("Garbage Collector", "garbage-collector", CompetitorType.MEDIUM, "Colombia", 150.0, 1.5);
        Competitor bitwiseOperator = competitor("Bitwise Operator", "bitwise-operator", CompetitorType.MEDIUM, "Colombia", 145.0, 1.48);
        competitorRepository.saveAll(List.of(nullPointer, stackOverflow, littleLambda, captainCache,
                tinyDocker, byte_, kernel, garbageCollector, bitwiseOperator));

        Team fiveExceptions = teamRepository.save(Team.builder()
                .name("The Five Exceptions")
                .description("Equipo íntegramente compuesto por enanos, especialista en carreras por equipos")
                .coachName("Mr. Abandonado")
                .build());
        fiveExceptions.addMember(nullPointer);
        fiveExceptions.addMember(stackOverflow);
        fiveExceptions.addMember(littleLambda);
        fiveExceptions.addMember(captainCache);
        fiveExceptions.addMember(tinyDocker);

        Team desertThreads = teamRepository.save(Team.builder()
                .name("The Desert Threads")
                .description("Equipo mixto de camellos y competidores medianos")
                .coachName("Ada Lovelace")
                .build());
        desertThreads.addMember(kernel);
        desertThreads.addMember(garbageCollector);
        desertThreads.addMember(bitwiseOperator);

        competitorRepository.saveAll(fiveExceptions.getMembers());
        competitorRepository.saveAll(desertThreads.getMembers());

        seedCompletedRace(byte_, fiveExceptions);
        seedInProgressRace(littleLambda, captainCache, desertThreads);
        seedOpenRace();

        log.info("Catálogo de ejemplo creado: 9 competidores, 2 equipos y 3 carreras.");
    }

    private Competitor competitor(String name, String nickname, CompetitorType type,
                                   String country, double weight, double height) {
        return Competitor.builder()
                .name(name)
                .nickname(nickname)
                .type(type)
                .birthDate(LocalDate.now().minusYears(20))
                .weight(weight)
                .height(height)
                .originCountry(country)
                .status(CompetitorStatus.ACTIVE)
                .build();
    }

    // Carrera ya finalizada, con inscripciones aprobadas y podio oficial
    // registrado: reproduce el escenario de demostración sugerido por la guía.
    private void seedCompletedRace(Competitor byte_, Team fiveExceptions) {
        LocalDateTime scheduledAt = LocalDateTime.now().minusMonths(6);
        Race race = raceRepository.save(Race.builder()
                .name("Gran Premio de Zúñiga 1979")
                .description("La carrera fundacional de la liga, disputada en el campus original de Zúñiga")
                .scheduledAt(scheduledAt)
                .startLocation("Campus Zúñiga")
                .finishLocation("Alto de las Palmas")
                .distanceMeters(1000.0)
                .maxParticipants(10)
                .type(RaceType.MIXED)
                .status(RaceStatus.COMPLETED)
                .organizerName("Mr. Abandonado")
                .registrationDeadline(scheduledAt.minusWeeks(1))
                .build());

        RaceRegistration byteRegistration = registrationRepository.save(RaceRegistration.builder()
                .race(race).competitor(byte_).status(RegistrationStatus.APPROVED)
                .startingPosition(2).performedBy("organizador").build());
        RaceRegistration teamRegistration = registrationRepository.save(RaceRegistration.builder()
                .race(race).team(fiveExceptions).status(RegistrationStatus.APPROVED)
                .startingPosition(1).performedBy("organizador").build());

        resultRepository.save(RaceResult.builder()
                .registration(teamRegistration).finalPosition(1)
                .completionTimeSeconds(180.5).penaltyTimeSeconds(0.0)
                .status(ResultStatus.FINISHED).notes("Ganador oficial")
                .recordedBy("organizador").build());
        resultRepository.save(RaceResult.builder()
                .registration(byteRegistration).finalPosition(2)
                .completionTimeSeconds(195.2).penaltyTimeSeconds(0.0)
                .status(ResultStatus.FINISHED).notes("Segundo lugar")
                .recordedBy("organizador").build());

        fiveExceptions.setWins(fiveExceptions.getWins() + 1);
        teamRepository.save(fiveExceptions);
        byte_.setRacesCompleted(byte_.getRacesCompleted() + 1);
        byte_.setLosses(byte_.getLosses() + 1);
        competitorRepository.save(byte_);
    }

    // Carrera en curso, ya con inscripciones aprobadas y lista para que se
    // carguen los tiempos oficiales durante la demostración en vivo.
    private void seedInProgressRace(Competitor littleLambda, Competitor captainCache, Team desertThreads) {
        LocalDateTime scheduledAt = LocalDateTime.now().minusDays(2);
        Race race = raceRepository.save(Race.builder()
                .name("Clásico Alto de Las Palmas")
                .description("Recorrido de montaña entre el campus y el mirador de Las Palmas")
                .scheduledAt(scheduledAt)
                .startLocation("Coliseo EIA")
                .finishLocation("Mirador Las Palmas")
                .distanceMeters(1500.0)
                .maxParticipants(8)
                .type(RaceType.MIXED)
                .status(RaceStatus.IN_PROGRESS)
                .organizerName("organizador")
                .registrationDeadline(scheduledAt.minusDays(5))
                .build());

        registrationRepository.save(RaceRegistration.builder()
                .race(race).competitor(littleLambda).status(RegistrationStatus.APPROVED)
                .startingPosition(1).performedBy("organizador").build());
        registrationRepository.save(RaceRegistration.builder()
                .race(race).competitor(captainCache).status(RegistrationStatus.APPROVED)
                .startingPosition(2).performedBy("organizador").build());
        registrationRepository.save(RaceRegistration.builder()
                .race(race).team(desertThreads).status(RegistrationStatus.APPROVED)
                .startingPosition(3).performedBy("organizador").build());
    }

    // Carrera todavía abierta para inscripciones, para que la demostración
    // pueda registrar participantes en vivo sin depender de datos previos.
    private void seedOpenRace() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusWeeks(3);
        raceRepository.save(Race.builder()
                .name("Derby Tecnológico EIA")
                .description("La nueva carrera insignia de la liga, abierta a individuos y equipos")
                .scheduledAt(scheduledAt)
                .startLocation("Plazoleta EIA")
                .finishLocation("Alto de las Palmas")
                .distanceMeters(2000.0)
                .maxParticipants(12)
                .type(RaceType.MIXED)
                .status(RaceStatus.OPEN_FOR_REGISTRATION)
                .organizerName("organizador")
                .registrationDeadline(scheduledAt.minusWeeks(1))
                .build());
    }
}
