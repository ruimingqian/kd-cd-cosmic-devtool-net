package kd.cd.webapi.req;

import kd.cd.webapi.log.EventTracker;
import kd.cd.webapi.log.LogOption;
import lombok.Getter;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Map;

@Getter
public abstract class AbstractBaseRequest implements BaseRequest {
    protected String url;
    protected Method method;
    protected ContentType contentType;
    protected Map<String, String> headers;
    protected LogOption logOption;

    Request generateOkHttpRequest(RequestBody reqBody, ContentType contentType) {
        Request.Builder reqBuilder = new Request.Builder()
                .url(url)
                .method(method.getName(), reqBody);

        if (logOption != null) {
            EventTracker tracker = new EventTracker();
            tracker.setLogOption(logOption);
            reqBuilder.tag(EventTracker.class, tracker);
        }
        if (contentType != null) {
            reqBuilder.addHeader("Content-Type", contentType.getName());
        }
        if (headers != null) {
            headers.forEach(reqBuilder::addHeader);
        }
        return reqBuilder.build();
    }
}
