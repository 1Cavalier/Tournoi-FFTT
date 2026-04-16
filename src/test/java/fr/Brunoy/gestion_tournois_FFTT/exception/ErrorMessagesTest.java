package fr.Brunoy.gestion_tournois_FFTT.exception;

import org.junit.jupiter.api.Test;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorMessages;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMessagesTest {

    @Test
    void everyErrorCodeShouldHaveANonBlankMessage() {
        for (ErrorCode code : ErrorCode.values()) {
            String msg = ErrorMessages.message(code);

            assertNotNull(msg, "Message null pour " + code);
            assertFalse(msg.isBlank(), "Message vide pour " + code);

            // évite placeholders / oublis
            String trimmed = msg.trim();
            assertNotEquals("a", trimmed, "Placeholder détecté pour " + code);
            assertNotEquals("TODO", trimmed.toUpperCase(), "TODO détecté pour " + code);
        }
    }

    @Test
    void messagesShouldNotBeOverlyDuplicated() {
        // Pro mais réaliste :
        // - on autorise des doublons
        // - mais on évite que 80% des codes pointent vers 3 messages
        Map<String, Integer> counts = new HashMap<>();

        for (ErrorCode code : ErrorCode.values()) {
            String msg = ErrorMessages.message(code).trim();
            counts.merge(msg, 1, Integer::sum);
        }

        int maxDup = counts.values().stream().mapToInt(i -> i).max().orElse(0);

        // règle simple (ajuste si tu veux) :
        // aucun message ne doit être utilisé par plus de 6 codes
        assertTrue(maxDup <= 6,
                "Trop de duplication : un message est réutilisé " + maxDup + " fois. " +
                        "Soit c'est voulu (augmente le seuil), soit il manque des messages spécifiques.");
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