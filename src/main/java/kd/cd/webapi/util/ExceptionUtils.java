package kd.cd.webapi.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static String getSimpleMsg(Throwable t) {
        return t == null ? "" : (t.getClass().getName() + ": " + t.getMessage());
    }

    public static List<String> getKeyTrace(Throwable t, @NotNull String key) {
        return t == null ?
                Collections.emptyList() :
                Arrays.stream(t.getStackTrace())
                        .map(StackTraceElement::toString)
                        .filter(s -> s.replace(".", "").contains(key) || s.contains(key))
                        .collect(Collectors.toList());
    }

    public static List<String> getFullTrace(Throwable t) {
        return t == null ?
                Collections.emptyList() :
                Arrays.stream(t.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.toList());
    }

    public static List<String> getFullTrace(Throwable t, int top) {
        return t == null ?
                Collections.emptyList() :
                Arrays.stream(t.getStackTrace())
                        .map(StackTraceElement::toString)
                        .limit(top)
                        .collect(Collectors.toList());
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