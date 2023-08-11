package kd.cd.webapi.apachehttp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.webapi.core.RespHandle;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static kd.cd.webapi.apachehttp.HttpUtils.*;

public class RespHandler implements RespHandle<CloseableHttpResponse> {
    private final CloseableHttpResponse resp;

    public RespHandler(CloseableHttpResponse response) {
        this.resp = response;
    }

    @Override
    public CloseableHttpResponse response() {
        return resp;
    }

    @Override
    public String bodyToString() throws IOException {
        return respToString(resp);
    }

    @Override
    public ObjectNode bodyToJson() throws IOException {
        return respToJson(resp);
    }

    @Override
    public byte[] bodyToBytes() throws IOException {
        return respToBytes(resp);
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        InputStream inputStream = bodyToInputStream();
        IOUtils.copy(inputStream, outputStream);
    }

    @Override
    public InputStream bodyToInputStream() throws IOException {
        return respToInputStream(resp);
    }

    @Override
    public boolean isSuccessful() {
        StatusLine status = resp.getStatusLine();
        return status.getStatusCode() == HttpStatus.SC_OK;
    }
}