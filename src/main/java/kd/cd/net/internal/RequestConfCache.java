package kd.cd.net.internal;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.cd.net.log.LogParam;
import kd.cd.net.utils.JacksonUtils;
import kd.cd.net.utils.SystemPropertyUtils;
import lombok.SneakyThrows;
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
        if (isNotConfigured()) {
            throw new KDBizException(String.format("获取接口配置信息失败！编码为'%s'的第三方接口信息未配置或已被禁用", configNum));
        }
    }

    public static RequestConfCache of(String configNum) {
        return reqConfCachePool.computeIfAbsent(configNum, k -> new RequestConfCache(configNum));
    }

    public final boolean isNotConfigured() {
        return loadOrQuery() == null;
    }

    public LogParam getLogParam() {
        return getLogParam("");
    }

    public LogParam getLogParam(String bizForm) {
        if (!isEnableLogging()) {
            return null;
        }
        Object logParam = cache.get(bizForm + "_" + configNum + "_logparam", k -> {
            DynamicObject obj = loadOrQuery();
            return new LogParam(bizForm, obj.getString("number"), obj.getString("name"));
        });
        assert logParam != null;
        return ((LogParam) logParam).clone();
    }

    public boolean isEnableLogging() {
        return (boolean) getProperty("enablelog");
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

    public Map<String, Object> getCustomParamMap() {
        return loadOrQuery().getDynamicObjectCollection("entry").stream()
                .collect(Collectors.toMap(k -> k.getString("key"), v -> v.get("value")));
    }

    @SneakyThrows
    public ObjectNode getBodyStructureJson() {
        return (ObjectNode) JacksonUtils.getObjectMapper().readTree(getBodyStructure());
    }

    public String getBodyStructure() {
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

    public void clearCache() {
        cache.cleanUp();
        reqConfCachePool.remove(configNum);
    }
}