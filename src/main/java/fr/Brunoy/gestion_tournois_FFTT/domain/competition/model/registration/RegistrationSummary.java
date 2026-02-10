package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration;

import java.util.Collections;
import java.util.List;

public final class RegistrationSummary {

    private final List<RegistrationViolation> violations;

    public RegistrationSummary(List<RegistrationViolation> violations) {
        this.violations = List.copyOf(violations);
    }

    public boolean isValid() {
        return violations.isEmpty();
    }

    public List<RegistrationViolation> violations() {
        return Collections.unmodifiableList(violations);
    }
}
