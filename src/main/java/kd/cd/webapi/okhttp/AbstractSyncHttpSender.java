package kd.cd.webapi.okhttp;

import kd.cd.webapi.req.BaseRequest;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Function;

public abstract class AbstractSyncHttpSender<T> implements SyncHttpSend<T> {
    protected OkHttpClient client;

    public T sendRequest(BaseRequest baseRequest) throws IOException {
        return syncCall(baseRequest.convert(), respHandelFunction());
    }

    abstract Function<Response, T> respHandelFunction();

    T syncCall(Request req, Function<Response, T> function) throws IOException {
        if (client == null) {
            client = defaultBuilder().build();
        }
        Call call = client.newCall(req);
        Response resp = call.execute();

        return function.apply(resp);
    }

    abstract OkHttpClient.Builder defaultBuilder();
}
