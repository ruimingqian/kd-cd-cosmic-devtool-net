package kd.cd.net;

import kd.cd.net.log.LogParam;

import java.io.IOException;
import java.util.Map;

public interface RawSender<T> {
    T sendRaw(Method method, ContentType contentType, String url, String reqString, Map<String, String> headerMap, LogParam logParam) throws IOException;
}
