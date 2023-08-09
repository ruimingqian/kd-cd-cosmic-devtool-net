package kd.cd.webapi.okhttp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.cd.webapi.FailedResponseException;
import kd.cd.webapi.NullResponseException;
import kd.cd.webapi.utils.JacksonUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * OkHttp请求通用
 *
 * @author qrm
 * @version 1.0
 */
public final class OkHttpUtils {
    private static final Log log = LogFactory.getLog(OkHttpUtils.class);

    private OkHttpUtils() {
    }

    public static ObjectNode bodyToJson(Response resp) throws IOException {
        String bodyString = bodyToString(resp);
        return StringUtils.isEmpty(bodyString) ? null : (ObjectNode) JacksonUtils.getObjectMapper().readTree(bodyString);
    }

    public static String bodyToString(Response resp) throws IOException {
        checkResp(resp);
        ResponseBody body = resp.body();
        return body == null ? null : body.string();
    }

    public static byte[] bodyToBytes(Response resp) throws IOException {
        checkResp(resp);
        ResponseBody body = resp.body();
        return body == null ? null : body.bytes();
    }

    public static InputStream bodyToInputStream(Response resp) {
        checkResp(resp);
        ResponseBody body = resp.body();
        return body == null ? null : body.byteStream();
    }

    public static void checkResp(Response resp) {
        if (resp == null) {
            throw new NullResponseException("Okhttp response is null");
        }
        if (!resp.isSuccessful()) {
            throw new FailedResponseException(String.format("Okhttp request fail: %s", resp.message()));
        }
    }

    public static ObjectNode requestToJson(Request req, boolean includeBody) {
        return Optional.ofNullable(req).map(r -> {
                    ObjectNode json = JacksonUtils.getObjectMapper().createObjectNode();
                    json.put("url", r.url().toString());
                    json.put("method", r.method());
                    json.put("headers", r.headers().toString());
                    json.put("body", includeBody ? getReqBodyString(req) : null);
                    return json;
                }
        ).orElse(null);
    }

    public static ObjectNode responseToJson(Response resp, boolean includeBody) {
        return Optional.ofNullable(resp).map(r -> {
                    ObjectNode json = JacksonUtils.getObjectMapper().createObjectNode();
                    json.put("success", resp.isSuccessful());
                    json.put("headers", resp.headers().toString());
                    json.put("message", resp.message());
                    json.put("body", includeBody ? cloneRespBodyString(resp) : null);
                    return json;
                }
        ).orElse(null);
    }

    private static String getReqBodyString(Request req) {
        try (Buffer buffer = new Buffer()) {
            RequestBody body = req.body();
            if (body != null) {
                body.writeTo(buffer);
                return buffer.readUtf8();
            }

        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Buffer resp failed", e);
            }
        }
        return null;
    }

    private static String cloneRespBodyString(Response resp) {
        try (Buffer buffer = getRespBuffer(resp)) {
            return buffer.clone().readUtf8();

        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Clone resp failed", e);
            }
        }
        return null;
    }

    private static Buffer getRespBuffer(Response resp) throws IOException {
        ResponseBody respBody = resp.body();
        BufferedSource source = Objects.requireNonNull(respBody).source();
        source.request(Long.MAX_VALUE);
        return source.getBuffer();
    }
}