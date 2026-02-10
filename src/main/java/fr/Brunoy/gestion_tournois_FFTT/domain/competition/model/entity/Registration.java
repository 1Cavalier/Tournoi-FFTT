package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity;

import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PaymentMode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.RegistrationStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;

import java.time.Instant;
import java.util.Objects;

public final class Registration {

    private final String batchId; // pour grouper multi-tableaux (nullable)
    private final Player player;
    private final String tableauCode;

    private final Instant registeredAt;

    private final PaymentMode paymentMode; // ON_SITE / ONLINE
    private Instant reservedUntil; // utilisé seulement si ONLINE + RESERVED

    private RegistrationStatus status;

    /**
     * Constructeur "simple" (backward compatible) :
     * - Utilisé par ton RegistrationService existant
     * - Met CONFIRMED (place prise) par défaut
     */
    public Registration(Player player, String tableauCode) {
        this(null, player, tableauCode, PaymentMode.ON_SITE, RegistrationStatus.CONFIRMED, null);
    }

    /**
     * Constructeur complet pour le checkout (online reserve / confirm / etc.)
     */
    public Registration(
            String batchId,
            Player player,
            String tableauCode,
            PaymentMode paymentMode,
            RegistrationStatus status,
            Instant reservedUntil) {
        this.batchId = batchId;
        this.player = Objects.requireNonNull(player, "player");
        this.tableauCode = Objects.requireNonNull(tableauCode, "tableauCode").trim().toUpperCase();
        this.paymentMode = Objects.requireNonNull(paymentMode, "paymentMode");
        this.status = Objects.requireNonNull(status, "status");
        this.registeredAt = Instant.now();
        this.reservedUntil = reservedUntil;
    }

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

    public Instant reservedUntil() {
        return reservedUntil;
    }

    public RegistrationStatus status() {
        return status;
    }

    public boolean isActiveAt(Instant now) {
        if (status == RegistrationStatus.CANCELLED)
            return false;
        if (status == RegistrationStatus.CONFIRMED)
            return true;

        // RESERVED
        if (reservedUntil == null)
            return true; // sécurité, mais normalement ONLINE a une date
        return now.isBefore(reservedUntil);
    }

    public void confirm() {
        this.status = RegistrationStatus.CONFIRMED;
        this.reservedUntil = null;
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
        this.reservedUntil = null;
    }
}
