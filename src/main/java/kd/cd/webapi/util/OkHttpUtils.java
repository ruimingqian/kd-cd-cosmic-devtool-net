package kd.cd.webapi.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.cd.webapi.exception.FailedResponseException;
import kd.cd.webapi.exception.NullResponseException;
import kd.cd.webapi.log.DruationListenerFactory;
import kd.cd.webapi.log.RespInterceptor;
import kd.cd.webapi.ssl.MyX509TrustManager;
import kd.cd.webapi.ssl.SSLUtils;
import kd.cd.webapi.ssl.TrustAllHostnameVerifier;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
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

    public static OkHttpClient.Builder newDefaultCustomizedBuilder() {
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
            builder.eventListenerFactory(new DruationListenerFactory());
        }
        return builder;
    }

    public static JSONObject respBodyToJson(Response resp) throws IOException {
        String bodyString = respBodyToString(resp);
        return StringUtils.isEmpty(bodyString) ?
                null :
                JSON.parseObject(bodyString);
    }

    public static <T> T respBodyToBean(Response resp, Class<T> beanClass) throws IOException {
        String bodyString = respBodyToString(resp);
        return StringUtils.isEmpty(bodyString) ?
                null :
                JSON.parseObject(bodyString, beanClass);
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
            throw new NullResponseException();
        }
        if (!resp.isSuccessful()) {
            throw new FailedResponseException(resp.toString());
        }
    }

    public static String bufferReqBody(Request req) {
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

    public static String bufferRespBody(Response resp) {
        try (Buffer buffer = newRespBuffer(resp)) {
            return buffer.clone().readUtf8();
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Clone resp failed", e);
            }
        }
        return null;
    }

    private static Buffer newRespBuffer(Response resp) throws IOException {
        ResponseBody respBody = resp.body();
        BufferedSource source = Objects.requireNonNull(respBody).source();
        source.request(Long.MAX_VALUE);
        return source.getBuffer();
    }
}