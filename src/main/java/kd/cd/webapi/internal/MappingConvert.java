package kd.cd.webapi.internal;

import java.util.Map;

public interface MappingConvert<V> {
    MappingConvert<V> put(String key, V value);

    MappingConvert<V> replace(String key, V value);

    MappingConvert<V> remove(String... keys);

    String toJsonString();

    Map<String, V> toMap();
}
