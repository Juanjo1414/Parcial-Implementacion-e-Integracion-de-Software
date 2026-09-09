package com.eia.camelracing.competitor.entity;

// Estado actual del competidor. Solo ACTIVE puede inscribirse en carreras.
public enum CompetitorStatus {
    ACTIVE, INJURED, SUSPENDED, RETIRED
}