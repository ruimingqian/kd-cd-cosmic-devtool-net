package kd.cd.webapi.req;

import kd.cd.webapi.log.LogOption;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RawRequest extends AbstractBaseRequest {
    private final String reqString;

    private RawRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.contentType = builder.contentType;
        this.reqString = builder.reqString;
        this.headers = builder.headers;
        this.logOption = builder.logOption;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Request convert() {
        RequestBody body = (method == Method.GET) ?
                null :
                RequestBody.create(reqString, MediaType.parse(contentType.getName()));
        return generateOkHttpRequest(body, contentType);
    }

    public static class Builder {
        private String url;
        private Method method = Method.GET;
        private ContentType contentType = ContentType.APPLICATION_JSON;
        private String reqString = "";
        private Map<String, String> headers;
        private LogOption logOption;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder addHeader(String key, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>(4);
            }
            this.headers.put(key, value);
            return this;
        }

        public Builder logOption(LogOption logOption) {
            this.logOption = logOption == null ? null : logOption.clone();
            return this;
        }

        public Builder reqString(String reqString) {
            this.reqString = reqString;
            return this;
        }

        public RawRequest build() {
            return new RawRequest(this);
        }
    }
}
