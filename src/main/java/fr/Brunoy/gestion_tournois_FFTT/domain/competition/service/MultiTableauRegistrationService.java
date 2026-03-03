package fr.Brunoy.gestion_tournois_FFTT.domain.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Registration;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationDraft;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationSummary;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationViolation;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public final class MultiTableauRegistrationService {

    private final TournamentLevelEligibilityPolicy levelEligibility;

    public MultiTableauRegistrationService(TournamentLevelEligibilityPolicy levelEligibility) {
        this.levelEligibility = Objects.requireNonNull(levelEligibility, "levelEligibility");
    }

    /** Valide la sélection multi-tableaux et retourne un récap des violations. */
    public RegistrationSummary validate(Tournament tournament, RegistrationDraft draft, Instant now) {

        if (tournament == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REQUIRED);
        if (draft == null)
            throw new BusinessException(ErrorCode.REGISTRATION_REQUIRED);

        final Instant at = (now != null) ? now : Instant.now();

        Player player = draft.player();
        List<RegistrationViolation> violations = new ArrayList<>();

        // 0) niveau du tournoi (bloquant)
        try {
            levelEligibility.assertEligible(tournament, player);
        } catch (BusinessException e) {
            violations.add(new RegistrationViolation(e.getCode(), null));
            return new RegistrationSummary(violations);
        }

        // 1) certificat médical (bloquant)
        if (!player.hasValidMedicalCertificate()) {
            violations.add(new RegistrationViolation(ErrorCode.REGISTRATION_MEDICAL_CERT_INVALID, null));
            return new RegistrationSummary(violations);
        }

        // 2) max total tableaux (sélection)
        if (draft.size() > tournament.registrationPolicy().maxTotalTableaux()) {
            violations.add(new RegistrationViolation(
                    ErrorCode.REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED,
                    String.valueOf(draft.size())));
        }

        // 3) resolve tableaux + éligibilité tableau + déjà inscrit + capacité active
        Map<LocalDate, List<Tableau>> selectedByDay = new HashMap<>();

        for (String code : draft.tableauCodes()) {

            Tableau tableau = findTableau(tournament, code);
            if (tableau == null) {
                violations.add(new RegistrationViolation(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND, code));
                continue;
            }

            // éligibilité (points + genre, selon tableau.accepts)
            boolean eligible = tableau.accepts(
                    player.pointsFor(tournament.rankingPhase()),
                    player.isFemale());

            if (!eligible) {
                violations.add(new RegistrationViolation(ErrorCode.REGISTRATION_NOT_ELIGIBLE, tableau.code()));
                continue;
            }

            // déjà inscrit ACTIF à ce tableau ?
            boolean alreadyActive = tournament.registrationsFor(tableau.code()).stream()
                    .anyMatch(r -> r.player().equals(player) && r.isActiveAt(at));

            if (alreadyActive) {
                violations.add(new RegistrationViolation(ErrorCode.REGISTRATION_ALREADY_REGISTERED, tableau.code()));
                continue;
            }

            // capacité ACTIVE (inclut réservations online non expirées)
            long activeCount = tournament.activeRegistrationsCount(tableau.code(), at);
            if (activeCount >= tableau.maxPlayers()) {
                violations.add(new RegistrationViolation(ErrorCode.TABLEAU_FULL, tableau.code()));
                continue;
            }

            selectedByDay.computeIfAbsent(tableau.date(), d -> new ArrayList<>()).add(tableau);
        }

        // 4) max tableaux / jour (+ bonus féminin centralisé dans policy)
        for (Map.Entry<LocalDate, List<Tableau>> entry : selectedByDay.entrySet()) {

            LocalDate day = entry.getKey();
            List<Tableau> dayTabs = entry.getValue();

            int selectedCount = dayTabs.size();
            int allowed = tournament.registrationPolicy().allowedTableauxPerDay(player, dayTabs);

            if (selectedCount > allowed) {
                violations.add(new RegistrationViolation(
                        ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED,
                        day.toString()));
            }
        }

        return new RegistrationSummary(violations);
    }

    /** Inscription finale : ne passe que si validate() est OK. */
    public void register(Tournament tournament, RegistrationDraft draft, Instant now) {

        final Instant at = (now != null) ? now : Instant.now();

        RegistrationSummary summary = validate(tournament, draft, at);
        if (!summary.isValid()) {
            throw new BusinessException(ErrorCode.REGISTRATION_INVALID);
        }

        // On insère via l’agrégat (invariants : unicité + capacité active)
        for (String code : draft.tableauCodes()) {
            Tableau tableau = findTableau(tournament, code);
            if (tableau == null)
                continue;

            tournament.addRegistration(new Registration(draft.player(), tableau.code()), at);
        }
    }

    // compat : anciennes signatures
    public RegistrationSummary validate(Tournament tournament, RegistrationDraft draft) {
        return validate(tournament, draft, Instant.now());
    }

    public void register(Tournament tournament, RegistrationDraft draft) {
        register(tournament, draft, Instant.now());
    }

    // -------- helper --------

    private Tableau findTableau(Tournament tournament, String code) {
        if (code == null || code.isBlank())
            return null;

        for (Tableau t : tournament.tableaux()) {
            if (t.code().equalsIgnoreCase(code)) {
                return t;
            }
        }
        return null;
    }
}