package kd.cd.webapi.okhttp;

import kd.cd.webapi.core.FormDataSend;
import kd.cd.webapi.core.RawSend;
import kd.cd.webapi.core.UrlencodedSend;

public interface OkHttpSyncSend<T> extends FormDataSend<T>, RawSend<T>, UrlencodedSend<T> {
}
