package kd.cd.net.apachehttp;

import kd.cd.net.utils.SystemPropertyUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;

public class ApacheHttpSingletonSyncSender extends ApacheHttpSyncSender {
    protected static final int SINGLETON_MAX_CONNECTION = SystemPropertyUtils.getInt("apachehttpclient.singleton.maxconnection", 500);
    protected static final int SINGLETON_MAX_PER_ROUTE = SystemPropertyUtils.getInt("apachehttpclient.singleton.maxperroute", 50);

    private static volatile ApacheHttpSingletonSyncSender sender;

    private ApacheHttpSingletonSyncSender() {
        if (sender != null) {
            throw new IllegalStateException("No reflection allowed here");
        }
    }

    public static ApacheHttpSingletonSyncSender load() {
        if (sender == null) {
            synchronized (ApacheHttpSingletonSyncSender.class) {
                if (sender == null) {
                    sender = new ApacheHttpSingletonSyncSender();
                }
            }
        }
        return sender;
    }

    @Override
    public void close() {
        //since is a sigleton instance, you can't do this here
        throw new IllegalStateException("Do not support");
    }

    @Override
    HttpClientBuilder defaultBuilder() {
        HttpClientBuilder builder = super.defaultBuilder();
        ApacheHttpNetTuplesFactory af = ApacheHttpNetTuplesFactory.getInstance();
        //create a larger connection pool for sigleton instance
        HttpClientConnectionManager poolingManager = af.newPoolingManager(SINGLETON_MAX_CONNECTION, SINGLETON_MAX_PER_ROUTE);
        builder.setConnectionManager(poolingManager);
        return builder;
    }
}