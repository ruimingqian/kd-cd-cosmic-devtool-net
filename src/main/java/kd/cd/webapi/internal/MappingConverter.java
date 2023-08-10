package kd.cd.webapi.internal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MappingConverter<V> implements MappingConvert<V> {
    private MappingConvert<V> delegate;

    public static <V> MappingConverter<V> fromJson(JSONObject jsonObject) {
        MappingConverter<V> converter = new MappingConverter<>();
        converter.delegate = new JsonConverter<>(jsonObject);
        return converter;
    }

    public static <V> MappingConverter<V> fromJsonString(String jsonString) {
        MappingConverter<V> converter = new MappingConverter<>();
        converter.delegate = new JsonConverter<>(jsonString);
        return converter;
    }

    public static <V> MappingConverter<V> fromMap(Map<String, V> map) {
        MappingConverter<V> converter = new MappingConverter<>();
        converter.delegate = new MapConverter<>(map);
        return converter;
    }

    @Override
    public MappingConvert<V> put(String key, V value) {
        delegate.put(key, value);
        return this;
    }

    @Override
    public MappingConvert<V> replace(String key, V value) {
        delegate.replace(key, value);
        return this;
    }

    @Override
    public MappingConvert<V> remove(String... keys) {
        delegate.remove(keys);
        return this;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public String toJsonString() {
        return delegate.toJsonString();
    }

    @Override
    public Map<String, V> toMap() {
        return delegate.toMap();
    }

    static class JsonConverter<V> implements MappingConvert<V> {
        private final JSONObject jsonObject;

        public JsonConverter(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public JsonConverter(String jsonString) {
            if (StringUtils.isEmpty(jsonString)) {
                this.jsonObject = new JSONObject();
            } else {
                this.jsonObject = JSON.parseObject(jsonString);
            }
        }

        public String toJsonString() {
            return jsonObject.toString();
        }

        @Override
        public Map<String, V> toMap() {
            String jsonString = jsonObject.toJSONString();
            TypeReference<Map<String, V>> typeReference = new TypeReference<Map<String, V>>() {
            };
            return JSON.parseObject(jsonString, typeReference.getType());
        }

        @Override
        public MappingConvert<V> put(String key, V value) {
            jsonObject.put(key, value);
            return this;
        }

        @Override
        public MappingConvert<V> replace(String key, V value) {
            jsonObject.replace(key, value);
            return this;
        }

        @Override
        public MappingConvert<V> remove(String... keys) {
            for (String key : keys) {
                jsonObject.remove(key);
            }
            return this;
        }
    }

    static class MapConverter<V> implements MappingConvert<V> {
        private final Map<String, V> map;

        public MapConverter(Map<String, V> map) {
            if (map == null) {
                this.map = new HashMap<>();
            } else {
                this.map = new HashMap<>(map);
            }
        }

        public Map<String, V> toMap() {
            return this.map;
        }

        public MappingConvert<V> put(String key, V value) {
            this.map.put(key, value);
            return this;
        }

        public MappingConvert<V> replace(String key, V value) {
            this.map.replace(key, value);
            return this;
        }

        public MappingConvert<V> remove(String... keys) {
            Set<String> keySet = this.map.keySet();
            Arrays.asList(keys).forEach(keySet::remove);
            return this;
        }

        @Override
        public String toJsonString() {
            return JSON.toJSONString(this.map);
        }
    }
}
