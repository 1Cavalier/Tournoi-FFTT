package fr.Brunoy.gestion_tournois_FFTT.domain.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Registration;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;

import java.util.List;
import java.util.Objects;

public final class RegistrationService {

    private final TournamentLevelEligibilityPolicy levelEligibility;

    public RegistrationService(TournamentLevelEligibilityPolicy levelEligibility) {
        this.levelEligibility = Objects.requireNonNull(levelEligibility, "levelEligibility");
    }

    public void register(Tournament tournament, Player player, String tableauCode) {

        if (tournament == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REQUIRED);
        if (player == null)
            throw new BusinessException(ErrorCode.PLAYER_REQUIRED);

        // 0) éligibilité niveau (départemental/régional/national)
        levelEligibility.assertEligible(tournament, player);

        // 1) tableau existant
        Tableau tableau = tournament.tableaux().stream()
                .filter(t -> t.code().equalsIgnoreCase(tableauCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND));

        // 2) certificat médical
        if (!player.hasValidMedicalCertificate()) {
            throw new BusinessException(ErrorCode.REGISTRATION_MEDICAL_CERT_INVALID);
        }

        // 3) éligibilité tableau (points + genre)
        if (!tableau.accepts(player.pointsFor(tournament.rankingPhase()), player.isFemale())) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_ELIGIBLE);
        }

        // 4) pas déjà inscrit
        List<Registration> registrations = tournament.registrationsFor(tableau.code());
        boolean alreadyRegistered = registrations.stream().anyMatch(r -> r.player().equals(player));
        if (alreadyRegistered) {
            throw new BusinessException(ErrorCode.REGISTRATION_ALREADY_REGISTERED);
        }

        // 5) capacité tableau
        if (registrations.size() >= tableau.maxPlayers()) {
            throw new BusinessException(ErrorCode.TABLEAU_FULL);
        }

        // 6) inscription
        registrations.add(new Registration(player, tableau.code()));
    }
}