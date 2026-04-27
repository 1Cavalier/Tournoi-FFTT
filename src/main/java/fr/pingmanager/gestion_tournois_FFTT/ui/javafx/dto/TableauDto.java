package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto;

import java.util.List;

public record TableauDto(

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

        List<PrizeTierDto> prizeTiers,

        Integer poolSize, // 3 ou 4 — taille des poules de ce tableau
        Integer qualifiedPerPool, // 1 ou 2 — qualifiés par poule pour le KO
        String classificationMode, // NONE | THIRD_PLACE | TOP_8 | FULL

        String createdAt,
        String updatedAt) {
}