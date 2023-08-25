package kd.cd.webapi.okhttp;

import com.alibaba.fastjson.JSONObject;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static kd.cd.webapi.util.OkHttpUtils.*;

public class ResponseHandler implements ResponseHandle<Response> {
    private final Response resp;

    public ResponseHandler(Response response) {
        this.resp = response;
    }

    @Override
    public Response response() {
        return resp;
    }

    @Override
    public JSONObject bodyToJson() throws IOException {
        return respBodyToJson(resp);
    }

    @Override
    public byte[] bodyToBytes() throws IOException {
        return respBodyToBytes(resp);
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        InputStream inputStream = bodyToInputStream();
        IOUtils.copy(inputStream, outputStream);
    }

    @Override
    public InputStream bodyToInputStream() {
        return respBodyToInputStream(resp);
    }

    @Override
    public String bodyToString() throws IOException {
        return respBodyToString(resp);
    }

    @Override
    public boolean isSuccessful() {
        return resp.isSuccessful();
    }
}