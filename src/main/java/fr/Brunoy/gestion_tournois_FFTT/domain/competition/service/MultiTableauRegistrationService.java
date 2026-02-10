package fr.Brunoy.gestion_tournois_FFTT.domain.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Registration;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationDraft;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationSummary;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.RegistrationViolation;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.FemaleExtraRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;

import java.time.LocalDate;
import java.util.*;

public final class MultiTableauRegistrationService {

    /** Valide la sélection multi-tableaux et retourne un récap des violations. */
    public RegistrationSummary validate(Tournament tournament, RegistrationDraft draft) {

        if (tournament == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_REQUIRED);
        if (draft == null)
            throw new BusinessException(ErrorCode.REGISTRATION_REQUIRED);

        Player player = draft.player();
        List<RegistrationViolation> violations = new ArrayList<>();

        // 0) certificat médical (bloquant)
        if (!player.hasValidMedicalCertificate()) {
            violations.add(new RegistrationViolation(ErrorCode.REGISTRATION_MEDICAL_CERT_INVALID, null));
            return new RegistrationSummary(violations);
        }

        // 1) max total tableaux
        if (draft.size() > tournament.registrationPolicy().maxTotalTableaux()) {
            violations.add(new RegistrationViolation(
                    ErrorCode.REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED,
                    String.valueOf(draft.size())));
        }

        // 2) resolve tableaux + éligibilité tableau + déjà inscrit + capacité
        Map<LocalDate, List<Tableau>> selectedByDay = new HashMap<>();

        for (String code : draft.tableauCodes()) {

            Tableau tableau = findTableau(tournament, code);
            if (tableau == null) {
                violations.add(new RegistrationViolation(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND, code));
                continue;
            }

            // éligibilité (points + genre, selon ton tableau.accepts)
            boolean eligible = tableau.accepts(
                    player.pointsFor(tournament.rankingPhase()),
                    player.isFemale());
            if (!eligible) {
                violations.add(new RegistrationViolation(ErrorCode.REGISTRATION_NOT_ELIGIBLE, tableau.code()));
                continue;
            }

            // déjà inscrit à ce tableau ?
            List<Registration> regs = tournament.registrationsFor(tableau.code());
            boolean alreadyRegistered = regs.stream().anyMatch(r -> r.player().equals(player));
            if (alreadyRegistered) {
                violations.add(new RegistrationViolation(ErrorCode.REGISTRATION_ALREADY_REGISTERED, tableau.code()));
                continue;
            }

            // capacité tableau
            if (regs.size() >= tableau.maxPlayers()) {
                violations.add(new RegistrationViolation(ErrorCode.TABLEAU_FULL, tableau.code()));
                continue;
            }

            selectedByDay.computeIfAbsent(tableau.date(), d -> new ArrayList<>()).add(tableau);
        }

        // 3) max tableaux / jour (+ extra féminin configurable)
        int maxPerDay = tournament.registrationPolicy().maxTableauxPerDay();
        FemaleExtraRuleType ruleType = tournament.registrationPolicy().femaleExtraRuleType();
        String extraCode = tournament.registrationPolicy().femaleExtraTableauCode();

        for (Map.Entry<LocalDate, List<Tableau>> entry : selectedByDay.entrySet()) {
            LocalDate day = entry.getKey();
            List<Tableau> dayTabs = entry.getValue();

            int selectedCount = dayTabs.size();

            int extraUsed = 0;
            if (player.isFemale()) {
                switch (ruleType) {
                    case NONE:
                        extraUsed = 0;
                        break;

                    case ANY_TABLEAU:
                        // +1 seulement si elle en a besoin (si elle dépasse maxPerDay)
                        extraUsed = (selectedCount > maxPerDay) ? 1 : 0;
                        break;

                    case SPECIFIC_TABLEAU_CODE:
                        // +1 seulement si elle a choisi le tableau spécifique (optionnel)
                        boolean hasSpecific = extraCode != null && dayTabs.stream()
                                .anyMatch(t -> t.code().equalsIgnoreCase(extraCode));
                        extraUsed = hasSpecific ? 1 : 0;
                        break;
                }
            }

            int allowed = maxPerDay + extraUsed;

            if (selectedCount > allowed) {
                violations.add(new RegistrationViolation(
                        ErrorCode.REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED,
                        day.toString()));
            }
        }

        return new RegistrationSummary(violations);
    }

    /** Inscription finale : ne passe que si validate() est OK */
    public void register(Tournament tournament, RegistrationDraft draft) {
        RegistrationSummary summary = validate(tournament, draft);
        if (!summary.isValid()) {
            throw new BusinessException(ErrorCode.REGISTRATION_INVALID);
        }

        // On réinsère "simplement" les inscriptions (tableaux déjà validés)
        for (String code : draft.tableauCodes()) {
            Tableau tableau = findTableau(tournament, code);
            if (tableau == null)
                continue;

            List<Registration> regs = tournament.registrationsFor(tableau.code());
            boolean alreadyRegistered = regs.stream().anyMatch(r -> r.player().equals(draft.player()));
            if (alreadyRegistered)
                continue;

            if (regs.size() >= tableau.maxPlayers()) {
                throw new BusinessException(ErrorCode.TABLEAU_FULL);
            }

            regs.add(new Registration(draft.player(), tableau.code()));
        }
    }

    private Tableau findTableau(Tournament tournament, String code) {
        if (code == null)
            return null;
        for (Tableau t : tournament.tableaux()) {
            if (t.code().equalsIgnoreCase(code))
                return t;
        }
        return null;
    }
}
