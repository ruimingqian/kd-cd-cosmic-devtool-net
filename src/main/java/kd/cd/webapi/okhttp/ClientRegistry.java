package kd.cd.webapi.okhttp;

import kd.cd.webapi.util.OkHttpUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ClientRegistry {
    private static final Map<String, OkHttpClient> clientPoolMap = new ConcurrentHashMap<>(8);

    private ClientRegistry() {
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
