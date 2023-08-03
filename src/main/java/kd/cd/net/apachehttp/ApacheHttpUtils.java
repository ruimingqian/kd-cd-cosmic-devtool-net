package kd.cd.net.apachehttp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.net.FailedResponseException;
import kd.cd.net.NullResponseException;
import kd.cd.net.utils.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ApacheHttpUtils {
    private ApacheHttpUtils() {
    }

    public static ObjectNode bodyToJson(CloseableHttpResponse resp) throws IOException {
        String bodyString = bodyToString(resp);
        return StringUtils.isEmpty(bodyString) ? null : (ObjectNode) JacksonUtils.getObjectMapper().readTree(bodyString);
    }

    public static String bodyToString(CloseableHttpResponse resp) throws IOException {
        checkResp(resp);
        return EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
    }

    public static byte[] bodyToBytes(CloseableHttpResponse resp) throws IOException {
        checkResp(resp);
        return EntityUtils.toByteArray(resp.getEntity());
    }

    public static InputStream bodyToStream(CloseableHttpResponse resp) throws IOException {
        try {
            checkResp(resp);
            return resp.getEntity().getContent();
        } finally {
            closeResp(resp);
        }
    }

    private static void checkResp(CloseableHttpResponse resp) {
        if (resp == null) {
            throw new NullResponseException();
        }
        HttpEntity respEntity = resp.getEntity();
        if (respEntity == null) {
            closeResp(resp);
            throw new NullResponseException("Null CloseableHttpResponse.Entity");
        }
        if (!isSuccessful(resp)) {
            closeResp(resp);
            throw new FailedResponseException("http request fail:" + resp.getStatusLine().getReasonPhrase());
        }
    }

    public static void closeResp(CloseableHttpResponse resp) {
        try {
            if (resp != null) {
                resp.close();
            }
        } catch (IOException e) {
            //Ignore
        }
    }

    public static boolean isSuccessful(CloseableHttpResponse resp) {
        if (resp == null) {
            return false;
        }
        return resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }
}
