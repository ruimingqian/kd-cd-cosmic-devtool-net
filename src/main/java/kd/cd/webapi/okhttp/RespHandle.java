package kd.cd.webapi.okhttp;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RespHandle<R> {
    R response();

    String bodyToString() throws IOException;

    JSONObject bodyToJson() throws IOException;

    <T> T bodyToBean(Class<T> beanClass) throws IOException;

    byte[] bodyToBytes() throws IOException;

    InputStream bodyToInputStream() throws IOException;

    void writeTo(OutputStream outputStream) throws IOException;

    boolean isSuccessful();
}
