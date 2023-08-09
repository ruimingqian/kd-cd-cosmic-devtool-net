package kd.cd.webapi;

/**
 * 响应失败异常
 *
 * @author qrm
 * @version 1.0
 * @see RuntimeException
 */
public class FailedResponseException extends RuntimeException {
    public FailedResponseException() {
        super();
    }

    public FailedResponseException(String message) {
        super(message);
    }

    public FailedResponseException(Throwable cause) {
        super(cause);
    }

    public FailedResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
