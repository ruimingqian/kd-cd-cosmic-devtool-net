package kd.cd.net;

import kd.cd.net.log.LogParam;

import java.io.IOException;
import java.util.Map;

public interface FormDataSender<T> {
    T sendFormData(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogParam logParam) throws IOException;
}
