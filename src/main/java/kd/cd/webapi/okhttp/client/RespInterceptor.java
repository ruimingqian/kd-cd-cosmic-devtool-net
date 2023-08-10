package kd.cd.webapi.okhttp.client;

import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.okhttp.OkHttpUtils;
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
        LogParam logParam = tracker.getLogParam();
        if (logParam == null) {
            return chain.proceed(req);
        }

        Response resp = null;
        try {
            resp = chain.proceed(req);

        } catch (Exception e) {
            logParam.setException(e);
            throw e;

        } finally {
            logParam.setReqInfo(OkHttpUtils.requestToJson(req, logParam.isRecordFullRequest()));
            logParam.setRespInfo(OkHttpUtils.responseToJson(resp, logParam.isRecordFullResponse()));
        }

        return resp;
    }
}