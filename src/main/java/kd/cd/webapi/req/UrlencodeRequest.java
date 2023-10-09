package kd.cd.webapi.req;

import kd.cd.webapi.log.LogOption;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class UrlencodeRequest extends AbstractBaseRequest {
    private final Map<String, String> reqMap;

    private UrlencodeRequest(UrlencodeRequest.Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.reqMap = builder.reqMap;
        this.headers = builder.headers;
        this.logOption = builder.logOption;
    }

    public static UrlencodeRequest.Builder builder() {
        return new UrlencodeRequest.Builder();
    }

    @Override
    public Request convert() throws IOException {
        List<BasicNameValuePair> list = new ArrayList<>(reqMap.size());
        reqMap.forEach((key, value) -> list.add(new BasicNameValuePair(key, value)));

        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, StandardCharsets.UTF_8);
        InputStreamReader ir = new InputStreamReader(urlEncodedFormEntity.getContent(), StandardCharsets.UTF_8);

        try (BufferedReader br = new BufferedReader(ir)) {
            String line = br.readLine();
            line = line == null ? "" : line;
            String content = URLDecoder.decode(line, StandardCharsets.UTF_8.name());

            RequestBody body = (method == Method.GET) ?
                    null :
                    RequestBody.create(content, MediaType.parse(ContentType.APPLICATION_URLENCODED.getName()));
            return generateOkHttpRequest(body, ContentType.APPLICATION_URLENCODED);
        }
    }

    public static class Builder {
        private String url;
        private Method method = Method.GET;
        private Map<String, String> reqMap;
        private Map<String, String> headers;
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
            if (this.headers == null) {
                this.headers = new HashMap<>(4);
            }
            this.headers.put(key, value);
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
