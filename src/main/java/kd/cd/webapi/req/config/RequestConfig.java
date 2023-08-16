package kd.cd.webapi.req.config;

import kd.cd.webapi.log.LogParam;

import java.util.Map;

public interface RequestConfig {
    String url();

    LogParam logParam();

    LogParam logParam(String bizFormId);

    Integer chompSize();

    String getCustomParam(String key);

    Map<String, Object> allCustomParamMap();

    String reqTemplateText();

    Object getProperty(String property);

    boolean isEnableFormat();

    boolean isEnableLogging();
}
