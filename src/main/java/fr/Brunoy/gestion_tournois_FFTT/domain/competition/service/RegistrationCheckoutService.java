package fr.Brunoy.gestion_tournois_FFTT.domain.competition.service;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Registration;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tournament;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PaymentMode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.RegistrationStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class RegistrationCheckoutService {

    private final MultiTableauRegistrationService validator;
    private final Duration onlineHoldDuration = Duration.ofHours(1);

    public RegistrationCheckoutService(MultiTableauRegistrationService validator) {
        this.validator = validator;
    }

    /** 1) Récap + prix (sans écrire en “registrations”) */
    public RegistrationRecap recap(Tournament tournament, RegistrationDraft draft, PaymentMode paymentMode) {
        if (paymentMode == null)
            throw new BusinessException(ErrorCode.REGISTRATION_PAYMENT_MODE_REQUIRED);

        RegistrationSummary summary = validator.validate(tournament, draft);
        if (!summary.isValid()) {
            throw new BusinessException(ErrorCode.REGISTRATION_INVALID);
        }

        List<Tableau> selected = resolveSelectedTableaux(tournament, draft);

        List<RegistrationLine> lines = selected.stream()
                .map(t -> new RegistrationLine(
                        t.code(),
                        t.designation(), // si ton Tableau n’a pas designation(), remplace par ce que tu as
                        t.date(),
                        t.fee().amountFor(paymentMode)))
                .collect(Collectors.toList());

        return new RegistrationRecap(lines);
    }

    /** 2A) Paiement sur place : on confirme direct (place prise) */
    public String confirmOnSite(Tournament tournament, RegistrationDraft draft, Instant now) {
        RegistrationSummary summary = validator.validate(tournament, draft);
        if (!summary.isValid())
            throw new BusinessException(ErrorCode.REGISTRATION_INVALID);

        String batchId = UUID.randomUUID().toString();
        List<Tableau> selected = resolveSelectedTableaux(tournament, draft);

        for (Tableau t : selected) {
            ensureCapacityActive(tournament, t, now);

            tournament.registrationsFor(t.code()).add(
                    new Registration(batchId, draft.player(), t.code(),
                            PaymentMode.ON_SITE, RegistrationStatus.CONFIRMED, null));
        }
        return batchId;
    }

    /** 2B) Paiement en ligne : on réserve 1h (place bloquée) */
    public ReservationReceipt reserveOnline(Tournament tournament, RegistrationDraft draft, Instant now) {
        RegistrationSummary summary = validator.validate(tournament, draft);
        if (!summary.isValid())
            throw new BusinessException(ErrorCode.REGISTRATION_INVALID);

        String batchId = UUID.randomUUID().toString();
        Instant reservedUntil = now.plus(onlineHoldDuration);
        List<Tableau> selected = resolveSelectedTableaux(tournament, draft);

        for (Tableau t : selected) {
            ensureCapacityActive(tournament, t, now);

            tournament.registrationsFor(t.code()).add(
                    new Registration(batchId, draft.player(), t.code(),
                            PaymentMode.ONLINE, RegistrationStatus.RESERVED, reservedUntil));
        }

        return new ReservationReceipt(batchId, reservedUntil);
    }

    /** 3) Après paiement réussi : confirmer (transforme RESERVED -> CONFIRMED) */
    public void confirmOnlinePayment(Tournament tournament, String batchId, Instant now) {
        if (batchId == null || batchId.isBlank())
            throw new BusinessException(ErrorCode.REGISTRATION_BATCH_ID_REQUIRED);

        List<Registration> regs = findByBatchId(tournament, batchId);
        if (regs.isEmpty())
            throw new BusinessException(ErrorCode.REGISTRATION_BATCH_NOT_FOUND);

        // si une seule est expirée => on annule tout (il devra recommencer)
        boolean expired = regs.stream().anyMatch(r -> r.status() == RegistrationStatus.RESERVED && !r.isActiveAt(now));
        if (expired) {
            regs.forEach(Registration::cancel);
            throw new BusinessException(ErrorCode.REGISTRATION_RESERVATION_EXPIRED);
        }

        regs.stream()
                .filter(r -> r.status() == RegistrationStatus.RESERVED)
                .forEach(Registration::confirm);
    }

    /** 4) Nettoyage : annule toutes les réservations ONLINE expirées */
    public int cancelExpiredOnlineReservations(Tournament tournament, Instant now) {
        int removed = 0;

        for (Tableau t : tournament.tableaux()) {
            List<Registration> regs = tournament.registrationsFor(t.code());

            for (Iterator<Registration> it = regs.iterator(); it.hasNext();) {
                Registration r = it.next();

                if (r.status() == RegistrationStatus.RESERVED && !r.isActiveAt(now)) {
                    it.remove();
                    removed++;
                }
            }
        }
        return removed;
    }

    // -------- helpers --------

    private List<Tableau> resolveSelectedTableaux(Tournament tournament, RegistrationDraft draft) {
        List<Tableau> selected = new ArrayList<>();
        for (String code : draft.tableauCodes()) {
            Tableau t = tournament.tableaux().stream()
                    .filter(tb -> tb.code().equalsIgnoreCase(code))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_TABLEAU_NOT_FOUND));
            selected.add(t);
        }
        return selected;
    }

    private void ensureCapacityActive(Tournament tournament, Tableau tableau, Instant now) {
        long activeCount = tournament.registrationsFor(tableau.code()).stream()
                .filter(r -> r.isActiveAt(now))
                .count();
        if (activeCount >= tableau.maxPlayers()) {
            throw new BusinessException(ErrorCode.TABLEAU_FULL);
        }
    }

    private List<Registration> findByBatchId(Tournament tournament, String batchId) {
        List<Registration> out = new ArrayList<>();
        for (Tableau t : tournament.tableaux()) {
            for (Registration r : tournament.registrationsFor(t.code())) {
                if (batchId.equals(r.batchId())) {
                    out.add(r);
                }
            }
        }
        return out;
    }

    public record ReservationReceipt(String batchId, Instant reservedUntil) {
    }
}
