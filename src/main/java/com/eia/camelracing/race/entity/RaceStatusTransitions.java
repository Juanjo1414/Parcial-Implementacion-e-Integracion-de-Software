package com.eia.camelracing.race.entity;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Define qué transiciones de estado son válidas para una carrera.
 * Ej: desde DRAFT solo se puede pasar a OPEN_FOR_REGISTRATION o CANCELLED;
 * desde COMPLETED no se puede pasar a ningún otro estado (es terminal).
 */
public class RaceStatusTransitions {

    private RaceStatusTransitions() {}

    private static final Map<RaceStatus, Set<RaceStatus>> ALLOWED = new EnumMap<>(RaceStatus.class);

    static {
        ALLOWED.put(RaceStatus.DRAFT,
                EnumSet.of(RaceStatus.OPEN_FOR_REGISTRATION, RaceStatus.CANCELLED));
        ALLOWED.put(RaceStatus.OPEN_FOR_REGISTRATION,
                EnumSet.of(RaceStatus.CLOSED_FOR_REGISTRATION, RaceStatus.CANCELLED));
        ALLOWED.put(RaceStatus.CLOSED_FOR_REGISTRATION,
                EnumSet.of(RaceStatus.IN_PROGRESS, RaceStatus.CANCELLED));
        ALLOWED.put(RaceStatus.IN_PROGRESS,
                EnumSet.of(RaceStatus.COMPLETED, RaceStatus.CANCELLED));
        // COMPLETED y CANCELLED son estados terminales: no tienen salidas.
        ALLOWED.put(RaceStatus.COMPLETED, EnumSet.noneOf(RaceStatus.class));
        ALLOWED.put(RaceStatus.CANCELLED, EnumSet.noneOf(RaceStatus.class));
    }

    public static boolean isValid(RaceStatus from, RaceStatus to) {
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(RaceStatus.class)).contains(to);
    }
}