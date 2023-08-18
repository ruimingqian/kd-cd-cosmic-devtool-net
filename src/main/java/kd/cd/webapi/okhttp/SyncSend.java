package kd.cd.webapi.okhttp;

import kd.cd.webapi.log.LogOption;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;

import java.io.IOException;
import java.util.Map;

public interface SyncSend<T> {
    T sendFormData(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogOption logOption) throws IOException;

    T sendRawText(Method method, ContentType contentType, String url, String reqString, Map<String, String> headerMap, LogOption logOption) throws IOException;

    T sendUrlencoded(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogOption logOption) throws IOException;
}
