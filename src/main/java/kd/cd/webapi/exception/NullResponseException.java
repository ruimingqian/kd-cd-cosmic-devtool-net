package kd.cd.webapi.exception;

/**
 * 响应为空异常
 *
 * @author qrm
 * @version 1.0
 * @see RuntimeException
 */
public class NullResponseException extends IllegalResponseException {
    public NullResponseException() {
        super();
    }

    public NullResponseException(String message) {
        super(message);
    }

    public NullResponseException(Throwable cause) {
        super(cause);
    }

    public NullResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
