package kd.cd.webapi.okhttp;

import kd.cd.webapi.log.EventTracker;
import kd.cd.webapi.log.LogOption;
import kd.cd.webapi.req.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
import java.util.List;
import java.util.Map;

/**
 * OkHttp 请求工厂
 *
 * @author qrm
 * @version 1.0
 * @see Request
 * @see Method
 * @see ContentType
 */
public final class RequestFactory {
    private RequestFactory() {
    }

    public static Request newRawRequest(RawRequest rawReq) {
        String reqString = rawReq.getReqString();
        ContentType contentType = rawReq.getContentType();

        RequestBody body = RequestBody.create(reqString, MediaType.parse(contentType.getName()));
        return generate(body, contentType, rawReq);
    }

    public static Request newUrlencodedRequest(UrlencodeRequest urlencodeRequest) throws IOException {
        Map<String, String> reqMap = urlencodeRequest.getReqMap();

        List<BasicNameValuePair> list = new ArrayList<>(reqMap.size());
        reqMap.forEach((key, value) -> list.add(new BasicNameValuePair(key, value)));

        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, StandardCharsets.UTF_8);
        InputStreamReader ir = new InputStreamReader(urlEncodedFormEntity.getContent(), StandardCharsets.UTF_8);

        try (BufferedReader br = new BufferedReader(ir)) {
            String line = br.readLine();
            line = line == null ? "" : line;
            String content = URLDecoder.decode(line, StandardCharsets.UTF_8.name());

            RequestBody body = RequestBody.create(content, MediaType.parse(ContentType.APPLICATION_URLENCODED.getName()));
            return generate(body, ContentType.APPLICATION_URLENCODED, urlencodeRequest);
        }
    }

    public static Request newFormDataRequest(FormDataRequest formDataRequest) {
        Map<String, String> reqMap = formDataRequest.getReqMap();

        MultipartBody.Builder builder = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM);

        for (Map.Entry<String, String> entry : reqMap.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        MultipartBody multipartBody = builder.build();

        return generate(multipartBody, ContentType.TEXT_PLAIN, formDataRequest);
    }

    private static Request generate(RequestBody reqBody, ContentType contentType, RequestBase base) {
        Method method = base.getMethod();
        String url = base.getUrl();
        Map<String, String> headerMap = base.getHeaderMap();
        LogOption logOption = base.getLogOption();

        Request.Builder reqBuilder = new Request.Builder()
                .url(url)
                .method(method.getName(), reqBody);

        if (logOption != null) {
            EventTracker tracker = new EventTracker();
            tracker.setLogOption(logOption);
            reqBuilder.tag(EventTracker.class, tracker);
        }
        if (contentType != null) {
            reqBuilder.addHeader("Content-Type", contentType.getName());
        }
        if (headerMap != null) {
            headerMap.forEach(reqBuilder::addHeader);
        }
        return reqBuilder.build();
    }
}
