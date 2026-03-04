package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.PaymentMode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.PaymentStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.RegistrationStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Player;

import java.time.Instant;
import java.util.Objects;

public final class Registration {

    private final String batchId; // optionnel (multi-tableaux plus tard)
    private final Player player;
    private final String tableauCode;

    private final Instant registeredAt;

    private final PaymentMode paymentMode; // ON_SITE / ONLINE
    private PaymentStatus paymentStatus; // UNPAID / PAID

    private Instant reservedUntil; // uniquement si RESERVED
    private RegistrationStatus status; // WAITLISTED / RESERVED / CONFIRMED / CANCELLED

    private Registration(
            String batchId,
            Player player,
            String tableauCode,
            PaymentMode paymentMode,
            PaymentStatus paymentStatus,
            RegistrationStatus status,
            Instant reservedUntil,
            Instant registeredAt) {
        this.batchId = normalizeOptional(batchId);
        this.player = Objects.requireNonNull(player, "player");
        this.tableauCode = Objects.requireNonNull(tableauCode, "tableauCode").trim().toUpperCase();

        this.paymentMode = Objects.requireNonNull(paymentMode, "paymentMode");
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus");

        this.status = Objects.requireNonNull(status, "status");
        this.registeredAt = Objects.requireNonNull(registeredAt, "registeredAt");

        this.reservedUntil = reservedUntil;

        validateInvariants();
    }

    // -------------------------------------------------------------------------
    // FACTORIES
    // -------------------------------------------------------------------------

    /** Inscription sur place : place prise, paiement non encaissé par défaut. */
    public static Registration onSiteConfirmed(Player player, String tableauCode, Instant now) {
        Instant at = (now != null) ? now : Instant.now();
        return new Registration(
                null,
                player,
                tableauCode,
                PaymentMode.ON_SITE,
                PaymentStatus.UNPAID,
                RegistrationStatus.CONFIRMED,
                null,
                at);
    }

    /**
     * Réservation en ligne : place bloquée jusqu'à reservedUntil, paiement en
     * attente.
     */
    public static Registration onlineReserved(
            Player player,
            String tableauCode,
            Instant reservedUntil,
            Instant now,
            String batchId) {
        Instant at = (now != null) ? now : Instant.now();
        Objects.requireNonNull(reservedUntil, "reservedUntil");
        return new Registration(
                batchId,
                player,
                tableauCode,
                PaymentMode.ONLINE,
                PaymentStatus.UNPAID,
                RegistrationStatus.RESERVED,
                reservedUntil,
                at);
    }

    /** Inscription en file d'attente (ne bloque pas de place). */
    public static Registration waitlisted(Player player, String tableauCode, Instant now, String batchId) {
        Instant at = (now != null) ? now : Instant.now();
        return new Registration(
                batchId,
                player,
                tableauCode,
                PaymentMode.ON_SITE, // choix simple par défaut (tu peux changer plus tard)
                PaymentStatus.UNPAID,
                RegistrationStatus.WAITLISTED,
                null,
                at);
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String batchId() {
        return batchId;
    }

    public Player player() {
        return player;
    }

    public String tableauCode() {
        return tableauCode;
    }

    public Instant registeredAt() {
        return registeredAt;
    }

    public PaymentMode paymentMode() {
        return paymentMode;
    }

    public PaymentStatus paymentStatus() {
        return paymentStatus;
    }

    public Instant reservedUntil() {
        return reservedUntil;
    }

    public RegistrationStatus status() {
        return status;
    }

    // -------------------------------------------------------------------------
    // LOGIC
    // -------------------------------------------------------------------------

    /** Active = pas CANCELLED et (si RESERVED) pas expirée. */
    public boolean isActiveAt(Instant now) {
        Instant at = (now != null) ? now : Instant.now();

        if (status == RegistrationStatus.CANCELLED)
            return false;

        if (status == RegistrationStatus.CONFIRMED || status == RegistrationStatus.WAITLISTED)
            return true;

        // RESERVED
        return reservedUntil != null && at.isBefore(reservedUntil);
    }

    /** Encaissement (sur place ou après paiement en ligne). */
    public void markPaid() {
        if (status == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Impossible d'encaisser une inscription annulée.");
        }
        this.paymentStatus = PaymentStatus.PAID;
    }

    /** Passe en file d'attente (ne bloque pas de place). */
    public void waitlist() {
        if (status == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Impossible de mettre en file d'attente une inscription annulée.");
        }
        this.status = RegistrationStatus.WAITLISTED;
        this.reservedUntil = null;
        validateInvariants();
    }

    /** Confirmation : place définitivement prise. */
    public void confirm() {
        if (status == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Impossible de confirmer une inscription annulée.");
        }
        this.status = RegistrationStatus.CONFIRMED;
        this.reservedUntil = null;
        validateInvariants();
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
        this.reservedUntil = null;
        validateInvariants();
    }

    // -------------------------------------------------------------------------
    // INVARIANTS
    // -------------------------------------------------------------------------

    private void validateInvariants() {
        // RESERVED => reservedUntil obligatoire
        if (status == RegistrationStatus.RESERVED && reservedUntil == null) {
            throw new IllegalArgumentException("reservedUntil obligatoire quand status=RESERVED");
        }

        // WAITLISTED/CONFIRMED/CANCELLED => reservedUntil doit être null
        if ((status == RegistrationStatus.WAITLISTED
                || status == RegistrationStatus.CONFIRMED
                || status == RegistrationStatus.CANCELLED) && reservedUntil != null) {
            throw new IllegalArgumentException("reservedUntil doit être null quand status=" + status);
        }
    }

    private static String normalizeOptional(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}