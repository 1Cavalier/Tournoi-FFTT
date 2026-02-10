package fr.Brunoy.gestion_tournois_FFTT.domain.model.competition;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.RegistrationFee;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationFeeTest {

    @Test
    void shouldCreateFee_whenValuesAreNonNegative() {
        RegistrationFee fee = new RegistrationFee(8, 10);

        assertEquals(8, fee.prepaid());
        assertEquals(10, fee.onSite());
    }

    @Test
    void shouldThrow_whenFeeIsNegative() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new RegistrationFee(-1, 10));

        assertEquals(ErrorCode.TABLEAU_FEE_NEGATIVE, ex.getCode());
    }
}
