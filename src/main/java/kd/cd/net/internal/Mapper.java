package kd.cd.net.internal;

import java.util.Map;

public interface Mapper<V> {
    Mapper<V> put(String key, V value);

    Mapper<V> replace(String key, V value);

    Mapper<V> remove(String... keys);

    String toJsonString();

    Map<String, V> toMap();
}
