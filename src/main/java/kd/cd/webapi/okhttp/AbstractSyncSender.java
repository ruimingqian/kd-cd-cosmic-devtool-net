package kd.cd.webapi.okhttp;

import kd.cd.webapi.req.FormDataRequest;
import kd.cd.webapi.req.RawRequest;
import kd.cd.webapi.req.UrlencodeRequest;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Function;

public abstract class AbstractSyncSender<T> implements SyncSend<T> {
    protected OkHttpClient client;

    @Override
    public T sendRaw(RawRequest rawRequest) throws IOException {
        Request req = RequestFactory.newRawRequest(rawRequest);
        return execSync(req, respHandelFunction());
    }

    @Override
    public T sendUrlencoded(UrlencodeRequest urlencodeRequest) throws IOException {
        Request req = RequestFactory.newUrlencodedRequest(urlencodeRequest);
        return execSync(req, respHandelFunction());
    }

    @Override
    public T sendFormData(FormDataRequest formDataRequest) throws IOException {
        Request req = RequestFactory.newFormDataRequest(formDataRequest);
        return execSync(req, respHandelFunction());
    }

    abstract Function<Response, T> respHandelFunction();

    T execSync(Request req, Function<Response, T> function) throws IOException {
        if (client == null) {
            client = defaultBuilder().build();
        }
        Call call = client.newCall(req);
        Response resp = call.execute();

        return function.apply(resp);
    }

    abstract OkHttpClient.Builder defaultBuilder();
}
