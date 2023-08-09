package kd.cd.webapi.okhttp;

import kd.bos.context.RequestContext;
import kd.cd.webapi.AbstractNetFactory;
import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.log.OkHttpLogger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OkHttpNetFactory extends AbstractNetFactory {
    private static volatile OkHttpNetFactory factory;

    private OkHttpNetFactory() {
        if (factory != null) {
            throw new IllegalStateException("No reflection allowed here");
        }
    }

    public static OkHttpNetFactory getInstance() {
        if (factory == null) {
            synchronized (OkHttpNetFactory.class) {
                if (factory == null) {
                    factory = new OkHttpNetFactory();
                }
            }
        }
        return factory;
    }

    public ConnectionPool newConnectionPool(int maxConnections, long keepAliveMinutes) {
        return new ConnectionPool(maxConnections, keepAliveMinutes, TimeUnit.MINUTES);
    }

    public EventListener.Factory newTrackEventListenerFactory() {
        return new TrackEventListenerFactory();
    }

    public Interceptor newRespLogInterceptor() {
        return new RespLogInterceptor();
    }

    static class RespLogInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request req = chain.request();
            OkHttpNetFactory.EventTracker tracker = req.tag(OkHttpNetFactory.EventTracker.class);

            if (tracker == null) {
                return chain.proceed(req);
            }
            LogParam logParam = tracker.getLogParam();
            if (logParam == null) {
                return chain.proceed(req);
            }

            Response resp = null;
            try {
                resp = chain.proceed(req);

            } catch (Exception e) {
                logParam.setException(e);
                throw e;

            } finally {
                logParam.setReqInfo(OkHttpUtils.requestToJson(req, logParam.isRecordFullRequest()));
                logParam.setRespInfo(OkHttpUtils.responseToJson(resp, logParam.isRecordFullResponse()));
            }

            return resp;
        }
    }

    static class TrackEventListenerFactory implements EventListener.Factory {
        @NotNull
        @Override
        public EventListener create(Call call) {
            EventTracker tracker = call.request().tag(EventTracker.class);
            return tracker == null ? EventListener.NONE : new EventTrackListener(tracker);
        }
    }

    static class EventTrackListener extends EventListener {
        private static final OkHttpLogger okHttpLogger = new OkHttpLogger();
        private final EventTracker tracker;
        private long callStart;
        private long dnsStart;
        private long connectStart;
        private long secureConnectStart;
        private long requestStart;
        private long responseStart;
        private long requestBodyEnd;

        public EventTrackListener(EventTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public void callStart(@NotNull Call call) {
            callStart = System.currentTimeMillis();
        }

        @Override
        public void dnsStart(@NotNull Call call, @NotNull String domainName) {
            dnsStart = System.currentTimeMillis();
        }

        @Override
        public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
            tracker.setDnsDuration(System.currentTimeMillis() - dnsStart);
        }

        @Override
        public void connectStart(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy) {
            connectStart = System.currentTimeMillis();
        }

        @Override
        public void secureConnectStart(@NotNull Call call) {
            secureConnectStart = System.currentTimeMillis();
        }

        @Override
        public void secureConnectEnd(@NotNull Call call, Handshake handshake) {
            tracker.setSslDuration(System.currentTimeMillis() - secureConnectStart);
        }

        @Override
        public void connectEnd(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, Protocol protocol) {
            tracker.setConnetDuration(System.currentTimeMillis() - connectStart);
        }

        @Override
        public void connectFailed(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, Protocol protocol, @NotNull IOException ioe) {
            tracker.setConnetDuration(System.currentTimeMillis() - connectStart);
        }

        @Override
        public void requestHeadersStart(@NotNull Call call) {
            requestStart = System.currentTimeMillis();
            super.requestHeadersStart(call);
        }

        @Override
        public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
            tracker.setRequestDuration(System.currentTimeMillis() - requestStart);
        }

        @Override
        public void requestBodyStart(@NotNull Call call) {
            tracker.setRequestDuration(System.currentTimeMillis() - requestStart);
        }

        @Override
        public void requestBodyEnd(@NotNull Call call, long byteCount) {
            tracker.setRequestDuration(System.currentTimeMillis() - requestStart);
            requestBodyEnd = System.currentTimeMillis();
            responseStart = 0L;
        }

        @Override
        public void requestFailed(@NotNull Call call, @NotNull IOException ioe) {
            tracker.setRequestDuration(System.currentTimeMillis() - requestStart);
            responseStart = System.currentTimeMillis();
        }

        @Override
        public void responseHeadersStart(@NotNull Call call) {
            responseStart = System.currentTimeMillis();
            tracker.setResponseDuration(0L);
        }

        @Override
        public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
            tracker.setResponseDuration(System.currentTimeMillis() - responseStart);
        }

        @Override
        public void responseBodyStart(@NotNull Call call) {
            if (responseStart == 0L) {
                responseStart = System.currentTimeMillis();
            }
        }

        @Override
        public void responseBodyEnd(@NotNull Call call, long byteCount) {
            tracker.setResponseDuration(System.currentTimeMillis() - responseStart);
            tracker.setServeDuration(responseStart - (requestStart + tracker.getRequestDuration()));
        }

        @Override
        public void responseFailed(@NotNull Call call, @NotNull IOException ioe) {
            if (responseStart == 0L) {
                responseStart = requestBodyEnd;
            }
            tracker.setResponseDuration(System.currentTimeMillis() - responseStart);
            tracker.setServeDuration(System.currentTimeMillis() - (requestStart + tracker.getRequestDuration()));
        }

        @Override
        public void callEnd(@NotNull Call call) {
            tracker.setCallDuration(System.currentTimeMillis() - callStart);
            toLog(null);
        }

        @Override
        public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
            tracker.setCallDuration(System.currentTimeMillis() - callStart);
            toLog(ioe);
        }

        private void toLog(Exception e) {
            LogParam logParam = tracker.getLogParam();
            if (logParam == null) {
                return;
            }

            logParam.setTimeCost(tracker.getCallDuration());
            logParam.setTrackInfo(tracker.toString());
            if (e != null) {
                logParam.setException(e);
            }

            if (RequestContext.get() == null) {
                RequestContext.set(tracker.getRequestContext());
            }

            if (logParam.isEnableNewThread()) {
                logParam.setRequestContext(RequestContext.get());
                okHttpLogger.logAsync(logParam);
            } else {
                okHttpLogger.log(logParam);
            }
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    static class EventTracker {
        private RequestContext requestContext;
        private long callDuration;
        private long dnsDuration;
        private long connetDuration;
        private long sslDuration;
        private long requestDuration;
        private long responseDuration;
        private long serveDuration;
        private LogParam logParam;

        @Override
        public String toString() {
            return "Call Start => Call End:" + callDuration + "ms\n" +
                    "DNS Resolution:" + dnsDuration + "ms\n" +
                    "Establish Socket Channel:" + connetDuration + "ms\n" +
                    "SSL Handshake:" + sslDuration + "ms\n" +
                    "Request End => Response Start:" + serveDuration + "ms\n" +
                    "Write Bytes:" + requestDuration + "ms\n" +
                    "Read Bytes:" + responseDuration + "ms\n";
        }
    }
}
