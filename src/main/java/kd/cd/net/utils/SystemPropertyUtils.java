package kd.cd.net.utils;

import javax.validation.constraints.NotNull;

public final class SystemPropertyUtils {
    private SystemPropertyUtils() {
    }

    public static String getString(String nm, String def) {
        return System.getProperty(nm, String.valueOf(def));
    }

    public static boolean getBoolean(String nm, boolean def) {
        return Boolean.parseBoolean(System.getProperty(nm, String.valueOf(def)));
    }

    public static long getLong(String nm, @NotNull Long def) {
        return getLong(nm, def, null, null);
    }

    public static long getLong(String nm, @NotNull Long def, Long min, Long max) {
        Long value;
        value = Long.getLong(nm, def);
        if (min != null) {
            value = Math.max(value, min);
        }
        if (max != null) {
            value = Math.min(value, max);
        }
        return value;
    }

    public static int getInt(String nm, @NotNull Integer def) {
        return getInt(nm, def, null, null);
    }

    public static int getInt(String nm, @NotNull Integer def, Integer min, Integer max) {
        int value;
        value = Integer.getInteger(nm, def);
        if (min != null) {
            value = Math.max(value, min);
        }
        if (max != null) {
            value = Math.min(value, max);
        }
        return value;
    }
}
