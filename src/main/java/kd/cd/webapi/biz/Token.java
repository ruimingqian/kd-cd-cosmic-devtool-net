package kd.cd.webapi.biz;

public interface Token {

    boolean isMeetExpireThreshold(long expireThreshold);

    boolean isExpired();

    String getContent();
}
