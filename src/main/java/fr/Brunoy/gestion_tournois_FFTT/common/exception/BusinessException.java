package fr.Brunoy.gestion_tournois_FFTT.common.exception;

import java.util.Objects;

public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(ErrorCode code) {
        super(Objects.requireNonNull(code, "ErrorCode required").name());
        this.code = code;
    }

    public BusinessException(ErrorCode code, Throwable cause) {
        super(Objects.requireNonNull(code, "ErrorCode required").name(), cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "BusinessException{code=" + code + "}";
    }
}