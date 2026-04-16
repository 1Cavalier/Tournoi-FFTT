package fr.pingmanager.gestion_tournois_FFTT.common.exception;

import java.util.Objects;

public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(ErrorCode code) {
        super(); // pas de message ici
        this.code = Objects.requireNonNull(code, "code");
    }

    public BusinessException(ErrorCode code, Throwable cause) {
        super(cause);
        this.code = Objects.requireNonNull(code, "code");
    }

    public ErrorCode getCode() {
        return code;
    }
}