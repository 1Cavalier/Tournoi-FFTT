package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.registration;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

public final class RegistrationViolation {

        private final ErrorCode code;
        private final String context; // optionnel

        public RegistrationViolation(ErrorCode code) {
                this(code, null);
        }

        public RegistrationViolation(ErrorCode code, String context) {
                this.code = code;
                this.context = context;
        }

        public ErrorCode getCode() {
                return code;
        }

        public String getContext() {
                return context;
        }
}
