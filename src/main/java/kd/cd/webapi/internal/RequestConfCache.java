package kd.cd.webapi.internal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.utils.SystemPropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 请求参数配置缓存
 *
 * @author qrm
 * @version 1.0
 * @since cosmic 5.0
 */
public class RequestConfCache {
    protected static final String REQUEST_FORM = SystemPropertyUtils.getString("outapilog.formid.3rdreq", "kdcd_3rdrequst");
    private static final Cache<String, Object> cache;
    private static final Map<String, RequestConfCache> reqConfCachePool = new ConcurrentHashMap<>();
    private String configNum;

    private RequestConfCache() {
    }

    static {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.initialCapacity(SystemPropertyUtils.getInt("reqconfigcache.default.initcapacity", 50));
        builder.maximumSize(SystemPropertyUtils.getInt("reqconfigcache.default.maxsize", 10000));
        builder.expireAfterWrite(SystemPropertyUtils.getInt("reqconfigcache.default.expireminutes", 30), TimeUnit.MINUTES);
        cache = builder.build();
    }

    private RequestConfCache(String configNum) {
        this.configNum = configNum;
        if (StringUtils.isEmpty(configNum)) {
            throw new IllegalArgumentException("Empty 3rdapi config number");
        }
        if (loadOrQuery() == null) {
            throw new KDBizException(String.format("获取接口配置信息失败！编码为'%s'的第三方接口信息未配置或已被禁用", configNum));
        }
    }

    public static RequestConfCache of(String configNum) {
        return reqConfCachePool.computeIfAbsent(configNum, k -> new RequestConfCache(configNum));
    }

    public LogParam getLogParam() {
        return getLogParam("");
    }

    public LogParam getLogParam(String bizFormId) {
        if (!isEnableLogging()) {
            return null;
        }
        LogParam logParam = (LogParam) cache.get(bizFormId + "_" + configNum + "_logparam", k -> {
            DynamicObject obj = loadOrQuery();
            return new LogParam(bizFormId, obj.getString("number"), obj.getString("name"));
        });
        assert logParam != null;
        if (isEnableFormat()) {
            logParam.setEnableFormat(true);
        }
        Integer respLimit = getChompSize();
        if (respLimit != null && respLimit > 0) {
            logParam.setRespLimitSize(respLimit);
        }
        return logParam.clone();
    }

    private Integer getChompSize() {
        try {
            return (Integer) getProperty("resplimit");
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isEnableFormat() {
        try {
            return (boolean) getProperty("formatlog");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isEnableLogging() {
        try {
            return (boolean) getProperty("enablelog");
        } catch (Exception e) {
            return true;
        }
    }

    public String getUrl() {
        return (String) cache.get(configNum + "_url", k -> {
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

    public Object getProperty(String property) {
        return loadOrQuery().get(property);
    }

    private DynamicObject loadOrQuery() {
        return (DynamicObject) cache.get(configNum + "_config", k -> {
            List<QFilter> filters = new ArrayList<>();
            filters.add(new QFilter("status", QCP.equals, "C"));
            filters.add(new QFilter("enable", QCP.equals, "1"));
            filters.add(new QFilter("number", QCP.equals, configNum));

            return BusinessDataServiceHelper.loadSingle(REQUEST_FORM, filters.toArray(new QFilter[0]));
        });
    }

    public static void clearCache() {
        cache.invalidateAll();
        reqConfCachePool.clear();
    }
}