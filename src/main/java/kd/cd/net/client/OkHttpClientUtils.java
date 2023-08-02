package kd.cd.net.client;

import kd.cd.net.okhttp.OkHttpNetTuplesFactory;
import kd.cd.net.utils.SystemPropertyUtils;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class OkHttpClientUtils {
    protected static final long CONNECT_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.connecttimeoutseconds", 10L);
    protected static final long READ_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.readtimeoutseconds", 60L);
    protected static final long WRITE_TIMEOUT_SECONDS = SystemPropertyUtils.getLong("okhttpclient.default.writetimeoutseconds", 60L);
    protected static final boolean IGNORE_SSL_CHECK = SystemPropertyUtils.getBoolean("okhttpclient.default.ignoressl", true);
    protected static final boolean ADD_LOG_MONITOR = SystemPropertyUtils.getBoolean("okhttpclient.default.addlogmonitor", true);

    public static OkHttpClient.Builder getDefaultBulider() {
        return getBulider(IGNORE_SSL_CHECK, ADD_LOG_MONITOR);
    }

    public static OkHttpClient.Builder getBulider(boolean ignoreSSL, boolean addLogMonitor) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        builder.connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        OkHttpNetTuplesFactory cf = OkHttpNetTuplesFactory.getInstance();
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
