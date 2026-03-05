package fr.Brunoy.gestion_tournois_FFTT.common.exception;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMessagesTest {

    @Test
    void everyErrorCodeShouldHaveANonBlankMessage() {
        for (ErrorCode code : ErrorCode.values()) {
            String msg = ErrorMessages.message(code);
            assertNotNull(msg, "Message null pour " + code);
            assertFalse(msg.isBlank(), "Message vide pour " + code);

            // Pro : évite les placeholders
            assertNotEquals("a", msg.trim(), "Placeholder détecté pour " + code);
        }
    }

    @Test
    void messagesShouldBeUniqueOrAtLeastNotTooDuplicated() {
        // Ce test est optionnel, mais très utile en "pro".
        // Si tu as volontairement des doublons, tu peux l'assouplir.
        Set<String> seen = new HashSet<>();
        for (ErrorCode code : ErrorCode.values()) {
            String msg = ErrorMessages.message(code).trim();
            assertTrue(seen.add(msg), "Message dupliqué détecté : '" + msg + "' (code=" + code + ")");
        }
    }

    @Test
    void messageShouldNotThrowForAnyErrorCode() {
        assertDoesNotThrow(() -> {
            for (ErrorCode code : ErrorCode.values()) {
                ErrorMessages.message(code);
            }
        });
    }
}