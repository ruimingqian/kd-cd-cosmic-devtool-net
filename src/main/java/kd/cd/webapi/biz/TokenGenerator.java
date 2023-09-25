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
import kd.cd.webapi.util.SystemPropertyUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenGenerator {
    private static final IAppCache cache = AppCache.get("TOKEN");
    private static final Map<String, TokenGenerator> poolMap = new ConcurrentHashMap<>(64);
    private static final long INTERVAL_THRESHOLD = SystemPropertyUtils.getLong("accesstoken.cache.intervalthreshold", 60000L);
    private final String appId;
    private final String appSecuret;

    private TokenGenerator(String appId, String appSecuret) {
        this.appId = appId;
        this.appSecuret = appSecuret;
    }

    public static TokenGenerator of(String appId, String appSecuret) {
        return poolMap.computeIfAbsent(appId + appSecuret, k -> new TokenGenerator(appId, appSecuret));
    }

    public String cacheAccessToken(String phone) {
        String key = appId + phone;
        Token token = cache.get(key, Token.class);

        if (token == null) {
            Token newToken = newAccessToken(phone);
            cache.put(key, newToken);
            return newToken.getTokenText();
        } else {
            if (System.currentTimeMillis() > token.getExpireTime() + INTERVAL_THRESHOLD) {
                Token newToken = newAccessToken(phone);
                cache.put(key, newToken);
                return newToken.getTokenText();
            } else {
                return token.getTokenText();
            }
        }
    }

    public Token newAccessToken(String phone) {
        String domainUrl = RequestContext.get().getClientFullContextPath();
        return newAccessToken(domainUrl.substring(0, domainUrl.length() - 1), phone);
    }

    public Token newAccessToken(String domainUrl, String phone) {
        RequestContext ctx = RequestContext.get();
        String tenantId = ctx.getTenantId();
        String accountId = ctx.getAccountId();
        String appToken = newAppToken(domainUrl, tenantId, accountId).getTokenText();
        return newAccessToken(domainUrl, phone, appToken, tenantId, accountId);
    }

    @SneakyThrows
    private static Token newAccessToken(String domainUrl, String phone, String apptoken, String tenantid, String accountId) {
        JSONObject json = new JSONObject(5);
        json.put("user", phone);
        json.put("apptoken", apptoken);
        json.put("tenantid", tenantid);
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

    public Token newAppToken() {
        RequestContext ctx = RequestContext.get();
        String tenantId = ctx.getTenantId();
        String accountId = ctx.getAccountId();
        String path = ctx.getClientFullContextPath();
        return newAppToken(path.substring(0, path.length() - 1), tenantId, accountId);
    }

    @SneakyThrows
    public Token newAppToken(String domainUrl, String tenantid, String accountId) {
        JSONObject json = new JSONObject(5);
        json.put("appId", appId);
        json.put("appSecuret", appSecuret);
        json.put("tenantid", tenantid);
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

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Token {
        private String tokenText;
        private Long expireTime;

        public Token(JSONObject json, String type) {
            if ("success".equals(json.getString("state"))) {
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
    }
}