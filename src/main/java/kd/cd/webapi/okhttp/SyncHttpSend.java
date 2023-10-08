package kd.cd.webapi.okhttp;

import kd.cd.webapi.req.BaseRequest;

import java.io.IOException;

public interface SyncHttpSend<T> {
    T sendRequest(BaseRequest baseRequest) throws IOException;
}
