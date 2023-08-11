package kd.cd.webapi.okhttp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.webapi.core.RespHandle;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static kd.cd.webapi.okhttp.OkHttpUtils.*;

public class RespHandler implements RespHandle<Response> {
    private final Response resp;

    public RespHandler(Response response) {
        this.resp = response;
    }

    public Response response() {
        return resp;
    }

    public ObjectNode bodyToJson() throws IOException {
        return respBodyToJson(resp);
    }

    public byte[] bodyToBytes() throws IOException {
        return respBodyToBytes(resp);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        InputStream inputStream = bodyToInputStream();
        IOUtils.copy(inputStream, outputStream);
    }

    public InputStream bodyToInputStream() {
        return respBodyToInputStream(resp);
    }

    public String bodyToString() throws IOException {
        return respBodyToString(resp);
    }

    public boolean isSuccessful() {
        return resp.isSuccessful();
    }
}