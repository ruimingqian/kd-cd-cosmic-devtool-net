package kd.cd.webapi;

import kd.cd.webapi.log.LogParam;

import java.io.IOException;
import java.util.Map;

public interface UrlencodedSender<T> {
    T sendUrlencoded(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogParam logParam) throws IOException;
}
