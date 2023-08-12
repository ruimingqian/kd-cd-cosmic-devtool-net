package kd.cd.webapi.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.webapi.exception.IllegalResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RespHandle<R> {
    R response();

    String bodyToString() throws IOException, IllegalResponseException;

    ObjectNode bodyToJson() throws IOException, IllegalResponseException;

    byte[] bodyToBytes() throws IOException, IllegalResponseException;

    InputStream bodyToInputStream() throws IOException, IllegalResponseException;

    void writeTo(OutputStream outputStream) throws IOException, IllegalResponseException;

    boolean isSuccessful();
}
