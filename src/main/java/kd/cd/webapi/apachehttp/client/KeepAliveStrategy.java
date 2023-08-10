package kd.cd.webapi.apachehttp.client;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class KeepAliveStrategy implements ConnectionKeepAliveStrategy {
    private final long defaultTimeMillis;

    public KeepAliveStrategy(long defaultTimeMillis) {
        this.defaultTimeMillis = defaultTimeMillis;
    }

    @Override
    public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
        HeaderElementIterator iter = new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (iter.hasNext()) {
            HeaderElement he = iter.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (value != null && "timeout".equalsIgnoreCase(param)) {
                return Long.parseLong(value) * 1000;
            }
        }
        return defaultTimeMillis;
    }
}
