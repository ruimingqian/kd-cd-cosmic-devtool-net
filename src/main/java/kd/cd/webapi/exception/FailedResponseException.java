package kd.cd.webapi.exception;

import lombok.Getter;

@Getter
public class FailedResponseException extends IllegalResponseException {

    public FailedResponseException(String message) {
        super(message);
    }
}
