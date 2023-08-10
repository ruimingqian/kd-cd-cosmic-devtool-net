package kd.cd.webapi.okhttp.client;

import kd.bos.context.RequestContext;
import kd.cd.webapi.log.LogParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class EventTracker {
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
        return String.format("Call Start => Call End: %s ms\nDNS Resolution: %s ms\n" +
                        "Establish Socket Channel: %s ms\nSSL Handshake: %s ms\n" +
                        "Request End => Response Start: %s ms\nWrite Bytes: %s ms\n" +
                        "Read Bytes: %s ms"
                , callDuration, dnsDuration, connetDuration, sslDuration, serveDuration, requestDuration, responseDuration);
    }
}