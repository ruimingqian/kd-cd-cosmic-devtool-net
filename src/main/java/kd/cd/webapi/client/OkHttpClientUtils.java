package kd.cd.webapi.client;

import kd.cd.webapi.okhttp.OkHttpServiceFactory;
import kd.cd.webapi.utils.SystemPropertyUtils;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class OkHttpClientUtils {
    private static final long CONNECT_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.connecttimeoutseconds", 10L);
    private static final long READ_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.readtimeoutseconds", 60L);
    private static final long WRITE_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.writetimeoutseconds", 60L);
    private static final boolean IGNORE_SSL_CHECK = SystemPropertyUtils.getBoolean("okhttpclient.default.ignoressl", true);
    private static final boolean ADD_LOG_MONITOR = SystemPropertyUtils.getBoolean("okhttpclient.default.addlogmonitor", true);

    private OkHttpClientUtils() {
    }

    public static OkHttpClient.Builder getDefaultBulider() {
        return getBulider(IGNORE_SSL_CHECK, ADD_LOG_MONITOR);
    }

    public static OkHttpClient.Builder getBulider(boolean ignoreSSL, boolean addLogMonitor) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        builder.connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        OkHttpServiceFactory cf = OkHttpServiceFactory.getInstance();
        if (ignoreSSL) {
            builder.sslSocketFactory(cf.newSSLSocketFactory(), cf.newX509TrustManager());
            builder.hostnameVerifier(cf.newTrustAllHostnameVerifier());
        }

        if (addLogMonitor) {
            builder.addInterceptor(cf.newRespLogInterceptor());
            builder.eventListenerFactory(cf.newTrackEventListenerFactory());
        }
        return builder;
    }
}
