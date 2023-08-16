package kd.cd.webapi.okhttp;

import kd.cd.webapi.log.LogParam;
import kd.cd.webapi.util.SystemPropertyUtils;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * <b>OkHttp同步调用单例模式封装</b>
 * <p>
 * （1）OkHttp的单例模式封装，避免重复创建{@link OkHttpClient}对象，请勿通过反射破坏单例
 * <p>
 * （2）Socket读写超时时间均默认60s，可在MC系统公共参数项进行配置。如需设置自定义Client参数，请使用{@link OkHttpSyncSender}
 * <p>
 * （3）Request参数可通过{@link OkHttpRequestFactory}类生成
 * <p>
 * （4）传入日志参数{@link LogParam}可记录详细调用日志至系统日志表单
 * <p>
 * （5）调用结果对象{@link RespHandler }内置对{@link okhttp3.Response}的各种处理
 *
 * <p>
 * <b>示例</b>
 * <pre> {@code
 *    JSONObject json = OkHttpSingletonSyncSender.load()
 *                     .urlencodedPOST(url, reqMap, null, logParam)
 *                     .bodyToJson()
 * }</pre>
 *
 * @author qrm
 * @version 1.2
 * @see AbstractOkHttpSyncSender
 */
public class OkHttpSingletonSyncSender extends OkHttpSyncSender {
    protected static final int SINGLETON_CONNECTPOOL_SIZE = SystemPropertyUtils.getInt("okhttpclient.singleton.connectpoolsize", 32);
    protected static final long SINGLETON_KEEPALIVE_MINUTES = SystemPropertyUtils.getLong("okhttpclient.singleton.keepaliveminutes", 5L);

    private static volatile OkHttpSingletonSyncSender sender;

    private OkHttpSingletonSyncSender() {
        if (sender != null) {
            throw new IllegalStateException("No reflection allowed here");
        }
    }

    public static OkHttpSingletonSyncSender load() {
        if (sender == null) {
            synchronized (OkHttpSingletonSyncSender.class) {
                if (sender == null) {
                    sender = new OkHttpSingletonSyncSender();
                }
            }
        }
        return sender;
    }

    @Override
    OkHttpClient.Builder defaultBuilder() {
        OkHttpClient.Builder builder = super.defaultBuilder();
        //create a larger connection pool for sigleton instance
        ConnectionPool pool = new ConnectionPool(SINGLETON_CONNECTPOOL_SIZE, SINGLETON_KEEPALIVE_MINUTES, TimeUnit.MINUTES);
        builder.connectionPool(pool);
        return builder;
    }
}
