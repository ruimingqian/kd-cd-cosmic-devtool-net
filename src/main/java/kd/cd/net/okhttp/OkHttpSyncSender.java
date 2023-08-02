package kd.cd.net.okhttp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.net.ContentType;
import kd.cd.net.Method;
import kd.cd.net.NullResponseException;
import kd.cd.net.RespHandle;
import kd.cd.net.client.OkHttpClientUtils;
import kd.cd.net.log.LogParam;
import kd.cd.net.utils.OkHttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        return OkHttpClientUtils.getDefaultBulider();
    }

    static class RespHandler implements RespHandle<Response> {
        private final Response resp;

        public RespHandler(Response response) {
            this.resp = response;
        }

        public Response response() {
            return resp;
        }

        public ObjectNode bodyToJson() throws IOException, NullResponseException {
            return OkHttpUtils.bodyToJson(resp);
        }

        public byte[] bodyToBytes() throws IOException, NullResponseException {
            return OkHttpUtils.bodyToBytes(resp);
        }

        public InputStream bodyToByteStream() throws NullResponseException {
            return OkHttpUtils.bodyToByteStream(resp);
        }

        public void writeTo(OutputStream outputStream) throws IOException, NullResponseException {
            InputStream inputStream = bodyToByteStream();
            IOUtils.copy(inputStream, outputStream);
        }

        public String bodyToString() throws IOException, NullResponseException {
            return OkHttpUtils.bodyToString(resp);
        }

        public boolean isSuccessful() {
            return resp.isSuccessful();
        }
    }
}
