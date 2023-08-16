package kd.cd.webapi.core;

import kd.cd.webapi.log.LogOption;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;

import java.io.IOException;
import java.util.Map;

public interface RawTextSend<T> {
    T sendRawText(Method method, ContentType contentType, String url, String reqString, Map<String, String> headerMap, LogOption logOption) throws IOException;
}
