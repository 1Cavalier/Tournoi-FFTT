package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.util.List;

import fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto.PrizeTierDto;

public record UpdateTableauCommand(

        String id,
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