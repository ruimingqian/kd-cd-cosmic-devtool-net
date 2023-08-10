package kd.cd.webapi.apachehttp.client;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

public class RetryHandler implements HttpRequestRetryHandler {
    private final int retryCount;

    public RetryHandler(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext httpContext) {
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

        HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
        HttpRequest request = clientContext.getRequest();

        // 请求幂等,再次尝试
        return !(request instanceof HttpEntityEnclosingRequest);
    }
}
