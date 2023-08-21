package kd.cd.webapi.exception;

import lombok.Getter;
import okhttp3.Response;

@Getter
public class FailedResponseException extends IllegalResponseException {
    protected final long httpCode;
    protected final String respMsg;

    public FailedResponseException(Response resp) {
        super(resp.toString());
        this.httpCode = resp.code();
        this.respMsg = resp.message();
    }
}
