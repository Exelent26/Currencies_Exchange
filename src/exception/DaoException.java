package exception;

public class DaoException extends RuntimeException {

    public enum ErrorCode {
        DUPLICATE_CURRENCY,
        CURRENCY_NOT_FOUND,
        DATABASE_ERROR,
        DUPLICATE_EXCHANGE_RATE,
        INVALID_INPUT,
        EXCHANGE_RATE_NOT_FOUND,
        DB_CONSTRAINT_ERROR,
        }

    private ErrorCode errorCode;

    public DaoException(String message) {
        super(message);
    }

    public DaoException(Throwable throwable) {
        super(throwable);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DaoException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
