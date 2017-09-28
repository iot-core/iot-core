package iot.core.utils.binding;

public class RequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ErrorCondition condition;

    public RequestException(final ErrorCondition condition) {
        super();
        this.condition = condition;
    }

    public RequestException(final ErrorCondition condition, final String message, final Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.condition = condition;
    }

    public RequestException(final ErrorCondition condition, final String message, final Throwable cause) {
        super(message, cause);
        this.condition = condition;
    }

    public RequestException(final ErrorCondition condition, final String message) {
        super(message);
        this.condition = condition;
    }

    public RequestException(final ErrorCondition condition, final Throwable cause) {
        super(cause);
        this.condition = condition;
    }

    public ErrorCondition getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return String.format("%s: [%s] %s", getClass().getName(), condition, getLocalizedMessage());
    }
}
