package kd.cd.webapi.apachehttp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.webapi.apachehttp.client.RetryHandler;
import kd.cd.webapi.exception.FailedResponseException;
import kd.cd.webapi.exception.NullResponseException;
import kd.cd.webapi.ssl.SSLUtils;
import kd.cd.webapi.ssl.TrustAllHostnameVerifier;
import kd.cd.webapi.util.JacksonUtils;
import kd.cd.webapi.util.SystemPropertyUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class HttpUtils {
    private static final int CONNECT_TIMEOUT_SECONDS = SystemPropertyUtils.getInt("apachehttpclient.default.connecttimeoutseconds", 10);
    private static final int SOCKET_TIMEOUT_SECONDS = SystemPropertyUtils.getInt("apachehttpclient.default.sockettimeoutseconds", 60);
    private static final boolean IGNORE_SSL_CHECK = SystemPropertyUtils.getBoolean("apachehttpclient.default.ignoressl", true);
    private static final boolean ENABLE_RETRY = SystemPropertyUtils.getBoolean("apachehttpclient.default.retry", true);

    private HttpUtils() {
    }

    public static HttpClientBuilder newDefaultBulider() {
        return newBulider(IGNORE_SSL_CHECK, ENABLE_RETRY);
    }

    public static HttpClientBuilder newBulider(boolean ignoreSSL, boolean enableRetry) {
        HttpClientBuilder builder = HttpClients.custom();

        RequestConfig requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(true)
                .setConnectTimeout(CONNECT_TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(SOCKET_TIMEOUT_SECONDS * 1000)
                .build();
        builder.setDefaultRequestConfig(requestConfig);

        if (ignoreSSL) {
            builder.setSSLSocketFactory(SSLUtils.newSSLConnectionSocketFactory());
            builder.setSSLHostnameVerifier(new TrustAllHostnameVerifier());
        }
        if (enableRetry) {
            RetryHandler retryHandler = new RetryHandler(3);
            builder.setRetryHandler(retryHandler);
        }
        builder.evictExpiredConnections();

        return builder;
    }

    @SneakyThrows
    public static HttpClientConnectionManager newClientConnectionManager(int maxConnection, int defMaxPerRoute) {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", SSLUtils.newSSLConnectionSocketFactory()).build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 设置整个连接池最大连接数
        connectionManager.setMaxTotal(maxConnection);
        // 最大路由
        connectionManager.setDefaultMaxPerRoute(defMaxPerRoute);
        return connectionManager;
    }

    public static ObjectNode respToJson(CloseableHttpResponse resp) throws IOException {
        String bodyString = respToString(resp);
        return StringUtils.isEmpty(bodyString) ? null : (ObjectNode) JacksonUtils.getObjectMapper().readTree(bodyString);
    }

    public static String respToString(CloseableHttpResponse resp) throws IOException {
        checkResp(resp);
        return EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
    }

    public static byte[] respToBytes(CloseableHttpResponse resp) throws IOException {
        checkResp(resp);
        return EntityUtils.toByteArray(resp.getEntity());
    }

    public static InputStream respToInputStream(CloseableHttpResponse resp) throws IOException {
        try {
            checkResp(resp);
            return resp.getEntity().getContent();
        } finally {
            closeResp(resp);
        }
    }

    private static void checkResp(CloseableHttpResponse resp) {
        if (resp == null) {
            throw new NullResponseException();
        }
        HttpEntity respEntity = resp.getEntity();
        if (respEntity == null) {
            closeResp(resp);
            throw new NullResponseException("Null CloseableHttpResponse.Entity");
        }
        if (!isSuccessful(resp)) {
            closeResp(resp);
            throw new FailedResponseException("http request fail:" + resp.getStatusLine().getReasonPhrase());
        }
    }

    public static void closeResp(CloseableHttpResponse resp) {
        try {
            if (resp != null) {
                resp.close();
            }
        } catch (IOException e) {
            //Ignore
        }
    }

    public static boolean isSuccessful(CloseableHttpResponse resp) {
        if (resp == null) {
            return false;
        }
        return resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }
}
