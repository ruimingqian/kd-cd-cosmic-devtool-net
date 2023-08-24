package kd.cd.webapi.okhttp;

import kd.cd.webapi.util.OkHttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.function.Function;

public class SyncSender extends AbstractSyncSender<RespHandle<Response>> {
    SyncSender() {
    }

    private SyncSender(OkHttpClient client) {
        this.client = client;
    }

    public static SyncSender of(OkHttpClient client) {
        return new SyncSender(client);
    }

    @Override
    Function<Response, RespHandle<Response>> respHandelFunction() {
        return RespHandler::new;
    }

    @Override
    OkHttpClient.Builder defaultBuilder() {
        return OkHttpUtils.newDefaultCustomizedBuilder();
    }
}
