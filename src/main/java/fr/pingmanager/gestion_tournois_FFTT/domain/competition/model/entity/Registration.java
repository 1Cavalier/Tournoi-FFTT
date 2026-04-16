package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.entity;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.PaymentMode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.PaymentStatus;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.RegistrationStatus;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.FfttParticipant;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Player;

/**
 * Inscription à un tableau.
 *
 * V1 :
 * - WAITLISTED / RESERVED / CONFIRMED / CANCELLED
 * - ONLINE/ON_SITE + PAID/UNPAID
 * - RESERVED expire automatiquement via isActiveAt(now)
 * - Invariants stricts sur reservedUntil
 *
 * IMPORTANT : on stocke un Participant (FFTT / guest / foreign).
 * Des factories "Player" existent pour compat, sans casser l'UI actuelle.
 */
public final class Registration {

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------

    private final String batchId; // optionnel (multi-tableaux plus tard)
    private final Participant participant; // FFTT/guest/foreign
    private final String tableauCode; // normalisé (trim + upper)

    private final Instant registeredAt;

    private final PaymentMode paymentMode; // ON_SITE / ONLINE
    private PaymentStatus paymentStatus; // UNPAID / PAID

    private Instant reservedUntil; // uniquement si RESERVED
    private RegistrationStatus status; // WAITLISTED / RESERVED / CONFIRMED / CANCELLED

    // -------------------------------------------------------------------------
    // CONSTRUCTOR (private)
    // -------------------------------------------------------------------------

    private Registration(
            String batchId,
            Participant participant,
            String tableauCode,
            PaymentMode paymentMode,
            PaymentStatus paymentStatus,
            RegistrationStatus status,
            Instant reservedUntil,
            Instant registeredAt) {

        this.batchId = normalizeOptional(batchId);

        this.participant = Objects.requireNonNull(participant, "participant");

        this.tableauCode = normalizeRequiredTableauCode(tableauCode);

        this.paymentMode = Objects.requireNonNull(paymentMode, "paymentMode");
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus");

        this.status = Objects.requireNonNull(status, "status");
        this.registeredAt = Objects.requireNonNull(registeredAt, "registeredAt");

        this.reservedUntil = reservedUntil;

        validateInvariants();
    }

    // -------------------------------------------------------------------------
    // FACTORIES (Participant)
    // -------------------------------------------------------------------------

    /** Inscription sur place : place prise, paiement non encaissé par défaut. */
    public static Registration onSiteConfirmed(Participant participant, String tableauCode, Instant now) {
        Instant at = (now != null) ? now : Instant.now();
        return new Registration(
                null,
                participant,
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
            Participant participant,
            String tableauCode,
            Instant reservedUntil,
            Instant now,
            String batchId) {

        Instant at = (now != null) ? now : Instant.now();
        Objects.requireNonNull(reservedUntil, "reservedUntil");

        return new Registration(
                batchId,
                participant,
                tableauCode,
                PaymentMode.ONLINE,
                PaymentStatus.UNPAID,
                RegistrationStatus.RESERVED,
                reservedUntil,
                at);
    }

    /** Inscription en file d'attente (ne bloque pas de place). */
    public static Registration waitlisted(Participant participant, String tableauCode, Instant now, String batchId) {
        Instant at = (now != null) ? now : Instant.now();
        return new Registration(
                batchId,
                participant,
                tableauCode,
                PaymentMode.ON_SITE, // choix simple par défaut (modifiable plus tard)
                PaymentStatus.UNPAID,
                RegistrationStatus.WAITLISTED,
                null,
                at);
    }

    // -------------------------------------------------------------------------
    // FACTORIES (compat Player FFTT)
    // -------------------------------------------------------------------------

    public static Registration onSiteConfirmed(Player player, String tableauCode, Instant now) {
        return onSiteConfirmed(new FfttParticipant(player), tableauCode, now);
    }

    public static Registration onlineReserved(
            Player player,
            String tableauCode,
            Instant reservedUntil,
            Instant now,
            String batchId) {
        return onlineReserved(new FfttParticipant(player), tableauCode, reservedUntil, now, batchId);
    }

    public static Registration waitlisted(Player player, String tableauCode, Instant now, String batchId) {
        return waitlisted(new FfttParticipant(player), tableauCode, now, batchId);
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String batchId() {
        return batchId;
    }

    public Participant participant() {
        return participant;
    }

    /**
     * Compat / debug uniquement : retourne le Player FFTT si c'est un participant
     * FFTT,
     * sinon null (guest/foreign).
     */
    public Player ffttPlayerOrNull() {
        if (participant instanceof FfttParticipant fp) {
            return fp.player();
        }
        return null;
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
    // BUSINESS RULES
    // -------------------------------------------------------------------------

    /**
     * Active = pas CANCELLED et (si RESERVED) pas expirée.
     * WAITLISTED est actif (mais ne bloque pas de place).
     */
    public boolean isActiveAt(Instant now) {
        Instant at = (now != null) ? now : Instant.now();

        if (status == RegistrationStatus.CANCELLED) {
            return false;
        }

        if (status == RegistrationStatus.CONFIRMED || status == RegistrationStatus.WAITLISTED) {
            return true;
        }

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

    /** Annulation : libère la place / supprime toute réservation. */
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
                || status == RegistrationStatus.CANCELLED)
                && reservedUntil != null) {
            throw new IllegalArgumentException("reservedUntil doit être null quand status=" + status);
        }

        // Note : en V1, on reste permissif pour ONLINE + WAITLISTED (possible plus
        // tard).
        // Si tu veux durcir : lever une exception ici.
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private static String normalizeRequiredTableauCode(String tableauCode) {
        String raw = Objects.requireNonNull(tableauCode, "tableauCode").trim();
        if (raw.isEmpty()) {
            // Ici je mets une IllegalArgumentException (pas BusinessException) car c'est un
            // invariant "tech"
            // Si tu veux être 100% métier, crée un ErrorCode TABLEAU_CODE_REQUIRED côté
            // registration.
            throw new IllegalArgumentException("tableauCode obligatoire");
        }
        return raw.toUpperCase(Locale.ROOT);
    }

    private static String normalizeOptional(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}