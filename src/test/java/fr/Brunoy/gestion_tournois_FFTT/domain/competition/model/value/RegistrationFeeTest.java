package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.PaymentMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationFeeTest {

    @Test
    void shouldReturnCorrectAmountDependingOnPaymentMode() {
        RegistrationFee fee = new RegistrationFee(7, 10);

        assertEquals(7, fee.amountFor(PaymentMode.ONLINE));
        assertEquals(10, fee.amountFor(PaymentMode.ON_SITE));
    }

    @Test
    void negativeFeesShouldThrow() {
        BusinessException ex1 = assertThrows(BusinessException.class, () -> new RegistrationFee(-1, 0));
        assertEquals(ErrorCode.TABLEAU_FEE_NEGATIVE, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class, () -> new RegistrationFee(0, -1));
        assertEquals(ErrorCode.TABLEAU_FEE_NEGATIVE, ex2.getCode());
    }
}