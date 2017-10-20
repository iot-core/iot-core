package org.iotbricks.hono.transport;

public class HonoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int statusCode;

    public HonoException(final int code) {
        super();
        this.statusCode = code;
    }

    public HonoException(final int code, final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = code;
    }

    public HonoException(final int code, final String message) {
        super(message);
        this.statusCode = code;
    }

    public HonoException(final int code, final Throwable cause) {
        super(cause);
        this.statusCode = code;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getLocalizedMessage() {
        return String.format("%s: %s", this.statusCode, getMessage());
    }

}
