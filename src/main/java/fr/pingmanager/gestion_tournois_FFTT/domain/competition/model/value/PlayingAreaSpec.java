package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.PlayingAreaPreset;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;

/**
 * Spécification "aire de jeu" pour le règlement.
 * - Standard selon niveau
 * - Mode CUSTOM possible
 *
 * Pro :
 * - si compliant=false => publication officielle impossible
 */
public final class PlayingAreaSpec implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final PlayingAreaPreset preset;
    private final String infoText;

    // Optionnel : utile en CUSTOM ou si tu veux afficher des chiffres
    private final Integer lengthMeters;
    private final Integer widthMeters;

    /**
     * Indique si l'aire de jeu est déclarée conforme.
     * Si false => règlement officiel impossible.
     */
    private final boolean compliant;

    private PlayingAreaSpec(
            PlayingAreaPreset preset,
            String infoText,
            Integer lengthMeters,
            Integer widthMeters,
            boolean compliant) {

        this.preset = Objects.requireNonNull(preset, "preset obligatoire");
        this.infoText = normalize(infoText);

        this.lengthMeters = lengthMeters;
        this.widthMeters = widthMeters;

        this.compliant = compliant;

        validateSelf();
    }

    // -------------------------------------------------------------------------
    // FACTORIES
    // -------------------------------------------------------------------------

    public static PlayingAreaSpec standardFor(TournamentLevel level) {
        Objects.requireNonNull(level, "level obligatoire");

        PlayingAreaPreset preset = mapPreset(level);

        String text = switch (preset) {
            case DEPARTEMENTAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration départementale).";
            case REGIONAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration régionale).";
            case NATIONAL_STANDARD ->
                "Aires de jeu conformes à la réglementation FFTT (configuration nationale).";
            case INTERNATIONAL_STANDARD ->
                "Aires de jeu conformes aux standards internationaux (configuration internationale).";
            case CUSTOM ->
                "Aires de jeu : configuration personnalisée (voir détails).";
        };

        return new PlayingAreaSpec(preset, text, null, null, true);
    }

    public static PlayingAreaSpec custom(String infoText, Integer lengthMeters, Integer widthMeters,
            boolean compliant) {
        return new PlayingAreaSpec(PlayingAreaPreset.CUSTOM, infoText, lengthMeters, widthMeters, compliant);
    }

    // -------------------------------------------------------------------------
    // RULES
    // -------------------------------------------------------------------------

    private static PlayingAreaPreset mapPreset(TournamentLevel level) {
        return switch (level) {
            case DEPARTEMENTAL -> PlayingAreaPreset.DEPARTEMENTAL_STANDARD;
            case REGIONAL -> PlayingAreaPreset.REGIONAL_STANDARD;
            case NATIONAL_B, NATIONAL_A -> PlayingAreaPreset.NATIONAL_STANDARD;
            case INTERNATIONAL -> PlayingAreaPreset.INTERNATIONAL_STANDARD;
        };
    }

    private void validateSelf() {
        if (preset == PlayingAreaPreset.CUSTOM) {

            if (isBlank(infoText)) {
                throw new BusinessException(ErrorCode.TOURNAMENT_PLAYING_AREA_CUSTOM_INFO_REQUIRED);
            }

            if ((lengthMeters == null) != (widthMeters == null)) {
                throw new BusinessException(ErrorCode.TOURNAMENT_PLAYING_AREA_DIMENSIONS_INCOMPLETE);
            }

            if (lengthMeters != null && (lengthMeters <= 0 || widthMeters <= 0)) {
                throw new BusinessException(ErrorCode.TOURNAMENT_PLAYING_AREA_DIMENSIONS_INVALID);
            }
        }
    }

    /**
     * Contrôle de cohérence preset <-> niveau tournoi.
     *
     * Règle :
     * - DEPARTEMENTAL : dep/reg/custom OK
     * - REGIONAL : reg/nat/custom OK
     * - NATIONAL(*) : nat/int/custom OK
     * - INTERNATIONAL : int/custom OK
     *
     * Pro :
     * - compliant=false => publication officielle impossible
     */
    public void validateCompatibleWith(TournamentLevel level) {
        Objects.requireNonNull(level, "level obligatoire");

        if (!compliant) {
            throw new BusinessException(ErrorCode.TOURNAMENT_PLAYING_AREA_NOT_COMPLIANT);
        }

        boolean ok = switch (level) {
            case DEPARTEMENTAL ->
                preset == PlayingAreaPreset.DEPARTEMENTAL_STANDARD
                        || preset == PlayingAreaPreset.REGIONAL_STANDARD
                        || preset == PlayingAreaPreset.CUSTOM;

            case REGIONAL ->
                preset == PlayingAreaPreset.REGIONAL_STANDARD
                        || preset == PlayingAreaPreset.NATIONAL_STANDARD
                        || preset == PlayingAreaPreset.CUSTOM;

            case NATIONAL_B, NATIONAL_A ->
                preset == PlayingAreaPreset.NATIONAL_STANDARD
                        || preset == PlayingAreaPreset.INTERNATIONAL_STANDARD
                        || preset == PlayingAreaPreset.CUSTOM;

            case INTERNATIONAL ->
                preset == PlayingAreaPreset.INTERNATIONAL_STANDARD
                        || preset == PlayingAreaPreset.CUSTOM;
        };

        if (!ok) {
            throw new BusinessException(ErrorCode.TOURNAMENT_PLAYING_AREA_INCOMPATIBLE_LEVEL);
        }
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public PlayingAreaPreset preset() {
        return preset;
    }

    public String infoText() {
        return infoText;
    }

    public Integer lengthMeters() {
        return lengthMeters;
    }

    public Integer widthMeters() {
        return widthMeters;
    }

    public boolean compliant() {
        return compliant;
    }

    // -------------------------------------------------------------------------
    // VALUE OBJECT
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PlayingAreaSpec that))
            return false;
        return compliant == that.compliant
                && preset == that.preset
                && Objects.equals(infoText, that.infoText)
                && Objects.equals(lengthMeters, that.lengthMeters)
                && Objects.equals(widthMeters, that.widthMeters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preset, infoText, lengthMeters, widthMeters, compliant);
    }

    @Override
    public String toString() {
        String dims = (lengthMeters == null) ? "" : (" - " + lengthMeters + "m x " + widthMeters + "m");
        String compliantText = compliant ? "conforme" : "non conforme";
        String base = preset + dims + " (" + compliantText + ")";
        return (infoText == null) ? base : base + " - " + infoText;
    }

    // -------------------------------------------------------------------------
    // UTIL
    // -------------------------------------------------------------------------

    private static String normalize(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}