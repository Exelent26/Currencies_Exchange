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
}
