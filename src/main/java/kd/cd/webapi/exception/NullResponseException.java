package kd.cd.webapi.exception;

public class NullResponseException extends IllegalResponseException {

    public NullResponseException() {
        super("Response is null");
    }
}
