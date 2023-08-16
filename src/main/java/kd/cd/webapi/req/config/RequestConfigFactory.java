package kd.cd.webapi.req.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestConfigFactory {
    private static final Map<String, RequestConfig> configPoolMap = new ConcurrentHashMap<>(64);

    private RequestConfigFactory() {
    }

    public static RequestConfig getConfig(String configNum) {
        return configPoolMap.computeIfAbsent(configNum, k -> new RequestConfigImpl(configNum));
    }
}
