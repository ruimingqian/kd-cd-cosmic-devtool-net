package kd.cd.webapi.okhttp;

import kd.cd.webapi.FormDataSend;
import kd.cd.webapi.RawSend;
import kd.cd.webapi.UrlencodedSend;

public interface OkHttpSyncSend<T> extends FormDataSend<T>, RawSend<T>, UrlencodedSend<T> {
}
