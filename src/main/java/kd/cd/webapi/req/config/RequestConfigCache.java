package kd.cd.webapi.req.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestConfigCache {
    private static final Map<String, RequestConfig> configPoolMap = new ConcurrentHashMap<>(64);

    private RequestConfigCache() {
    }

    public static RequestConfig getConfig(String configNum) {
        return configPoolMap.computeIfAbsent(configNum, k -> new RequestConfigImpl(configNum));
    }
}
