package kd.cd.webapi.apachehttp;

import kd.cd.webapi.core.RawTextSend;
import kd.cd.webapi.core.UrlencodedSend;

public interface HttpSyncSend<T> extends RawTextSend<T>, UrlencodedSend<T> {
}
