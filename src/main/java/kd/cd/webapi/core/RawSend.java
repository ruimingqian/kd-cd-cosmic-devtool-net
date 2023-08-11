package kd.cd.webapi.core;

import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;

import java.io.IOException;
import java.util.Map;

public interface RawSend<T> {
    T sendRaw(Method method, ContentType contentType, String url, String reqString, Map<String, String> headerMap, LogParam logParam) throws IOException;
}
