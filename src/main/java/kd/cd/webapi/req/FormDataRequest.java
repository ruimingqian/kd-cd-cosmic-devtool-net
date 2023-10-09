package kd.cd.webapi.req;

import kd.cd.webapi.log.LogOption;
import lombok.Getter;
import okhttp3.MultipartBody;
import okhttp3.Request;

import java.util.HashMap;
import java.util.Map;

@Getter
public class FormDataRequest extends AbstractBaseRequest {
    private final Map<String, String> reqMap;

    private FormDataRequest(FormDataRequest.Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.reqMap = builder.reqMap;
        this.headers = builder.headers;
        this.logOption = builder.logOption;
    }

    public static FormDataRequest.Builder builder() {
        return new FormDataRequest.Builder();
    }

    @Override
    public Request convert() {
        MultipartBody.Builder builder = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM);

        for (Map.Entry<String, String> entry : reqMap.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        MultipartBody multipartBody = builder.build();

        return generateOkHttpRequest(multipartBody, ContentType.TEXT_PLAIN);
    }

    public static class Builder {
        private String url;
        private Method method = Method.GET;
        private Map<String, String> reqMap;
        private Map<String, String> headers;
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
            if (this.headers == null) {
                this.headers = new HashMap<>(4);
            }
            this.headers.put(key, value);
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
