package kd.cd.webapi.apachehttp;

import kd.cd.webapi.ContentType;
import kd.cd.webapi.Method;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApacheHttpRequestFactory {
    private ApacheHttpRequestFactory() {
    }

    public static HttpRequestBase newRawRequest(Method method, ContentType contentType, String url, String reqString, Map<String, String> headerMap) {

        if (Method.POST == method) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", contentType.getName());
            httpPost.setHeader("Content-Type", contentType.getName());
            if (headerMap != null) {
                headerMap.forEach(httpPost::addHeader);
            }
            httpPost.setEntity(new StringEntity(reqString, StandardCharsets.UTF_8));

            return httpPost;

        } else if (Method.GET == method) {

            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", contentType.getName());
            httpGet.setHeader("Content-Type", contentType.getName());
            if (headerMap != null) {
                headerMap.forEach(httpGet::addHeader);
            }

            return httpGet;
        } else {
            throw new IllegalStateException("UnSupported yet");
        }
    }

    public static HttpRequestBase newUrlencodedRequest(Method method, String url, @NotNull Map<String, String> reqMap, Map<String, String> headerMap) {

        if (Method.POST == method) {
            HttpPost httpPost = new HttpPost(url);

            List<BasicNameValuePair> list = new ArrayList<>(reqMap.size());
            reqMap.forEach((key, value) -> list.add(new BasicNameValuePair(key, value)));
            if (headerMap != null) {
                headerMap.forEach(httpPost::addHeader);
            }
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, StandardCharsets.UTF_8);
            httpPost.setEntity(urlEncodedFormEntity);

            return httpPost;
        } else {
            throw new IllegalStateException("UnSupported yet");
        }
    }
}
