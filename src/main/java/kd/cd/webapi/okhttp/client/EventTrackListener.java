package kd.cd.webapi.okhttp.client;

import kd.bos.context.RequestContext;
import kd.cd.webapi.log.LogParam;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

public class EventTrackListener extends EventListener {
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