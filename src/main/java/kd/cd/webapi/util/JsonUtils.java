package kd.cd.webapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * JSON转换工具
 *
 * @author qrm
 * @version 1.0
 */
public final class JsonUtils {
    private JsonUtils() {
    }

    public static String format(String str) {
        try {
            return isValidJSON(str) ? executeFormat(str) : str;
        } catch (Exception e) {
            return str;
        }
    }

    public static String fuzzyFormat(String str) {
        try {
            if (isValidJSON(str)) {
                return executeFormat(str);
            }
            if (str.contains("{") && str.contains("}")) {
                int startIndex = str.indexOf("{");
                int endIndex = str.lastIndexOf("}");
                return str.substring(0, startIndex) + format(str.substring(startIndex, endIndex + 1)) + str.substring(endIndex + 1);
            } else {
                return str;
            }
        } catch (Exception e) {
            return str;
        }
    }

    private static String executeFormat(String str) {
        StringBuilder sb = new StringBuilder();
        int length = str.length();
        int number = 0;
        char key;
        for (int i = 0; i < length; i++) {

            key = str.charAt(i);
            if ((key == '[') || (key == '{')) {
                if ((i - 1 > 0) && (str.charAt(i - 1) == ':')) {
                    sb.append('\n').append(indent(number));
                }
                sb.append(key).append('\n');
                number++;
                sb.append(indent(number));
                continue;
            }

            if ((key == ']') || (key == '}')) {
                sb.append('\n');
                number--;
                sb.append(indent(number)).append(key);
                continue;
            }

            if ((key == ',')) {
                sb.append(key).append('\n').append(indent(number));
                continue;
            }
            sb.append(key);
        }
        return sb.toString();
    }

    private static String indent(int number) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < number; i++) {
            result.append("   ");
        }
        return result.toString();
    }

    public static boolean isValidJSON(String str) {
        boolean valid = true;
        try {
            JacksonUtils.getObjectMapper().readTree(str);
        } catch (JsonProcessingException e) {
            valid = false;
        }
        return valid;
    }
}