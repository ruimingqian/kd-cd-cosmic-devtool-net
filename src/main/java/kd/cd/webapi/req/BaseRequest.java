package kd.cd.webapi.req;

import okhttp3.Request;

import java.io.IOException;

public interface BaseRequest {
    Request convert() throws IOException;
}
