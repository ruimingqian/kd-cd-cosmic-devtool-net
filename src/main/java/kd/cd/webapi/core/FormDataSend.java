package kd.cd.webapi.core;

import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.req.Method;

import java.io.IOException;
import java.util.Map;

public interface FormDataSend<T> {
    T sendFormData(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogParam logParam) throws IOException;
}