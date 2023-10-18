package kd.cd.webapi.biz;

import com.alibaba.fastjson.JSONObject;
import kd.bos.dlock.DLock;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.exception.KDBizException;
import kd.cd.webapi.okhttp.SyncSingletonHttpSender;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;
import kd.cd.webapi.req.RawRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Builder
public class YZJTokenGenerator {
    private static final IAppCache cache = AppCache.get("YZJ_TOKEN");
    private final String eid;
    private final String secret;

    public String cacheAccessToken() {
        return cacheAccessToken(300000L);
    }

    public String cacheAccessToken(long thresholdMillis) {
        String key = this.eid;
        YZJToken token = getRealTimeCachedToken(key);

        if (token == null || token.isMeetExpireThreshold(thresholdMillis) || token.isExpired()) {
            try (DLock lock = DLock.create(key)) {
                lock.lock();

                token = getRealTimeCachedToken(key);
                if (token == null || token.isMeetExpireThreshold(thresholdMillis) || token.isExpired()) {
                    Token newToken;
                    if (token == null) {
                        newToken = this.newAccessToken();
                    } else {
                        String refreshToken = token.getRefreshToken();
                        newToken = this.refreshAccessToken(refreshToken);
                    }
                    cache.put(key, newToken);
                    return newToken.getContent();

                } else {
                    return token.getContent();
                }
            }

        } else {
            return token.getContent();
        }
    }

    private static YZJToken getRealTimeCachedToken(String key) {
        return cache.get(key, YZJToken.class);
    }

    @SneakyThrows
    public Token newAccessToken() {
        JSONObject json = new JSONObject(4);
        json.put("eid", eid);
        json.put("secret", secret);
        json.put("scope", "resGroupSecret");
        json.put("timestamp", System.currentTimeMillis());

        RawRequest rawRequest = RawRequest.builder()
                .url("https://yunzhijia.com/gateway/oauth2/token/getAccessToken")
                .method(Method.POST)
                .contentType(ContentType.APPLICATION_JSON)
                .reqString(json.toJSONString())
                .build();

        JSONObject result = SyncSingletonHttpSender.get()
                .sendRequest(rawRequest)
                .bodyToJson();

        return new YZJToken(result);
    }

    @SneakyThrows
    public Token refreshAccessToken(String refreshToken) {
        JSONObject json = new JSONObject(4);
        json.put("eid", eid);
        json.put("refreshToken", refreshToken);
        json.put("scope", "resGroupSecret");
        json.put("timestamp", System.currentTimeMillis());

        RawRequest rawRequest = RawRequest.builder()
                .url("https://yunzhijia.com/gateway/oauth2/token/refreshToken")
                .method(Method.POST)
                .contentType(ContentType.APPLICATION_JSON)
                .reqString(json.toJSONString())
                .build();

        JSONObject result = SyncSingletonHttpSender.get()
                .sendRequest(rawRequest)
                .bodyToJson();

        return new YZJToken(result);
    }

    @Getter
    @NoArgsConstructor
    public static class YZJToken implements Token {
        private String content;
        private String refreshToken;
        private Long expireTime;

        public YZJToken(JSONObject json) {
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                JSONObject data = (JSONObject) json.get("data");
                this.content = data.getString("accessToken");
                this.refreshToken = data.getString("refreshToken");
                this.expireTime = data.getLong("expireIn") * 1000 + System.currentTimeMillis();
            } else {
                throw new KDBizException(json.getString("message"));
            }
        }

        public boolean isMeetExpireThreshold(long expireThreshold) {
            return !isExpired() && expireTime < System.currentTimeMillis() + expireThreshold;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expireTime;
        }
    }
}
