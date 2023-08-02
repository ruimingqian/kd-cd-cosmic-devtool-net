import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.net.NullResponseException;
import kd.cd.net.internal.RequestConfCache;
import kd.cd.net.okhttp.OkHttpSingletonSyncSender;

import java.io.IOException;

public class test {
    public static void main(String[] args) {

        RequestConfCache conf = RequestConfCache.of("jsnnda");
        if (conf.isNotConfigured()) {
            throw new IllegalStateException("");
        }

        try {
            ObjectNode jsonNodes = OkHttpSingletonSyncSender.load()
                    .get("", "", null, null)
                    .bodyToJson();
        } catch (IOException | NullResponseException e) {
            throw new RuntimeException(e);
        }
    }
}
