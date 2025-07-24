package exception;

public class ServiceException extends RuntimeException {
    public enum ErrorCode {
        VALIDATION_ERROR,
        DUPLICATE_ENTITY,
        NOT_FOUND,
        DATABASE_ERROR,
        UNKNOWN,
        DAO_ERROR
    }

    private final ErrorCode errorCode;

    public ServiceException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatusCode() {
        return switch (errorCode) {
            case VALIDATION_ERROR -> 400;
            case DUPLICATE_ENTITY -> 409;
            case NOT_FOUND -> 404;
            case DAO_ERROR, DATABASE_ERROR -> 500;
            default -> 500;
        };
    }
}
