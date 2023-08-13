package kd.cd.webapi.req.config;

import kd.bos.cache.CacheConfigInfo;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.LocalMemoryCache;
import kd.bos.dataentity.entity.DynamicObject;

import java.util.function.Function;

public class EhcacheImpl extends AbstractRequestConfCache {
    private static final LocalMemoryCache cache;

    EhcacheImpl(String configNum) {
        super(configNum);
    }

    @Override
    public DynamicObject loadOrQuery() {
        return (DynamicObject) getFromCache(configNum + "_config", k -> query());
    }

    public <T> Object getFromCache(String key, Function<? super String, ? extends T> function) {
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
