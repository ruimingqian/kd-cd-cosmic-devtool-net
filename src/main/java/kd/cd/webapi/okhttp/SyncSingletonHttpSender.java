package kd.cd.webapi.okhttp;

import kd.cd.webapi.log.LogOption;
import kd.cd.webapi.util.SystemPropertyUtils;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * <b>OkHttp同步调用单例模式封装</b>
 * <p>
 * （1）OkHttp的单例模式封装，避免重复创建{@link OkHttpClient}对象，请勿通过反射破坏单例
 * <p>
 * （2）Socket读写超时时间均默认60s，可在MC系统公共参数项进行配置。如需设置自定义Client参数，请使用{@link SyncHttpSender}
 * <p>
 * （3）传入日志参数{@link LogOption}可记录详细调用日志至系统日志表单
 * <p>
 * （4）调用结果对象{@link ResponseHandler }内置对{@link okhttp3.Response}的各种处理
 *
 * <p>
 * <b>示例</b>
 * <pre> {@code
 *         RawRequest rawRequest = RawRequest.builder()
 *                 .method(Method.POST)
 *                 .contentType(ContentType.APPLICATION_JSON)
 *                 .url("http://localhost...")
 *                 .reqString(reqString)
 *                 .logOption(cfg.logOption())
 *                 .build();
 *
 *         try {
 *             String s = SyncSingletonHttpSender.get()
 *                     .sendRequest(rawRequest)
 *                     .bodyToString();
 *         } catch (IOException e) {
 *             throw new RuntimeException(e);
 *         }
 * }</pre>
 *
 * @author qrm
 * @version 1.3
 * @see AbstractSyncHttpSender
 */
public class SyncSingletonHttpSender extends SyncHttpSender {
    protected static final int SINGLETON_CONNECTPOOL_SIZE = SystemPropertyUtils.getInt("okhttpclient.singleton.connectpoolsize", 32);
    protected static final long SINGLETON_KEEPALIVE_MINUTES = SystemPropertyUtils.getLong("okhttpclient.singleton.keepaliveminutes", 5L);

    private static volatile SyncSingletonHttpSender sender;

    private SyncSingletonHttpSender() {
        if (sender != null) {
            throw new IllegalStateException("No reflection allowed here");
        }
    }

    public static SyncSingletonHttpSender get() {
        if (sender == null) {
            synchronized (SyncSingletonHttpSender.class) {
                if (sender == null) {
                    sender = new SyncSingletonHttpSender();
                }
            }
        }
        return sender;
    }

    @Override
    OkHttpClient.Builder defaultClientBuilder() {
        OkHttpClient.Builder builder = super.defaultClientBuilder();
        //create a larger connection pool for sigleton instance
        ConnectionPool pool = new ConnectionPool(SINGLETON_CONNECTPOOL_SIZE, SINGLETON_KEEPALIVE_MINUTES, TimeUnit.MINUTES);
        builder.connectionPool(pool);
        return builder;
    }
}
