package com.eia.camelracing.competitor.mapper;

import com.eia.camelracing.competitor.dto.CompetitorRequest;
import com.eia.camelracing.competitor.dto.CompetitorResponse;
import com.eia.camelracing.competitor.entity.Competitor;

// Clase de solo métodos estáticos: no necesita ser un @Bean de Spring,
// no tiene estado, solo transforma datos de un lado a otro.
public class CompetitorMapper {

    private CompetitorMapper() {} // evita que alguien haga "new CompetitorMapper()"

    public static Competitor toEntity(CompetitorRequest request) {
        if (request == null) return null;
        return Competitor.builder()
                .name(request.name())
                .nickname(request.nickname())
                .type(request.type())
                .birthDate(request.birthDate())
                .weight(request.weight())
                .height(request.height())
                .originCountry(request.originCountry())
                .build();
    }

    public static CompetitorResponse toResponse(Competitor competitor) {
        if (competitor == null) return null;
        return new CompetitorResponse(
                competitor.getId(),
                competitor.getName(),
                competitor.getNickname(),
                competitor.getType(),
                competitor.getBirthDate(),
                competitor.getWeight(),
                competitor.getHeight(),
                competitor.getOriginCountry(),
                competitor.getStatus(),
                competitor.getRegisteredAt(),
                competitor.getWins(),
                competitor.getLosses(),
                competitor.getRacesCompleted()
        );
    }
}