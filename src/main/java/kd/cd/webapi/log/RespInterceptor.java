package kd.cd.webapi.log;

import kd.cd.webapi.okhttp.BufferedRequest;
import kd.cd.webapi.okhttp.BufferedResponse;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RespInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request req = chain.request();
        EventTracker tracker = req.tag(EventTracker.class);

        if (tracker == null) {
            return chain.proceed(req);
        }
        LogOption logOption = tracker.getLogOption();
        if (logOption == null) {
            return chain.proceed(req);
        }

        Response resp = null;
        try {
            resp = chain.proceed(req);

        } catch (Exception e) {
            logOption.exception = e;
            throw e;

        } finally {
            logOption.bufferedReq = BufferedRequest.create(req, logOption.recordFullRequest);
            logOption.bufferedResp = BufferedResponse.create(resp, logOption.recordFullResponse);
        }

        return resp;
    }
}