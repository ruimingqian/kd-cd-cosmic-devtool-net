package kd.cd.webapi.log;

public interface Logger<T> {
    void logAsync(T t);

    void log(T t);
}
