package kd.cd.webapi.okhttp;

import kd.cd.webapi.util.OkHttpUtils;
import lombok.Getter;
import okhttp3.Response;

@Getter
public class BufferedResponse {
    public static final BufferedResponse NONE = new BufferedResponse();
    private int code;
    private String message;
    private String header;
    private boolean success;
    private String body;

    private BufferedResponse() {
    }

    private BufferedResponse(Response response, boolean includeBody) {
        code = response.code();
        header = response.headers().toString();
        success = response.isSuccessful();
        message = response.message();
        body = includeBody ? OkHttpUtils.getBufferedRespBody(response) : null;
    }

    public static BufferedResponse create(Response response, boolean includeBody) {
        if (response == null) {
            return NONE;
        } else {
            return new BufferedResponse(response, includeBody);
        }
    }
}
