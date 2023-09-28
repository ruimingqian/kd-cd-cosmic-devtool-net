package kd.cd.webapi.biz;

import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.exception.KDBizException;
import kd.cd.webapi.okhttp.SyncSingletonHttpSender;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;
import kd.cd.webapi.req.RawRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class TokenGenerator {
    private static final IAppCache cache = AppCache.get("TOKEN");
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

    public String recacheAccessToken(String phone) {
        clearCache(phone);
        return cacheAccessToken(phone);
    }

    public String cacheAccessToken(String phone) {
        String key = this.appId + phone;
        Token token = cache.get(key, Token.class);
        if (token == null || token.isExpired()) {
            Token newToken = this.newAccessToken(phone);
            cache.put(key, newToken);
            return newToken.getTokenText();
        } else {
            return token.getTokenText();
        }
    }

    public void clearCache(String phone) {
        cache.remove(this.appId + phone);
    }

    @SneakyThrows
    public Token newAccessToken(String phone) {
        JSONObject json = new JSONObject(6);
        json.put("user", phone);
        json.put("apptoken", newAppToken().getTokenText());
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

        JSONObject resp = SyncSingletonHttpSender.require()
                .sendRaw(rawRequest)
                .bodyToJson();

        return new Token(resp, "access_token");
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

        JSONObject resp = SyncSingletonHttpSender.require()
                .sendRaw(rawRequest)
                .bodyToJson();

        return new Token(resp, "app_token");
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
            init();
            return new TokenGenerator(this.appId, this.appSecuret, this.domainUrl, this.accountId, this.tenantId);
        }

        private void init() {
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
        }

        public String toString() {
            return "TokenGenerator.TokenGeneratorBuilder(appId=" + this.appId + ", appSecuret=" + this.appSecuret + ", domainUrl=" + this.domainUrl + ", accountId=" + this.accountId + ", tenantId=" + this.tenantId + ")";
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Token {
        private String tokenText;
        private Long expireTime;

        public Token(JSONObject json, String type) {
            if ("success".equalsIgnoreCase(json.getString("state"))) {
                JSONObject data = (JSONObject) json.get("data");
                if ("0".equals(data.get("error_code"))) {
                    this.tokenText = data.getString(type);
                    this.expireTime = data.getLong("expire_time");
                } else {
                    throw new KDBizException(json.getString("message"));
                }
            } else {
                throw new KDBizException(json.getString("message"));
            }
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expireTime;
        }
    }
}