package kd.cd.webapi.config;

import kd.cd.webapi.log.LogOption;

import java.util.Map;

public interface RequestConfig {
    String url();

    LogOption logOption();

    Integer chompSize();

    String getCustomParam(String key);

    Map<String, Object> allCustomParamMap();

    String reqTemplateText();

    Object getProperty(String property, Object def);

    boolean isEnableFormat();

    boolean isEnableLogging();
}
