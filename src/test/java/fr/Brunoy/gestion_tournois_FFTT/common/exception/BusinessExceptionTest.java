package fr.Brunoy.gestion_tournois_FFTT.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void shouldStoreErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.PLAYER_LICENSE_REQUIRED);
        assertEquals(ErrorCode.PLAYER_LICENSE_REQUIRED, ex.getCode());
    }

    @Test
    void shouldStoreCause() {
        RuntimeException cause = new RuntimeException("boom");
        BusinessException ex = new BusinessException(ErrorCode.TOURNAMENT_NAME_REQUIRED, cause);

        assertEquals(ErrorCode.TOURNAMENT_NAME_REQUIRED, ex.getCode());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldNotAcceptNullCode() {
        assertThrows(NullPointerException.class, () -> new BusinessException(null));
    }
}