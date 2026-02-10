package fr.Brunoy.gestion_tournois_FFTT.domain.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;

import java.util.List;

public final class RegistrationService {

    public void register(
            Tournament tournament,
            Player player,
            String tableauCode) {

        if (tournament == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REQUIRED);
        if (player == null)
            throw new BusinessException(ErrorCode.PLAYER_REQUIRED);

        // 1️tableau existant
        Tableau tableau = tournament.tableaux().stream()
                .filter(t -> t.code().equalsIgnoreCase(tableauCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND));

        // 2️certificat médical
        if (!player.hasValidMedicalCertificate()) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_ELIGIBLE);
        }

        // 3️éligibilité tableau (points, genre, âge, etc.)
        if (!tableau.accepts(
                player.pointsFor(tournament.rankingPhase()),
                player.isFemale())) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_ELIGIBLE);
        }

        // 4️ pas déjà inscrit
        List<Registration> registrations = tournament.registrationsFor(tableau.code());

        boolean alreadyRegistered = registrations.stream()
                .anyMatch(r -> r.player().equals(player));

        if (alreadyRegistered) {
            throw new BusinessException(ErrorCode.REGISTRATION_ALREADY_REGISTERED);
        }

        // 5️ capacité tableau
        if (registrations.size() >= tableau.maxPlayers()) {
            throw new BusinessException(ErrorCode.TABLEAU_FULL);
        }

        // 6️ inscription
        registrations.add(new Registration(player, tableau.code()));
    }
}
