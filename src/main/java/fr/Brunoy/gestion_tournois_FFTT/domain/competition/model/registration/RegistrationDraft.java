package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class RegistrationDraft {

    private final Player player;
    private final Set<String> tableauCodes = new HashSet<>();

    public RegistrationDraft(Player player) {
        if (player == null)
            throw new BusinessException(ErrorCode.PLAYER_REQUIRED);
        this.player = player;
    }

    public Player player() {
        return player;
    }

    public void addTableau(String tableauCode) {
        if (tableauCode == null || tableauCode.isBlank()) {
            throw new BusinessException(ErrorCode.TABLEAU_CODE_REQUIRED);
        }
        tableauCodes.add(tableauCode.trim().toUpperCase());
    }

    public Set<String> tableauCodes() {
        return Collections.unmodifiableSet(tableauCodes);
    }

    public int size() {
        return tableauCodes.size();
    }
}
