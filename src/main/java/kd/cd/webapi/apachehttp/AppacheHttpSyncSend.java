package kd.cd.webapi.apachehttp;

import kd.cd.webapi.RawSend;
import kd.cd.webapi.UrlencodedSend;

public interface AppacheHttpSyncSend<T> extends RawSend<T>, UrlencodedSend<T> {
}
