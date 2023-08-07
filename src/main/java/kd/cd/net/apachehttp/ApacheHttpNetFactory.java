package kd.cd.net.apachehttp;

import kd.cd.net.AbstractNetFactory;
import lombok.SneakyThrows;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;

import javax.net.ssl.SSLException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

public class ApacheHttpNetFactory extends AbstractNetFactory {
    private static volatile ApacheHttpNetFactory factory;

    private ApacheHttpNetFactory() {
        if (factory != null) {
            throw new IllegalStateException("No reflection allowed here");
        }
    }

    public static ApacheHttpNetFactory getInstance() {
        if (factory == null) {
            synchronized (ApacheHttpNetFactory.class) {
                if (factory == null) {
                    factory = new ApacheHttpNetFactory();
                }
            }
        }
        return factory;
    }

    public SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
        return new SSLConnectionSocketFactory(newSSLContext(), new String[]{"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
    }

    @SneakyThrows
    public HttpClientConnectionManager newPoolingManager(int maxConnection, int defMaxPerRoute) {
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", getSSLConnectionSocketFactory()).build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 设置整个连接池最大连接数
        connectionManager.setMaxTotal(maxConnection);
        // 最大路由
        connectionManager.setDefaultMaxPerRoute(defMaxPerRoute);
        return connectionManager;
    }

    public HttpRequestRetryHandler newRetryHandler(int retryCount) {
        return (exception, executionCount, context) -> {
            // 重试次数
            if (executionCount >= retryCount) {
                return false;
            }

            if (exception instanceof UnknownHostException) {
                return false;
            }

            if (exception instanceof ConnectTimeoutException) {
                return false;
            }

            if (exception instanceof InterruptedIOException) {
                return false;
            }

            if (exception instanceof SSLException) {
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();

            // 请求幂等,再次尝试
            return !(request instanceof HttpEntityEnclosingRequest);
        };
    }

    public ConnectionKeepAliveStrategy newKeepAliveStrategy(long defaultTimeMillis) {
        return (response, context) -> {
            HeaderElementIterator iter = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (iter.hasNext()) {
                HeaderElement he = iter.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && "timeout".equalsIgnoreCase(param)) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return defaultTimeMillis;
        };
    }
}
