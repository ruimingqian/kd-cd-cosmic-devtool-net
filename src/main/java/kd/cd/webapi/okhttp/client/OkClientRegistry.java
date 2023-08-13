package kd.cd.webapi.okhttp.client;

import kd.cd.webapi.okhttp.OkHttpUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class OkClientRegistry {
    private static final Map<String, OkHttpClient> clientPoolMap = new ConcurrentHashMap<>(8);

    private OkClientRegistry() {
    }

    public static OkHttpClient getOrRegisterAsDefault(String clientName) {
        return getOrRegister(clientName, () ->
                OkHttpUtils.newCustomizedBuilder().build());
    }

    public static OkHttpClient getOrRegister(String clientName, Supplier<OkHttpClient> clientSupplier) {
        if (StringUtils.isBlank(clientName) || clientSupplier == null) {
            throw new IllegalArgumentException();
        }
        return clientPoolMap.computeIfAbsent(clientName, k -> clientSupplier.get());
    }

    public static void unregister(String key) {
        clientPoolMap.remove(key);
    }

    public static OkHttpClient get(String key) {
        return clientPoolMap.get(key);
    }
}
