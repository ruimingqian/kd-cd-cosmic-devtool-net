package kd.cd.webapi.ssl;

import lombok.SneakyThrows;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;

public class SSLUtils {
    private SSLUtils() {
    }

    public static SSLConnectionSocketFactory newSSLConnectionSocketFactory() {
        return new SSLConnectionSocketFactory(newSSLContext(), new String[]{"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
    }

    public static SSLSocketFactory newSSLSocketFactory() {
        return newSSLContext().getSocketFactory();
    }

    @SneakyThrows
    public static SSLContext newSSLContext() {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
        return sc;
    }
}
