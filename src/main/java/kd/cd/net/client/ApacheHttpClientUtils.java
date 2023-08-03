package kd.cd.net.client;

import kd.cd.net.apachehttp.ApacheHttpNetTuplesFactory;
import kd.cd.net.utils.SystemPropertyUtils;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class ApacheHttpClientUtils {
    private static final int CONNECT_TIMEOUT_SECONDS = SystemPropertyUtils.getInt("apachehttpclient.default.connecttimeoutseconds", 10);
    private static final int SOCKET_TIMEOUT_SECONDS = SystemPropertyUtils.getInt("apachehttpclient.default.sockettimeoutseconds", 60);
    private static final boolean IGNORE_SSL_CHECK = SystemPropertyUtils.getBoolean("apachehttpclient.default.ignoressl", true);
    private static final boolean ENABLE_RETRY = SystemPropertyUtils.getBoolean("apachehttpclient.default.retry", true);

    private ApacheHttpClientUtils() {
    }

    public static HttpClientBuilder getDefaultBulider() {
        return getBulider(IGNORE_SSL_CHECK, ENABLE_RETRY);
    }

    public static HttpClientBuilder getBulider(boolean ignoreSSL, boolean enableRetry) {
        HttpClientBuilder builder = HttpClients.custom();

        RequestConfig requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(true)
                .setConnectTimeout(CONNECT_TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(SOCKET_TIMEOUT_SECONDS * 1000)
                .build();
        builder.setDefaultRequestConfig(requestConfig);

        ApacheHttpNetTuplesFactory af = ApacheHttpNetTuplesFactory.getInstance();
        if (ignoreSSL) {
            builder.setSSLSocketFactory(af.getSSLConnectionSocketFactory());
            builder.setSSLHostnameVerifier(af.newTrustAllHostnameVerifier());
        }
        if (enableRetry) {
            HttpRequestRetryHandler retryHandler = af.newRetryHandler(3);
            builder.setRetryHandler(retryHandler);
        }
        builder.evictExpiredConnections();

        return builder;
    }
}
