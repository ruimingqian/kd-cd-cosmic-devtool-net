package kd.cd.webapi.log;

import kd.bos.context.RequestContext;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class EventTracker {
    private LogOption logOption;
    private long callDuration;
    private long dnsDuration;
    private long connetDuration;
    private long sslDuration;
    private long requestDuration;
    private long responseDuration;
    private long serveDuration;

    public void log(Exception e) {
        if (logOption == null) {
            return;
        }
        logOption.timeCost = callDuration;
        logOption.trackInfo = toString();

        if (e != null) {
            logOption.exception = e;
        }

        if (logOption.enableNewThread) {
            logOption.requestContext = RequestContext.get();
            OkLogger.require().logAsync(logOption);
        } else {
            OkLogger.require().log(logOption);
        }
    }

    @Override
    public String toString() {
        return String.format("Call Start => Call End: %s ms\nDNS Resolution: %s ms\n" +
                        "Establish Socket Channel: %s ms\nSSL Handshake: %s ms\n" +
                        "Request End => Response Start: %s ms\nWrite Bytes: %s ms\n" +
                        "Read Bytes: %s ms"
                , callDuration, dnsDuration, connetDuration, sslDuration, serveDuration, requestDuration, responseDuration);
    }
}