package kd.cd.webapi.exception;

public class IllegalResponseException extends Exception {
    public IllegalResponseException() {
        super();
    }

    public IllegalResponseException(String message) {
        super(message);
    }

    public IllegalResponseException(Throwable cause) {
        super(cause);
    }

    public IllegalResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
