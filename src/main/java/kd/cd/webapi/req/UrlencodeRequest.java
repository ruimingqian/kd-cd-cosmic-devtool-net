package kd.cd.webapi.req;

import kd.cd.webapi.log.LogOption;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class UrlencodeRequest extends RequestBase {
    private final Map<String, String> reqMap;

    private UrlencodeRequest(UrlencodeRequest.Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.reqMap = builder.reqMap;
        this.headerMap = builder.headerMap;
        this.logOption = builder.logOption;
    }

    public static UrlencodeRequest.Builder builder() {
        return new UrlencodeRequest.Builder();
    }

    public static class Builder {
        private String url;
        private Method method = Method.GET;
        private Map<String, String> reqMap;
        private Map<String, String> headerMap;
        private LogOption logOption;

        public UrlencodeRequest.Builder url(String url) {
            this.url = url;
            return this;
        }

        public UrlencodeRequest.Builder method(Method method) {
            this.method = method;
            return this;
        }

        public UrlencodeRequest.Builder reqMap(Map<String, String> reqMap) {
            this.reqMap = reqMap;
            return this;
        }

        public UrlencodeRequest.Builder addHeader(String key, String value) {
            if (this.headerMap == null) {
                this.headerMap = new HashMap<>(4);
            }
            this.headerMap.put(key, value);
            return this;
        }

        public UrlencodeRequest.Builder logOption(LogOption logOption) {
            this.logOption = logOption == null ? null : logOption.clone();
            return this;
        }

        public UrlencodeRequest build() {
            if (this.reqMap == null || reqMap.isEmpty()) {
                throw new IllegalArgumentException("reqMap is null or empty");
            }
            return new UrlencodeRequest(this);
        }
    }
}
