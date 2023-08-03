package kd.cd.net.apachehttp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.net.ContentType;
import kd.cd.net.Method;
import kd.cd.net.RespHandle;
import kd.cd.net.client.ApacheHttpClientUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Function;

public class ApacheHttpSyncSender extends AbstractApacheHttpSyncSender<RespHandle<CloseableHttpResponse>> {

    public ApacheHttpSyncSender() {
    }

    public ApacheHttpSyncSender(CloseableHttpClient client) {
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
        return ApacheHttpClientUtils.getDefaultBulider();
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

    static class RespHandler implements RespHandle<CloseableHttpResponse> {
        private final CloseableHttpResponse resp;

        public RespHandler(CloseableHttpResponse response) {
            this.resp = response;
        }

        @Override
        public CloseableHttpResponse response() {
            return resp;
        }

        @Override
        public String bodyToString() throws IOException {
            return ApacheHttpUtils.bodyToString(resp);
        }

        @Override
        public ObjectNode bodyToJson() throws IOException {
            return ApacheHttpUtils.bodyToJson(resp);
        }

        @Override
        public byte[] bodyToBytes() throws IOException {
            return ApacheHttpUtils.bodyToBytes(resp);
        }

        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            InputStream inputStream = bodyToInputStream();
            IOUtils.copy(inputStream, outputStream);
        }

        @Override
        public InputStream bodyToInputStream() throws IOException {
            return ApacheHttpUtils.bodyToStream(resp);
        }

        @Override
        public boolean isSuccessful() {
            StatusLine status = resp.getStatusLine();
            return status.getStatusCode() == HttpStatus.SC_OK;
        }
    }
}
