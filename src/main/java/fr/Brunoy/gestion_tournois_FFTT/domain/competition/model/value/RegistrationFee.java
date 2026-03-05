package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.PaymentMode;

public final class RegistrationFee {

    private final int prepaid;
    private final int onSite;

    public RegistrationFee(int prepaid, int onSite) {
        if (prepaid < 0 || onSite < 0) {
            throw new BusinessException(ErrorCode.TABLEAU_FEE_NEGATIVE);
        }
        this.prepaid = prepaid;
        this.onSite = onSite;
    }

    public int prepaid() {
        return prepaid;
    }

    public int onSite() {
        return onSite;
    }

    public int amountFor(PaymentMode mode) {
        if (mode == null) {
            throw new BusinessException(ErrorCode.REGISTRATION_PAYMENT_MODE_REQUIRED);
        }
        return mode == PaymentMode.ONLINE ? prepaid : onSite;
    }

    @Override
    public String toString() {
        return "ONLINE=" + prepaid + "€, ON_SITE=" + onSite + "€";
    }
}