package kd.cd.webapi.req.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kd.bos.dataentity.entity.DynamicObject;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CaffeineImpl extends AbstractRequestConfCache {
    private static final Cache<String, Object> cache;

    CaffeineImpl(String configNum) {
        super(configNum);
    }

    @Override
    public DynamicObject loadOrQuery() {
        return (DynamicObject) getFromCache(configNum + "_config", k -> query());
    }

    @Override
    public <T> Object getFromCache(String key, Function<? super String, ? extends T> function) {
        return cache.get(key, function);
    }

    static {
        cache = Caffeine.newBuilder()
                .maximumSize(MAX_SIZE)
                .expireAfterWrite(EXPIRE_SECONDS, TimeUnit.SECONDS)
                .build();
    }
}