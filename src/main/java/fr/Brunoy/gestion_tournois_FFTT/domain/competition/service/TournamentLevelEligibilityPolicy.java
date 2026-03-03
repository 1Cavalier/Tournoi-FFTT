package fr.Brunoy.gestion_tournois_FFTT.domain.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.model.Club;

public final class TournamentLevelEligibilityPolicy {

    public void assertEligible(Tournament tournament, Player player) {
        if (tournament == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REQUIRED);
        if (player == null)
            throw new BusinessException(ErrorCode.PLAYER_REQUIRED);

        TournamentLevel level = tournament.level();
        if (level == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_LEVEL_REQUIRED);

        Club org = tournament.organizingClub();
        if (org == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_ORGANIZING_CLUB_REQUIRED);

        switch (level) {
            case DEPARTEMENTAL -> assertSameDepartment(org, player);
            case REGIONAL -> assertSameRegion(org, player);
            case NATIONAL_A, NATIONAL_B -> {
                /* pas de restriction géographique */ }
            case INTERNATIONAL -> throw new BusinessException(ErrorCode.REGISTRATION_LEVEL_INTERNATIONAL_NOT_SUPPORTED);
        }
    }

    private void assertSameDepartment(Club org, Player player) {
        String depOrg = normalize(org.getDepartment().getCode());
        String depPlayer = normalize(player.getClub().getDepartment().getCode());
        if (!depOrg.equals(depPlayer)) {
            throw new BusinessException(ErrorCode.REGISTRATION_PLAYER_NOT_IN_DEPARTEMENT);
        }
    }

    private void assertSameRegion(Club org, Player player) {
        String regOrg = normalize(org.getDepartment().getRegion().getCode());
        String regPlayer = normalize(player.getClub().getDepartment().getRegion().getCode());
        if (!regOrg.equals(regPlayer)) {
            throw new BusinessException(ErrorCode.REGISTRATION_PLAYER_NOT_IN_REGION);
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }
}