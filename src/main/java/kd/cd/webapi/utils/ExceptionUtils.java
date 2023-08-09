package kd.cd.webapi.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 异常处理通用
 *
 * @author qrm
 * @version 1.0
 */
public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static String getSimpleMsg(Throwable t) {
        return t == null ? "" : (t.getClass().getName() + ": " + t.getMessage());
    }

    public static List<String> getKeyTrace(Throwable t, @NotNull String key) {
        return t == null ? new ArrayList<>() : Arrays.stream(t.getStackTrace()).map(StackTraceElement::toString).filter(s -> s.replace(".", "").contains(key) || s.contains(key)).collect(Collectors.toList());
    }

    public static List<String> getFullTrace(Throwable t) {
        return t == null ? new ArrayList<>() : Arrays.stream(t.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList());
    }

    public static List<String> getFullTrace(Throwable t, int top) {
        return t == null ? new ArrayList<>() : Arrays.stream(t.getStackTrace()).map(StackTraceElement::toString).limit(top).collect(Collectors.toList());
    }

    public static String getKeyTraceString(Throwable t, String key) {
        return String.join("\n", getKeyTrace(t, key));
    }

    public static String getFullTraceString(Throwable t) {
        return String.join("\n", getFullTrace(t));
    }

    public static String getFullTraceString(Throwable t, int top) {
        return String.join("\n", getFullTrace(t, top));
    }
}
