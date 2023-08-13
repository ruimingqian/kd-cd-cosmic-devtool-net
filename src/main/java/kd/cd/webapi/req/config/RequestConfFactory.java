package kd.cd.webapi.req.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestConfFactory {
    private static final Map<String, RequestConfCache> configPoolMap = new ConcurrentHashMap<>(8);

    private RequestConfFactory() {
    }

    public static RequestConfCache getCaffine(String configNum) {
        return configPoolMap.computeIfAbsent(configNum + "_caffeine", k -> new CaffeineImpl(configNum));
    }

    public static RequestConfCache getCommon(String configNum) {
        return configPoolMap.computeIfAbsent(configNum + "_ehcache", k -> new EhcacheImpl(configNum));
    }
}
