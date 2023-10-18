package kd.cd.webapi.biz;

import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dlock.DLock;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.exception.KDBizException;
import kd.cd.webapi.okhttp.SyncSingletonHttpSender;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;
import kd.cd.webapi.req.RawRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class TokenGenerator {
    private static final IAppCache cache = AppCache.get("COSMIC_TOKEN");
    private final String appId;
    private final String appSecuret;
    private final String domainUrl;
    private final String accountId;
    private final String tenantId;

    TokenGenerator(String appId, String appSecuret, String domainUrl, String accountId, String tenantId) {
        this.appId = appId;
        this.appSecuret = appSecuret;
        this.domainUrl = domainUrl;
        this.accountId = accountId;
        this.tenantId = tenantId;
    }

    public String cacheAccessToken(String phone) {
        return cacheAccessToken(phone, 300000L);
    }

    public String cacheAccessToken(String phone, long thresholdMillis) {
        String key = this.appId + phone;
        CosmicToken token = getRealTimeCachedToken(key);

        if (token == null || token.isMeetExpireThreshold(thresholdMillis) || token.isExpired()) {
            try (DLock lock = DLock.create(key)) {
                lock.lock();

                token = getRealTimeCachedToken(key);
                if (token == null || token.isMeetExpireThreshold(thresholdMillis) || token.isExpired()) {
                    Token newToken = this.newAccessToken(phone);
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

    private static CosmicToken getRealTimeCachedToken(String key) {
        return cache.get(key, CosmicToken.class);
    }

    @SneakyThrows
    public Token newAccessToken(String phone) {
        JSONObject json = new JSONObject(6);
        json.put("user", phone);
        json.put("apptoken", newAppToken().getContent());
        json.put("tenantid", tenantId);
        json.put("accountId", accountId);
        json.put("usertype", "Mobile");
        json.put("language", "zh_CN");

        RawRequest rawRequest = RawRequest.builder()
                .url(domainUrl + "/api/login.do")
                .method(Method.POST)
                .contentType(ContentType.APPLICATION_JSON)
                .reqString(json.toString())
                .build();

        JSONObject resp = SyncSingletonHttpSender.get()
                .sendRequest(rawRequest)
                .bodyToJson();

        return new CosmicToken(resp, "access_token");
    }

    @SneakyThrows
    public Token newAppToken() {
        JSONObject json = new JSONObject(5);
        json.put("appId", appId);
        json.put("appSecuret", appSecuret);
        json.put("tenantid", tenantId);
        json.put("accountId", accountId);
        json.put("language", "zh_CN");

        RawRequest rawRequest = RawRequest.builder()
                .url(domainUrl + "/api/getAppToken.do")
                .method(Method.POST)
                .contentType(ContentType.APPLICATION_JSON)
                .reqString(json.toString())
                .build();

        JSONObject resp = SyncSingletonHttpSender.get()
                .sendRequest(rawRequest)
                .bodyToJson();

        return new CosmicToken(resp, "app_token");
    }

    public static TokenGeneratorBuilder builder() {
        return new TokenGeneratorBuilder();
    }

    public static class TokenGeneratorBuilder {
        private String appId;
        private String appSecuret;
        private String domainUrl;
        private String accountId;
        private String tenantId;

        TokenGeneratorBuilder() {
        }

        public TokenGeneratorBuilder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public TokenGeneratorBuilder appSecuret(String appSecuret) {
            this.appSecuret = appSecuret;
            return this;
        }

        public TokenGeneratorBuilder domainUrl(String domainUrl) {
            this.domainUrl = domainUrl;
            return this;
        }

        public TokenGeneratorBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public TokenGeneratorBuilder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public TokenGenerator build() {
            if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecuret)) {
                throw new IllegalArgumentException("appId and appSecuret is required");
            }
            RequestContext ctx = RequestContext.get();
            if (domainUrl == null) {
                String path = ctx.getClientFullContextPath();
                domainUrl = path.substring(0, path.length() - 1);
            }
            if (tenantId == null) {
                tenantId = ctx.getTenantId();
            }
            if (accountId == null) {
                accountId = ctx.getAccountId();
            }
            return new TokenGenerator(this.appId, this.appSecuret, this.domainUrl, this.accountId, this.tenantId);
        }

        public String toString() {
            return "TokenGenerator.TokenGeneratorBuilder(appId=" + this.appId + ", appSecuret=" + this.appSecuret + ", domainUrl=" + this.domainUrl + ", accountId=" + this.accountId + ", tenantId=" + this.tenantId + ")";
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CosmicToken implements Token {
        private String content;
        private Long expireTime;

        public CosmicToken(JSONObject json, String type) {
            if (Boolean.TRUE.equals(json.getBoolean("status"))) {
                JSONObject data = (JSONObject) json.get("data");
                if (Boolean.TRUE.equals(data.getBoolean("success"))) {
                    this.content = data.getString(type);
                    this.expireTime = data.getLong("expire_time");
                } else {
                    throw new KDBizException(json.getString("message"));
                }
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