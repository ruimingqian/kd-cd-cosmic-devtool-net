package kd.cd.webapi.config;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.cd.webapi.log.LogOption;
import kd.cd.webapi.req.URLBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestConfigImpl implements RequestConfig {
    private static final String REQUEST_FORM = "kdcd_3rdrequst";
    private final String configNum;
    private final DynamicObject entity;

    RequestConfigImpl(String configNum) {
        this.configNum = configNum;
        if (StringUtils.isBlank(configNum)) {
            throw new IllegalArgumentException("Empty config number");
        }
        this.entity = loadFromCache();
        if (this.entity == null) {
            throw new KDBizException(String.format("获取接口配置信息失败！编码为'%s'的第三方接口信息未配置或已被禁用", configNum));
        }
    }

    @Override
    public LogOption logOption() {
        if (isEnableLogging()) {
            String formId = (String) getProperty("bizform.number", "");
            LogOption logOption = new LogOption(formId, entity.getString("number"), entity.getString("name"));
            logOption.setEnableFormat(isEnableFormat());

            if (isRecordRespBody()) {
                Integer chopSize = chopSize();
                if (chopSize != null && chopSize > 0) {
                    logOption.setChopSize(chopSize);
                }
            } else {
                logOption.setRecordFullResponse(false);
            }
            return logOption;

        } else {
            return null;
        }
    }

    @Override
    public String url() {
        return new URLBuilder()
                .doMain(entity.getString("domain"))
                .port(entity.getInt("port"))
                .url(entity.getString("url"))
                .build();
    }

    @Override
    public String getCustomParam(String key) {
        return entity.getDynamicObjectCollection("entry").stream()
                .filter(a -> a.getString("key").equals(key))
                .map(o -> o.getString("value"))
                .findFirst()
                .orElse("");
    }

    @Override
    public Map<String, Object> allCustomParamMap() {
        return entity.getDynamicObjectCollection("entry").stream()
                .collect(Collectors.toMap(k -> k.getString("key"), v -> v.get("value")));
    }

    @Override
    public String reqTemplateText() {
        return (String) getProperty("body_tag", "");
    }

    @Override
    public Integer chopSize() {
        return (Integer) getProperty("resplimit", null);
    }

    @Override
    public boolean isEnableFormat() {
        return (Boolean) getProperty("formatlog", false);
    }

    @Override
    public boolean isEnableLogging() {
        return (Boolean) getProperty("enablelog", true);
    }

    @Override
    public boolean isRecordRespBody() {
        return (Boolean) getProperty("recordrespbody", true);
    }

    @Override
    public Object getProperty(String property, Object def) {
        try {
            return entity.get(property);
        } catch (Exception e) {
            return def;
        }
    }

    private DynamicObject loadFromCache() {
        List<QFilter> filters = new ArrayList<>(3);
        filters.add(new QFilter("status", QCP.equals, "C"));
        filters.add(new QFilter("enable", QCP.equals, "1"));
        filters.add(new QFilter("number", QCP.equals, configNum));
        return BusinessDataServiceHelper.loadSingleFromCache(REQUEST_FORM, filters.toArray(new QFilter[0]));
    }
}
