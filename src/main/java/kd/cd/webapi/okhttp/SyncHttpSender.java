package kd.cd.webapi.okhttp;

import kd.cd.webapi.util.OkHttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.function.Function;

public class SyncHttpSender extends AbstractSyncHttpSender<RespHandle<Response>> {
    SyncHttpSender() {
    }

    private SyncHttpSender(OkHttpClient client) {
        this.client = client;
    }

    public static SyncHttpSender of(OkHttpClient client) {
        return new SyncHttpSender(client);
    }

    @Override
    Function<Response, RespHandle<Response>> respHandelFunction() {
        return ResponseHandler::new;
    }

    @Override
    OkHttpClient.Builder defaultBuilder() {
        return OkHttpUtils.newDefaultCustomizedBuilder();
    }
}
