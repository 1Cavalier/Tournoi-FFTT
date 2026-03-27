package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.service;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.PrizeTierDto;

import java.util.List;

public record CreateTableauCommand(

        String tournamentId,

        String code,
        String designation,
        String date,

        String genderPolicy,

        String agePolicyType,
        String ageMinCategory,
        String ageMaxCategory,
        List<String> allowedAgeCategories,

        String pointsRuleType,
        Integer minPoints,
        Integer maxPoints,

        Integer maxPlayers,
        Integer waitlistCapacity,

        String checkInEnd,
        String startTime,

        Integer prepaidFee,
        Integer onSiteFee,

        List<PrizeTierDto> prizeTiers) {
}