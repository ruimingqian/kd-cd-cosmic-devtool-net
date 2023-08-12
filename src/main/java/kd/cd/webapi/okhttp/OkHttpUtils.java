package kd.cd.webapi.okhttp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.cd.webapi.exception.FailedResponseException;
import kd.cd.webapi.exception.NullResponseException;
import kd.cd.webapi.okhttp.client.RespInterceptor;
import kd.cd.webapi.okhttp.client.TrackEventListenerFactory;
import kd.cd.webapi.ssl.MyX509TrustManager;
import kd.cd.webapi.ssl.SSLUtils;
import kd.cd.webapi.ssl.TrustAllHostnameVerifier;
import kd.cd.webapi.util.JacksonUtils;
import kd.cd.webapi.util.SystemPropertyUtils;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp通用
 *
 * @author qrm
 * @version 1.0
 */
public final class OkHttpUtils {
    private static final Log log = LogFactory.getLog(OkHttpUtils.class);
    private static final long CONNECT_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.connecttimeoutseconds", 10L);
    private static final long READ_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.readtimeoutseconds", 60L);
    private static final long WRITE_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.writetimeoutseconds", 60L);
    private static final boolean IGNORE_SSL_CHECK = SystemPropertyUtils.getBoolean("okhttpclient.default.ignoressl", true);
    private static final boolean ADD_LOG_MONITOR = SystemPropertyUtils.getBoolean("okhttpclient.default.addlogmonitor", true);

    private OkHttpUtils() {
    }

    public static OkHttpClient.Builder newCustomizedBuilder() {
        return newBuilder(IGNORE_SSL_CHECK, ADD_LOG_MONITOR);
    }

    public static OkHttpClient.Builder newBuilder(boolean ignoreSSL, boolean addLogMonitor) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        builder.connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (ignoreSSL) {
            builder.sslSocketFactory(SSLUtils.newSSLSocketFactory(), new MyX509TrustManager());
            builder.hostnameVerifier(new TrustAllHostnameVerifier());
        }

        if (addLogMonitor) {
            builder.addInterceptor(new RespInterceptor());
            builder.eventListenerFactory(new TrackEventListenerFactory());
        }
        return builder;
    }

    public static ObjectNode respBodyToJson(Response resp) throws IOException {
        String bodyString = respBodyToString(resp);
        return StringUtils.isEmpty(bodyString) ?
                null :
                (ObjectNode) JacksonUtils.getObjectMapper().readTree(bodyString);
    }

    public static String respBodyToString(Response resp) throws IOException {
        checkResp(resp);
        ResponseBody body = resp.body();
        return body == null ? null : body.string();
    }

    public static byte[] respBodyToBytes(Response resp) throws IOException {
        checkResp(resp);
        ResponseBody body = resp.body();
        return body == null ? null : body.bytes();
    }

    public static InputStream respBodyToInputStream(Response resp) {
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

    public static ObjectNode fullReqToJson(Request req, boolean includeBody) {
        return Optional.ofNullable(req).map(r -> {
                    ObjectNode json = JacksonUtils.getObjectMapper().createObjectNode();
                    json.put("url", r.url().toString());
                    json.put("method", r.method());
                    json.put("headers", r.headers().toString());
                    json.put("body", includeBody ? getBufferedReqBodyString(req) : null);
                    return json;
                }
        ).orElse(null);
    }

    public static ObjectNode fullRespToJson(Response resp, boolean includeBody) {
        return Optional.ofNullable(resp).map(r -> {
                    ObjectNode json = JacksonUtils.getObjectMapper().createObjectNode();
                    json.put("success", resp.isSuccessful());
                    json.put("headers", resp.headers().toString());
                    json.put("message", resp.message());
                    json.put("body", includeBody ? getBufferedRespBodyString(resp) : null);
                    return json;
                }
        ).orElse(null);
    }

    private static String getBufferedReqBodyString(Request req) {
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

    private static String getBufferedRespBodyString(Response resp) {
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