package kd.cd.webapi.apachehttp;

import kd.cd.webapi.core.RespHandle;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class HttpSyncSender extends AbstractHttpSyncSender<RespHandle<CloseableHttpResponse>> {

    public HttpSyncSender() {
    }

    public HttpSyncSender(CloseableHttpClient client) {
        this.client = client;
    }

    public RespHandle<CloseableHttpResponse> post(String url, String reqString, Map<String, String> headerMap) throws IOException {
        return sendRaw(Method.POST, ContentType.APPLICATION_JSON, url, reqString, headerMap, null);
    }

    public RespHandle<CloseableHttpResponse> get(String url, String reqString, Map<String, String> headerMap) throws IOException {
        return sendRaw(Method.GET, ContentType.APPLICATION_JSON, url, reqString, headerMap, null);
    }

    public RespHandle<CloseableHttpResponse> urlencodedPost(String url, Map<String, String> reqMap, Map<String, String> headerMap) throws IOException {
        return sendUrlencoded(Method.POST, url, reqMap, headerMap, null);
    }

    @Override
    Function<CloseableHttpResponse, RespHandle<CloseableHttpResponse>> respHandelFunction() {
        return RespHandler::new;
    }

    @Override
    HttpClientBuilder defaultBuilder() {
        return HttpUtils.newDefaultBulider();
    }

    public void close() {
        try {
            if (this.client != null) {
                client.close();
            }
        } catch (Exception e) {
            //ignored
        } finally {
            this.client = null;
        }
    }
}
