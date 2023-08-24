package kd.cd.webapi.req;

import kd.cd.webapi.log.LogOption;
import lombok.Getter;

import java.util.Map;

@Getter
public class RequestBase {
    protected String url;
    protected Method method;
    protected ContentType contentType;
    protected Map<String, String> headerMap;
    protected LogOption logOption;
}
