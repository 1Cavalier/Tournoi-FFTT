package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.AgeCategory;

public final class AgeCategoryPolicy {

    public enum Type {
        ANY,
        ALLOWED_SET,
        RANGE
    }

    private final Type type;

    private final Set<AgeCategory> allowed; // EnumSet immuable côté VO
    private final AgeCategory min;
    private final AgeCategory max;

    private AgeCategoryPolicy(Type type, Set<AgeCategory> allowed, AgeCategory min, AgeCategory max) {
        this.type = Objects.requireNonNull(type, "type");

        if (allowed != null && allowed.contains(null)) {
            throw new BusinessException(ErrorCode.TABLEAU_AGE_POLICY_INVALID);
        }

        // Copie défensive (si non null)
        this.allowed = (allowed == null) ? null : EnumSet.copyOf(allowed);

        this.min = min;
        this.max = max;

        validate();
    }

    // ---------------- FACTORIES ----------------

    public static AgeCategoryPolicy any() {
        return new AgeCategoryPolicy(Type.ANY, null, null, null);
    }

    public static AgeCategoryPolicy allowed(Set<AgeCategory> allowed) {
        if (allowed == null || allowed.isEmpty()) {
            throw new BusinessException(ErrorCode.TABLEAU_AGE_POLICY_INVALID);
        }
        // on laisse le constructeur faire la copie défensive en EnumSet
        return new AgeCategoryPolicy(Type.ALLOWED_SET, allowed, null, null);
    }

    public static AgeCategoryPolicy range(AgeCategory min, AgeCategory max) {
        return new AgeCategoryPolicy(Type.RANGE, null, min, max);
    }

    // ---------------- LOGIC ----------------

    public boolean accepts(AgeCategory category) {
        if (category == null)
            return false;

        return switch (type) {
            case ANY -> true;
            case ALLOWED_SET -> allowed.contains(category);
            case RANGE -> inRange(category, min, max);
        };
    }

    private static boolean inRange(AgeCategory c, AgeCategory min, AgeCategory max) {
        return c.ordinal() >= min.ordinal() && c.ordinal() <= max.ordinal();
    }

    private void validate() {
        switch (type) {
            case ANY -> {
                // ok
            }
            case ALLOWED_SET -> {
                if (allowed == null || allowed.isEmpty()) {
                    throw new BusinessException(ErrorCode.TABLEAU_AGE_POLICY_INVALID);
                }
            }
            case RANGE -> {
                if (min == null || max == null) {
                    throw new BusinessException(ErrorCode.TABLEAU_AGE_POLICY_INVALID);
                }
                if (min.ordinal() > max.ordinal()) {
                    throw new BusinessException(ErrorCode.TABLEAU_AGE_POLICY_INVALID);
                }
            }
        }
    }

    // ---------------- GETTERS ----------------

    public Type type() {
        return type;
    }

    public Set<AgeCategory> allowed() {
        return allowed == null ? null : Set.copyOf(allowed);
    }

    public AgeCategory min() {
        return min;
    }

    public AgeCategory max() {
        return max;
    }
}