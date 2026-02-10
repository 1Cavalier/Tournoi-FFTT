package fr.Brunoy.gestion_tournois_FFTT.common.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(ErrorCode code) {
        super(); // pas de message ici
        this.code = code;
    }

    public BusinessException(ErrorCode code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
