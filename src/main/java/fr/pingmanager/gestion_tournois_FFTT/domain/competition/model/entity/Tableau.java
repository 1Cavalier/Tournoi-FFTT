package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.classification.ClassificationMode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TableauPointsRuleType;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value.AgeCategoryPolicy;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value.PrizeDistribution;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value.RegistrationFee;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.AgeCategory;

public final class Tableau {

    // -------------------------------------------------------------------------
    // CONSTANTES
    // -------------------------------------------------------------------------

    /** Taille de poule autorisée : 3 (standard FFTT) ou 4. */
    public static final int POOL_SIZE_STANDARD = 3;
    public static final int POOL_SIZE_LARGE = 4;

    /** Nombre de qualifiés par poule autorisé : 1 ou 2. */
    public static final int QUALIFIED_MIN = 1;
    public static final int QUALIFIED_MAX = 2;

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------

    private final String code;
    private final String designation;
    private final LocalDate date;

    private final GenderPolicy genderPolicy;

    private final TableauPointsRuleType pointsRuleType;
    private final Integer minPoints;
    private final Integer maxPoints;

    private final AgeCategoryPolicy ageCategoryPolicy; // null => ANY

    private final int maxPlayers;

    /** 0 = pas de file d'attente ; >0 capacité max waitlist */
    private final int waitlistCapacity;

    private final RegistrationFee fee;

    /** Fin de pointage */
    private final LocalTime checkInEnd;

    /** Début du tableau */
    private final LocalTime startTime;

    private final PrizeDistribution prizes;

    /**
     * Taille des poules : 3 (standard FFTT) ou 4.
     * Défaut : 3.
     * Note : l'algorithme de tirage est défini au niveau du tournoi.
     */
    private final int poolSize;

    /**
     * Nombre de joueurs qualifiés par poule pour le tableau KO.
     * 1 = seul le 1er qualifié, 2 = 1er et 2ème qualifiés.
     * Défaut : 2.
     */
    private final int qualifiedPerPool;

    /**
     * Mode de classement final.
     * Défaut : NONE (aucun match de classement).
     */
    private final ClassificationMode classificationMode;

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------

    public Tableau(
            String code,
            String designation,
            LocalDate date,
            GenderPolicy genderPolicy,
            AgeCategoryPolicy ageCategoryPolicy,
            TableauPointsRuleType pointsRuleType,
            Integer minPoints,
            Integer maxPoints,
            int maxPlayers,
            int waitlistCapacity,
            RegistrationFee fee,
            LocalTime checkInEnd,
            LocalTime startTime,
            PrizeDistribution prizes,
            Integer poolSize,
            Integer qualifiedPerPool,
            ClassificationMode classificationMode) {

        // ---- identifiants / description ----
        this.code = requireText(code, ErrorCode.TABLEAU_CODE_REQUIRED).toUpperCase(Locale.ROOT);
        this.designation = requireText(designation, ErrorCode.TABLEAU_DESIGNATION_REQUIRED);
        this.date = requireNonNull(date, ErrorCode.TABLEAU_DATE_REQUIRED);

        // ---- règles ----
        this.genderPolicy = requireNonNull(genderPolicy, ErrorCode.TABLEAU_GENDER_POLICY_REQUIRED);
        this.pointsRuleType = requireNonNull(pointsRuleType, ErrorCode.TABLEAU_POINTS_RULE_TYPE_REQUIRED);
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.ageCategoryPolicy = ageCategoryPolicy; // null => pas de restriction

        // ---- capacités ----
        if (maxPlayers <= 0)
            throw new BusinessException(ErrorCode.TABLEAU_MAX_PLAYERS_INVALID);
        this.maxPlayers = maxPlayers;

        if (waitlistCapacity < 0)
            throw new BusinessException(ErrorCode.TABLEAU_WAITLIST_CAPACITY_INVALID);
        this.waitlistCapacity = waitlistCapacity;

        // ---- frais / horaires / dotations ----
        this.fee = requireNonNull(fee, ErrorCode.TABLEAU_FEE_REQUIRED);
        this.checkInEnd = requireNonNull(checkInEnd, ErrorCode.TABLEAU_CHECKIN_TIME_REQUIRED);
        this.startTime = requireNonNull(startTime, ErrorCode.TABLEAU_START_TIME_REQUIRED);
        this.prizes = requireNonNull(prizes, ErrorCode.TABLEAU_PRIZE_REQUIRED);

        // ---- poules ----
        int ps = (poolSize != null) ? poolSize : POOL_SIZE_STANDARD;
        if (ps != POOL_SIZE_STANDARD && ps != POOL_SIZE_LARGE) {
            throw new BusinessException(ErrorCode.TABLEAU_POOL_SIZE_INVALID);
        }
        this.poolSize = ps;

        int qp = (qualifiedPerPool != null) ? qualifiedPerPool : QUALIFIED_MAX;
        if (qp < QUALIFIED_MIN || qp > QUALIFIED_MAX) {
            throw new BusinessException(ErrorCode.TABLEAU_QUALIFIED_PER_POOL_INVALID);
        }
        // Un seul qualifié n'est logique que pour une poule d'au moins 2
        this.qualifiedPerPool = qp;

        // ---- classement ----
        this.classificationMode = (classificationMode != null) ? classificationMode : ClassificationMode.NONE;

        // ---- validations cross-field ----
        validatePointsRule();
        validateTimes();
    }

    // -------------------------------------------------------------------------
    // HELPERS MÉTIER
    // -------------------------------------------------------------------------

    /**
     * Calcule le nombre maximum suggéré de joueurs pour ce tableau
     * selon le nombre de tables disponibles.
     *
     * Formule : nbTables × poolSize
     * Exemple : 8 tables × 3 joueurs/poule = 24 joueurs max suggérés
     *
     * @param numberOfTables nombre de tables disponibles dans la salle
     * @return nombre de joueurs suggéré, ou -1 si numberOfTables invalide
     */
    public int suggestedMaxPlayers(int numberOfTables) {
        if (numberOfTables <= 0)
            return -1;
        return numberOfTables * poolSize;
    }

    /**
     * Label lisible décrivant la formule de poule de ce tableau.
     * Ex : "Poules de 3 — 2 qualifiés"
     */
    public String poolFormulaLabel() {
        return "Poules de " + poolSize + " — "
                + qualifiedPerPool + " qualifié" + (qualifiedPerPool > 1 ? "s" : "");
    }

    // -------------------------------------------------------------------------
    // VALIDATIONS
    // -------------------------------------------------------------------------

    private void validatePointsRule() {
        switch (pointsRuleType) {
            case TOUTES_SERIES -> {
                if (minPoints != null || maxPoints != null)
                    throw new BusinessException(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT);
            }
            case MAX_ONLY -> {
                if (maxPoints == null)
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_REQUIRED);
                if (maxPoints < 0)
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_NEGATIVE);
                if (minPoints != null)
                    throw new BusinessException(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT);
            }
            case RANGE_MIN_MAX -> {
                if (minPoints == null)
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_POINTS_REQUIRED);
                if (maxPoints == null)
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_REQUIRED);
                if (minPoints < 0)
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_POINTS_NEGATIVE);
                if (maxPoints < 0)
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_NEGATIVE);
                if (minPoints > maxPoints)
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_GREATER_THAN_MAX);
            }
        }
    }

    private void validateTimes() {
        if (!checkInEnd.isBefore(startTime))
            throw new BusinessException(ErrorCode.TABLEAU_CHECKIN_AFTER_START);
    }

    // -------------------------------------------------------------------------
    // ELIGIBILITY
    // -------------------------------------------------------------------------

    public boolean accepts(int playerPoints, boolean isFemale) {
        return accepts(playerPoints, isFemale, null);
    }

    public boolean accepts(int playerPoints, boolean isFemale, AgeCategory ageCategory) {
        if (playerPoints < 0)
            return false;
        if (genderPolicy == GenderPolicy.FEMININ && !isFemale)
            return false;
        if (ageCategoryPolicy != null) {
            if (ageCategory == null)
                return false;
            if (!ageCategoryPolicy.accepts(ageCategory))
                return false;
        }
        return switch (pointsRuleType) {
            case TOUTES_SERIES -> true;
            case MAX_ONLY -> playerPoints <= maxPoints;
            case RANGE_MIN_MAX -> playerPoints >= minPoints && playerPoints <= maxPoints;
        };
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String code() {
        return code;
    }

    public String designation() {
        return designation;
    }

    public LocalDate date() {
        return date;
    }

    public GenderPolicy genderPolicy() {
        return genderPolicy;
    }

    public TableauPointsRuleType pointsRuleType() {
        return pointsRuleType;
    }

    public Integer minPoints() {
        return minPoints;
    }

    public Integer maxPoints() {
        return maxPoints;
    }

    public AgeCategoryPolicy ageCategoryPolicy() {
        return ageCategoryPolicy;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

    public int waitlistCapacity() {
        return waitlistCapacity;
    }

    public RegistrationFee fee() {
        return fee;
    }

    public LocalTime checkInEnd() {
        return checkInEnd;
    }

    public LocalTime startTime() {
        return startTime;
    }

    public PrizeDistribution prizes() {
        return prizes;
    }

    public int poolSize() {
        return poolSize;
    }

    public int qualifiedPerPool() {
        return qualifiedPerPool;
    }

    public ClassificationMode classificationMode() {
        return classificationMode;
    }

    // -------------------------------------------------------------------------
    // HELPERS PRIVÉS
    // -------------------------------------------------------------------------

    private static String requireText(String value, ErrorCode error) {
        if (value == null || value.isBlank())
            throw new BusinessException(error);
        return value.trim();
    }

    private static <T> T requireNonNull(T value, ErrorCode error) {
        if (Objects.isNull(value))
            throw new BusinessException(error);
        return value;
    }
}