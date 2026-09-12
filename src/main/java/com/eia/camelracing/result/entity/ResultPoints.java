package com.eia.camelracing.result.entity;

import java.util.Map;

/**
 * Escala de puntos de la liga según la posición final. Solo aplica cuando
 * el resultado es FINISHED; cualquier otro estado (DNF, DSQ, DNS) vale 0.
 */
public class ResultPoints {

    private ResultPoints() {}

    private static final Map<Integer, Integer> POINTS_BY_POSITION = Map.of(
            1, 10,
            2, 7,
            3, 5,
            4, 3,
            5, 1
    );

    public static int pointsFor(ResultStatus status, Integer position) {
        if (status != ResultStatus.FINISHED || position == null) {
            return 0; // DNF y DSQ siempre valen 0 puntos
        }
        return POINTS_BY_POSITION.getOrDefault(position, 0); // 6to lugar en adelante: 0
    }
}