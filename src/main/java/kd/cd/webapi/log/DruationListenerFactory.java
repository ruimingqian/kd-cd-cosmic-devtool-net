package kd.cd.webapi.log;

import okhttp3.Call;
import okhttp3.EventListener;
import org.jetbrains.annotations.NotNull;

public class DruationListenerFactory implements EventListener.Factory {
    @NotNull
    @Override
    public EventListener create(Call call) {
        EventTracker tracker = call.request().tag(EventTracker.class);
        return tracker == null ? EventListener.NONE : new DurationListener(tracker);
    }
}
