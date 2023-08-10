package kd.cd.webapi.okhttp;

import kd.cd.webapi.ContentType;
import kd.cd.webapi.Method;
import kd.cd.webapi.log.LogParam;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractOkHttpSyncSender<T> implements OkHttpSyncSend<T> {
    protected OkHttpClient client;

    @Override
    public T sendRaw(Method method, ContentType contentType, String url, String reqString, Map<String, String> headerMap, LogParam logParam) throws IOException {
        Request req = OkHttpRequestFactory.newRawRequest(method, contentType, url, reqString, headerMap, logParam);
        return execSync(req, respHandelFunction());
    }

    @Override
    public T sendUrlencoded(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogParam logParam) throws IOException {
        Request req = OkHttpRequestFactory.newUrlencodedRequest(method, url, reqMap, headerMap, logParam);
        return execSync(req, respHandelFunction());
    }

    @Override
    public T sendFormData(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogParam logParam) throws IOException {
        Request req = OkHttpRequestFactory.newFormDataRequest(method, url, reqMap, headerMap, logParam);
        return execSync(req, respHandelFunction());
    }

    abstract Function<Response, T> respHandelFunction();

    T execSync(Request req, Function<Response, T> function) throws IOException {
        if (client == null) {
            client = defaultBuilder().build();
        }
        Call call = client.newCall(req);
        Response resp = call.execute();

        return function.apply(resp);
    }

    abstract OkHttpClient.Builder defaultBuilder();
}
