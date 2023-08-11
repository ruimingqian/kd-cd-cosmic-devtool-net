package kd.cd.webapi.apachehttp;

import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractHttpSyncSender<T> implements HttpSyncSend<T> {
    protected CloseableHttpClient client;

    @Override
    public T sendRaw(Method method, ContentType contentType, String url, String reqString, Map<String, String> headerMap, LogParam logParam) throws IOException {
        HttpRequestBase request = HttpRequestFactory.newRawRequest(method, contentType, url, reqString, headerMap);
        return execSync(request, respHandelFunction());
    }

    @Override
    public T sendUrlencoded(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogParam logParam) throws IOException {
        HttpRequestBase request = HttpRequestFactory.newUrlencodedRequest(method, url, reqMap, headerMap);
        return execSync(request, respHandelFunction());
    }

    public T execSync(HttpRequestBase req, Function<CloseableHttpResponse, T> function) throws IOException {

        try {
            if (client == null) {
                client = defaultBuilder().build();
            }
            CloseableHttpResponse resp = client.execute(req);

            return function.apply(resp);

        } finally {
            req.releaseConnection();
        }
    }

    abstract Function<CloseableHttpResponse, T> respHandelFunction();

    abstract HttpClientBuilder defaultBuilder();

    abstract void close();
}
