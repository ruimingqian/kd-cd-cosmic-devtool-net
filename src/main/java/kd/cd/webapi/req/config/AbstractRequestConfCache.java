package kd.cd.webapi.req.config;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.req.URLBuilder;
import kd.cd.webapi.util.SystemPropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractRequestConfCache implements RequestConfCache {
    protected static final String REQUEST_FORM = SystemPropertyUtils.getString("outapilog.formid.3rdreq", "kdcd_3rdrequst");
    protected static final int MAX_SIZE = SystemPropertyUtils.getInt("reqconfigcache.default.maxsize", 5000);
    protected static final int EXPIRE_SECONDS = SystemPropertyUtils.getInt("reqconfigcache.default.expireseconds", 600);
    protected String configNum;

    AbstractRequestConfCache(String configNum) {
        this.configNum = configNum;
        if (StringUtils.isBlank(configNum)) {
            throw new IllegalArgumentException("Empty 3rdapi config number");
        }
    }

    @Override
    public LogParam getLogParam() {
        return getLogParam("");
    }

    @Override
    public LogParam getLogParam(String bizFormId) {
        if (isEnableLogging()) {
            LogParam logParam = (LogParam) getFromCache(bizFormId + "_" + configNum + "_logparam", k -> {
                DynamicObject obj = loadOrQuery();
                return new LogParam(bizFormId, obj.getString("number"), obj.getString("name"));
            });
            logParam.setEnableFormat(isEnableFormat());

            Integer respLimit = getChompSize();
            if (respLimit != null && respLimit > 0) {
                logParam.setRespLimitSize(respLimit);
            }
            return logParam.clone();
        } else {
            return null;
        }
    }

    @Override
    public String getUrl() {
        return (String) getFromCache(configNum + "_url", k -> {
            DynamicObject o = loadOrQuery();
            return new URLBuilder()
                    .doMain(o.getString("domain"))
                    .port(o.getInt("port"))
                    .url(o.getString("url"))
                    .build();
        });
    }

    public String getCustomParam(String key) {
        return loadOrQuery().getDynamicObjectCollection("entry").stream()
                .filter(a -> a.getString("key").equals(key))
                .map(o -> o.getString("value"))
                .findFirst()
                .orElse("");
    }

    public Map<String, Object> getAllCustomParamAsMap() {
        return loadOrQuery().getDynamicObjectCollection("entry").stream()
                .collect(Collectors.toMap(k -> k.getString("key"), v -> v.get("value")));
    }

    public String getReqTemplateAsText() {
        return (String) getProperty("body_tag");
    }

    public Integer getChompSize() {
        try {
            return (Integer) getProperty("resplimit");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isEnableFormat() {
        try {
            return (Boolean) getProperty("formatlog");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEnableLogging() {
        try {
            return (Boolean) getProperty("enablelog");
        } catch (Exception e) {
            return true;
        }
    }

    public Object getProperty(String property) {
        return loadOrQuery().get(property);
    }

    public abstract DynamicObject loadOrQuery();

    public abstract <T> Object getFromCache(String key, Function<? super String, ? extends T> function);

    DynamicObject query() {
        List<QFilter> filters = new ArrayList<>();
        filters.add(new QFilter("status", QCP.equals, "C"));
        filters.add(new QFilter("enable", QCP.equals, "1"));
        filters.add(new QFilter("number", QCP.equals, configNum));

        DynamicObject single = BusinessDataServiceHelper.loadSingleFromCache(REQUEST_FORM, filters.toArray(new QFilter[0]));
        if (single == null) {
            throw new KDBizException(String.format("获取接口配置信息失败！编码为'%s'的第三方接口信息未配置或已被禁用", configNum));
        }
        return single;
    }
}
