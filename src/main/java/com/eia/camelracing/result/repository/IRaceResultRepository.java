package com.eia.camelracing.result.repository;

import com.eia.camelracing.result.entity.RaceResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRaceResultRepository extends JpaRepository<RaceResult, UUID> {

    // Spring Data recorre la cadena registration -> race -> id automáticamente
    // a partir del nombre del método. Muy útil para no escribir JPQL a mano.
    List<RaceResult> findByRegistration_Race_Id(UUID raceId);

    boolean existsByRegistration_Race_IdAndFinalPosition(UUID raceId, Integer finalPosition);

    boolean existsByRegistration_Id(UUID registrationId);

    // Permiten bloquear el borrado físico de un competidor o equipo que ya
    // tiene resultados oficiales registrados.
    boolean existsByRegistration_Competitor_Id(UUID competitorId);

    boolean existsByRegistration_Team_Id(UUID teamId);

    Optional<RaceResult> findByRegistration_Id(UUID registrationId);
}