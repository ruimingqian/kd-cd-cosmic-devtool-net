package kd.cd.webapi.okhttp;

import kd.cd.webapi.req.FormDataRequest;
import kd.cd.webapi.req.RawRequest;
import kd.cd.webapi.req.UrlencodeRequest;

import java.io.IOException;

public interface SyncHttpSend<T> {
    T sendFormData(FormDataRequest formDataRequest) throws IOException;

    T sendRaw(RawRequest rawRequest) throws IOException;

    T sendUrlencoded(UrlencodeRequest urlencodeRequest) throws IOException;
}
