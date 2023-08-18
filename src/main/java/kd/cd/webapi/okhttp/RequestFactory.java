package kd.cd.webapi.okhttp;

import kd.cd.webapi.log.EventTracker;
import kd.cd.webapi.log.LogOption;
import kd.cd.webapi.req.ContentType;
import kd.cd.webapi.req.Method;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
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

    public static Request newRawRequest(Method method, ContentType contentType, String url, String reqString, Map<String, String> headerMap, LogOption logOption) {
        RequestBody body = RequestBody.create(reqString, MediaType.parse(contentType.getName()));
        return generate(method, contentType, url, body, headerMap, logOption);
    }

    public static Request newUrlencodedRequest(Method method, String url, @NotNull Map<String, String> reqMap, Map<String, String> headerMap, LogOption logOption) throws IOException {
        List<BasicNameValuePair> list = new ArrayList<>(reqMap.size());
        reqMap.forEach((key, value) -> list.add(new BasicNameValuePair(key, value)));

        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, StandardCharsets.UTF_8);
        InputStreamReader ir = new InputStreamReader(urlEncodedFormEntity.getContent(), StandardCharsets.UTF_8);

        try (BufferedReader br = new BufferedReader(ir)) {
            String line = br.readLine();
            line = line == null ? "" : line;
            String content = URLDecoder.decode(line, StandardCharsets.UTF_8.name());

            RequestBody body = RequestBody.create(content, MediaType.parse(ContentType.APPLICATION_URLENCODED.getName()));
            return generate(method, ContentType.APPLICATION_URLENCODED, url, body, headerMap, logOption);
        }
    }

    public static Request newFormDataRequest(Method method, String url, Map<String, String> reqMap, Map<String, String> headerMap, LogOption logOption) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, String> entry : reqMap.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        MultipartBody multipartBody = builder.build();
        return generate(method, ContentType.TEXT_PLAIN, url, multipartBody, headerMap, logOption);
    }

    public static Request newUploadFileRequest(Method method, String url, Map<String, File> fileMap, Map<String, String> headerMap, LogOption logOption) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, File> entry : fileMap.entrySet()) {
            File file = entry.getValue();
            RequestBody requestBody = RequestBody.create(file, MediaType.parse(ContentType.MUTIPART_FORMDATA.getName()));
            builder.addFormDataPart(entry.getKey(), file.getName(), requestBody);
        }
        MultipartBody multipartBody = builder.build();
        logOption.setRecordFullRequest(false);
        return generate(method, null, url, multipartBody, headerMap, logOption);
    }

    private static Request generate(Method method, ContentType contentType, String url, RequestBody reqBody, Map<String, String> headerMap, LogOption logOption) {
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
