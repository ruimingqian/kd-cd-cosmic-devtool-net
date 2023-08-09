package kd.cd.webapi.internal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MappingParser<V> implements Mapper<V> {
    private Mapper<V> delegate;

    public static <V> MappingParser<V> fromJson(JSONObject jsonObject) {
        MappingParser<V> parser = new MappingParser<>();
        parser.delegate = new JsonMapper<>(jsonObject);
        return parser;
    }

    public static <V> MappingParser<V> fromJsonString(String jsonString) {
        MappingParser<V> parser = new MappingParser<>();
        parser.delegate = new JsonMapper<>(jsonString);
        return parser;
    }

    public static <V> MappingParser<V> fromMap(Map<String, V> map) {
        MappingParser<V> parser = new MappingParser<>();
        parser.delegate = new MapMapper<>(map);
        return parser;
    }

    @Override
    public Mapper<V> put(String key, V value) {
        delegate.put(key, value);
        return this;
    }

    @Override
    public Mapper<V> replace(String key, V value) {
        delegate.replace(key, value);
        return this;
    }

    @Override
    public Mapper<V> remove(String... keys) {
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

    static class JsonMapper<V> implements Mapper<V> {
        private final JSONObject jsonObject;

        public JsonMapper(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public JsonMapper(String jsonString) {
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
        public Mapper<V> put(String key, V value) {
            jsonObject.put(key, value);
            return this;
        }

        @Override
        public Mapper<V> replace(String key, V value) {
            jsonObject.replace(key, value);
            return this;
        }

        @Override
        public Mapper<V> remove(String... keys) {
            for (String key : keys) {
                jsonObject.remove(key);
            }
            return this;
        }
    }

    static class MapMapper<V> implements Mapper<V> {
        private final Map<String, V> map;

        public MapMapper(Map<String, V> map) {
            if (map == null) {
                this.map = new HashMap<>();
            } else {
                this.map = new HashMap<>(map);
            }
        }

        public Map<String, V> toMap() {
            return this.map;
        }

        public Mapper<V> put(String key, V value) {
            this.map.put(key, value);
            return this;
        }

        public Mapper<V> replace(String key, V value) {
            this.map.replace(key, value);
            return this;
        }

        public Mapper<V> remove(String... keys) {
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
