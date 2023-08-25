package kd.cd.webapi.config;

import kd.bos.cache.CacheConfigInfo;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.LocalMemoryCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.cd.webapi.log.LogOption;
import kd.cd.webapi.req.URLBuilder;
import kd.cd.webapi.util.SystemPropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RequestConfigImpl implements RequestConfig {
    protected static final String REQUEST_FORM = SystemPropertyUtils.getString("outapilog.formid.3rdreq", "kdcd_3rdrequst");
    protected static final int MAX_SIZE = SystemPropertyUtils.getInt("reqconfigcache.default.maxsize", 5000);
    protected static final int EXPIRE_SECONDS = SystemPropertyUtils.getInt("reqconfigcache.default.expireseconds", 600);
    private static final LocalMemoryCache cache;
    protected String configNum;

    RequestConfigImpl(String configNum) {
        this.configNum = configNum;
        if (StringUtils.isBlank(configNum)) {
            throw new IllegalArgumentException("Empty config number");
        }
        if (loadObj() == null) {
            throw new KDBizException(String.format("获取接口配置信息失败！编码为'%s'的第三方接口信息未配置或已被禁用", configNum));
        }
    }

    @Override
    public LogOption logOption() {
        if (isEnableLogging()) {
            String bizFormId = (String) getProperty("bizform.number", "");

            LogOption logOption = (LogOption) getFromCache(configNum + "_logoption", k -> {
                DynamicObject o = loadObj();
                return new LogOption(bizFormId, o.getString("number"), o.getString("name"));
            });

            logOption.setEnableFormat(isEnableFormat());

            Integer respLimit = chompSize();
            if (respLimit != null && respLimit > 0) {
                logOption.setChopSize(respLimit);
            }
            return logOption.clone();

        } else {
            return null;
        }
    }

    @Override
    public String url() {
        return (String) getFromCache(configNum + "_url", k -> {
            DynamicObject o = loadObj();
            return new URLBuilder()
                    .doMain(o.getString("domain"))
                    .port(o.getInt("port"))
                    .url(o.getString("url"))
                    .build();
        });
    }

    @Override
    public String getCustomParam(String key) {
        return loadObj().getDynamicObjectCollection("entry").stream()
                .filter(a -> a.getString("key").equals(key))
                .map(o -> o.getString("value"))
                .findFirst()
                .orElse("");
    }

    @Override
    public Map<String, Object> allCustomParamMap() {
        return loadObj().getDynamicObjectCollection("entry").stream()
                .collect(Collectors.toMap(k -> k.getString("key"), v -> v.get("value")));
    }

    @Override
    public String reqTemplateText() {
        return (String) getProperty("body_tag", "");
    }

    @Override
    public Integer chompSize() {
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
    public Object getProperty(String property, Object def) {
        try {
            return loadObj().get(property);
        } catch (Exception e) {
            return def;
        }
    }

    private DynamicObject loadObj() {
        return (DynamicObject) getFromCache(configNum + "_config", k -> {
            List<QFilter> filters = new ArrayList<>();
            filters.add(new QFilter("status", QCP.equals, "C"));
            filters.add(new QFilter("enable", QCP.equals, "1"));
            filters.add(new QFilter("number", QCP.equals, configNum));

            return BusinessDataServiceHelper.loadSingleFromCache(REQUEST_FORM, filters.toArray(new QFilter[0]));
        });
    }

    private static <T> Object getFromCache(String key, Function<? super String, ? extends T> function) {
        Object o = cache.get(key);
        if (o == null) {
            T t = function.apply(key);
            cache.put(key, t);
            return t;
        }
        return o;
    }

    static {
        CacheConfigInfo localConfig = new CacheConfigInfo();
        localConfig.setMaxItemSize(MAX_SIZE);
        localConfig.setTimeout(EXPIRE_SECONDS);
        cache = CacheFactory.getCommonCacheFactory().$getOrCreateLocalMemoryCache("api_region", "3rdapi", localConfig);
    }
}
