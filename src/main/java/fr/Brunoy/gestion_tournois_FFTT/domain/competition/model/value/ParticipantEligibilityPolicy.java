package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.ForeignParticipant;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.GuestParticipant;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ParticipantEligibilityPolicy {

    private final boolean allowGuests;
    private final boolean allowForeignFederatedPlayers;

    /**
     * Si non vide => whitelist de pays autorisés (ISO-2 "BE", "CH", ...).
     * Si vide => tous pays acceptés.
     */
    private final Set<String> allowedCountryCodes;

    public ParticipantEligibilityPolicy(
            boolean allowGuests,
            boolean allowForeignFederatedPlayers,
            Set<String> allowedCountryCodes) {

        this.allowGuests = allowGuests;
        this.allowForeignFederatedPlayers = allowForeignFederatedPlayers;

        Set<String> norm = new HashSet<>();
        if (allowedCountryCodes != null) {
            for (String c : allowedCountryCodes) {
                if (c != null && !c.isBlank()) {
                    norm.add(c.trim().toUpperCase());
                }
            }
        }
        this.allowedCountryCodes = Collections.unmodifiableSet(norm);
    }

    public boolean allowGuests() {
        return allowGuests;
    }

    public boolean allowForeignFederatedPlayers() {
        return allowForeignFederatedPlayers;
    }

    public Set<String> allowedCountryCodes() {
        return allowedCountryCodes;
    }

    public void assertEligible(Participant participant) {
        if (participant == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_REQUIRED);

        if (participant instanceof GuestParticipant) {
            if (!allowGuests) {
                throw new BusinessException(ErrorCode.REGISTRATION_GUEST_NOT_ALLOWED);
            }
            return;
        }

        if (participant instanceof ForeignParticipant fp) {
            if (!allowForeignFederatedPlayers) {
                throw new BusinessException(ErrorCode.REGISTRATION_FOREIGN_NOT_ALLOWED);
            }

            if (!allowedCountryCodes.isEmpty()) {
                String cc = fp.nationalityCode();
                if (cc == null || cc.isBlank() || !allowedCountryCodes.contains(cc.trim().toUpperCase())) {
                    throw new BusinessException(ErrorCode.REGISTRATION_FOREIGN_COUNTRY_NOT_ALLOWED);
                }
            }
        }
        // FFTT : toujours OK
    }
}