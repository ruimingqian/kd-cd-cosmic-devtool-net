package kd.cd.webapi.okhttp;

import kd.cd.webapi.util.OkHttpUtils;
import lombok.Getter;
import okhttp3.Request;

@Getter
public class BufferedRequest {
    public static final BufferedRequest NONE = new BufferedRequest();
    private String url;
    private String method;
    private String headers;
    private String body;

    private BufferedRequest() {
    }

    private BufferedRequest(Request request, boolean includeBody) {
        url = request.url().toString();
        method = request.method();
        headers = request.headers().toString();
        body = includeBody ? OkHttpUtils.getBufferedReqBody(request) : null;
    }

    public static BufferedRequest create(Request request, boolean includeBody) {
        if (request == null) {
            return NONE;
        } else {
            return new BufferedRequest(request, includeBody);
        }
    }
}