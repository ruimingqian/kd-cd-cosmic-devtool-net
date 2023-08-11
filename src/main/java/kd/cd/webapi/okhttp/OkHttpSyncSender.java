package kd.cd.webapi.okhttp;

import kd.cd.webapi.core.RespHandle;
import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class OkHttpSyncSender extends AbstractOkHttpSyncSender<RespHandle<Response>> {
    public OkHttpSyncSender() {
    }

    public OkHttpSyncSender(OkHttpClient client) {
        this.client = client;
    }

    public RespHandle<Response> post(String url, String reqString, Map<String, String> headerMap, LogParam logParam) throws IOException {
        return sendRaw(Method.POST, ContentType.APPLICATION_JSON, url, reqString, headerMap, logParam);
    }

    public RespHandle<Response> get(String url, String reqString, Map<String, String> headerMap, LogParam logParam) throws IOException {
        return sendRaw(Method.GET, ContentType.APPLICATION_JSON, url, reqString, headerMap, logParam);
    }

    public RespHandle<Response> urlencodedPost(String url, Map<String, String> reqMap, Map<String, String> headerMap, LogParam logParam) throws IOException {
        return sendUrlencoded(Method.POST, url, reqMap, headerMap, logParam);
    }

    public RespHandle<Response> urlencodedGet(String url, Map<String, String> reqMap, Map<String, String> headerMap, LogParam logParam) throws IOException {
        return sendUrlencoded(Method.GET, url, reqMap, headerMap, logParam);
    }

    @Override
    Function<Response, RespHandle<Response>> respHandelFunction() {
        return RespHandler::new;
    }

    @Override
    OkHttpClient.Builder defaultBuilder() {
        return OkHttpUtils.newDefaultBulider();
    }
}
