package kd.cd.webapi.req.config;

import kd.cd.webapi.log.LogParam;

import java.util.Map;

public interface RequestConfig {
    LogParam getLogParam();

    LogParam getLogParam(String bizFormId);

    Integer getChompSize();

    boolean isEnableFormat();

    boolean isEnableLogging();

    String getUrl();

    String getCustomParam(String key);

    Map<String, Object> getAllCustomParamAsMap();

    String getReqTemplateAsText();

    Object getProperty(String property);
}
