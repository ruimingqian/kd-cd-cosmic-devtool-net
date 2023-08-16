package kd.cd.webapi.req.config;

import kd.cd.webapi.log.LogOption;

import java.util.Map;

public interface RequestConfig {
    String url();

    LogOption logOption();

    LogOption logOption(String bizFormId);

    Integer chompSize();

    String getCustomParam(String key);

    Map<String, Object> allCustomParamMap();

    String reqTemplateText();

    Object getProperty(String property);

    boolean isEnableFormat();

    boolean isEnableLogging();
}
