package kd.cd.webapi.apachehttp;

import kd.cd.webapi.core.RawSend;
import kd.cd.webapi.core.UrlencodedSend;

public interface HttpSyncSend<T> extends RawSend<T>, UrlencodedSend<T> {
}
