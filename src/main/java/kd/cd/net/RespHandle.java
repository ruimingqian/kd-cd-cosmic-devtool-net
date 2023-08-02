package kd.cd.net;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RespHandle<R> {

    R response();

    String bodyToString() throws IOException, NullResponseException;

    ObjectNode bodyToJson() throws IOException, NullResponseException;

    byte[] bodyToBytes() throws IOException, NullResponseException;

    InputStream bodyToByteStream() throws NullResponseException;

    void writeTo(OutputStream outputStream) throws NullResponseException, IOException;

    boolean isSuccessful();
}
