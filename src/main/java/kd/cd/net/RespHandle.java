package kd.cd.net;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RespHandle<R> {
    R response();

    String bodyToString() throws IOException;

    ObjectNode bodyToJson() throws IOException;

    byte[] bodyToBytes() throws IOException;

    InputStream bodyToInputStream() throws IOException;

    void writeTo(OutputStream outputStream) throws IOException;

    boolean isSuccessful();
}
