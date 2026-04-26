package fr.pingmanager.gestion_tournois_FFTT.domain.competition.service;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.bracket.KoBracket;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification.ClassificationBracket;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.pool.Poule;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object résultat d'une phase de poules.
 *
 * Contient :
 * - les poules constituées et jouées
 * - le tableau KO généré depuis les qualifiés des poules
 * - le bracket de classement (optionnel, généré après la fin du KO)
 *
 * Immuable : tous les champs sont en lecture seule.
 */
public final class PoolPhaseResult {

    private final String tableauCode;
    private final List<Poule> poules;
    private final KoBracket koBracket;

    /**
     * Bracket de classement. Null tant que le KO n'est pas terminé.
     * Généré via {@code ClassificationBracket.from(koBracket, mode)}.
     */
    private final ClassificationBracket classificationBracket;

    // -------------------------------------------------------------------------
    // CONSTRUCTEURS
    // -------------------------------------------------------------------------

    /** Résultat après tirage des poules (KO pas encore construit). */
    public PoolPhaseResult(String tableauCode, List<Poule> poules) {
        this.tableauCode = Objects.requireNonNull(tableauCode);
        this.poules = Collections.unmodifiableList(poules);
        this.koBracket = null;
        this.classificationBracket = null;
    }

    /** Résultat après construction du KO (classement pas encore généré). */
    public PoolPhaseResult(String tableauCode, List<Poule> poules, KoBracket koBracket) {
        this.tableauCode = Objects.requireNonNull(tableauCode);
        this.poules = Collections.unmodifiableList(poules);
        this.koBracket = Objects.requireNonNull(koBracket);
        this.classificationBracket = null;
    }

    /** Résultat complet (poules + KO + classement). */
    public PoolPhaseResult(String tableauCode, List<Poule> poules,
            KoBracket koBracket,
            ClassificationBracket classificationBracket) {
        this.tableauCode = Objects.requireNonNull(tableauCode);
        this.poules = Collections.unmodifiableList(poules);
        this.koBracket = Objects.requireNonNull(koBracket);
        this.classificationBracket = classificationBracket; // peut être null si NONE
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    public boolean allPoolMatchesFinished() {
        return poules.stream().allMatch(Poule::allMatchesFinished);
    }

    public boolean koBracketBuilt() {
        return koBracket != null;
    }

    public boolean koBracketComplete() {
        return koBracket != null && koBracket.isComplete();
    }

    public boolean classificationComplete() {
        return classificationBracket == null
                || classificationBracket.allMatchesFinished();
    }

    public boolean isFullyComplete() {
        return allPoolMatchesFinished() && koBracketComplete() && classificationComplete();
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String tableauCode() {
        return tableauCode;
    }

    public List<Poule> poules() {
        return poules;
    }

    public KoBracket koBracket() {
        return koBracket;
    }

    public ClassificationBracket classificationBracket() {
        return classificationBracket;
    }
}