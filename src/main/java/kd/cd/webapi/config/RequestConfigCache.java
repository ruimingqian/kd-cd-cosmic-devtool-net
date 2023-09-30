package kd.cd.webapi.config;

public class RequestConfigCache {
    private RequestConfigCache() {
    }

    public static RequestConfig getConfig(String configNum) {
        return new RequestConfigImpl(configNum);
    }
}
