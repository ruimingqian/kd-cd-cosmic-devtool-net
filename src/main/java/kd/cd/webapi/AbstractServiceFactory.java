package kd.cd.webapi;

import lombok.SneakyThrows;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * OkClientElementFactory
 *
 * @author qrm
 * @version 1.0
 */
public abstract class AbstractServiceFactory {

    @SneakyThrows
    public SSLContext newSSLContext() {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{newX509TrustManager()}, new SecureRandom());
        return sc;
    }

    public X509TrustManager newX509TrustManager() {
        return new MyX509TrustManager();
    }

    public HostnameVerifier newTrustAllHostnameVerifier() {
        return new TrustAllHostnameVerifier();
    }

    public SSLSocketFactory newSSLSocketFactory() {
        return newSSLContext().getSocketFactory();
    }

    static class MyX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
