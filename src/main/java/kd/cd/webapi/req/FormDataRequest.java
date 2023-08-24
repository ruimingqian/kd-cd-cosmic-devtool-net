package kd.cd.webapi.req;

import kd.cd.webapi.log.LogOption;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class FormDataRequest extends RequestBase {
    private final Map<String, String> reqMap;

    private FormDataRequest(FormDataRequest.Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.reqMap = builder.reqMap;
        this.headerMap = builder.headerMap;
        this.logOption = builder.logOption;
    }

    public static FormDataRequest.Builder builder() {
        return new FormDataRequest.Builder();
    }

    public static class Builder {
        private String url;
        private Method method = Method.GET;
        private Map<String, String> reqMap;
        private Map<String, String> headerMap;
        private LogOption logOption;

        public FormDataRequest.Builder url(String url) {
            this.url = url;
            return this;
        }

        public FormDataRequest.Builder method(Method method) {
            this.method = method;
            return this;
        }

        public FormDataRequest.Builder reqMap(Map<String, String> reqMap) {
            this.reqMap = reqMap;
            return this;
        }

        public FormDataRequest.Builder addHeader(String key, String value) {
            if (this.headerMap == null) {
                this.headerMap = new HashMap<>(4);
            }
            this.headerMap.put(key, value);
            return this;
        }

        public FormDataRequest.Builder logOption(LogOption logOption) {
            this.logOption = logOption == null ? null : logOption.clone();
            return this;
        }

        public FormDataRequest build() {
            if (this.reqMap == null || reqMap.isEmpty()) {
                throw new IllegalArgumentException("reqMap is null or empty");
            }
            return new FormDataRequest(this);
        }
    }
}