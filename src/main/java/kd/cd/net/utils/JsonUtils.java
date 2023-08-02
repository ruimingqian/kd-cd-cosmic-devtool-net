package kd.cd.net.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * JSON转换工具
 *
 * @author qrm
 * @version 1.0
 */
public final class JsonUtils {
    private static final SAXReader SAFE_SAX_READER = safeSaxReader();

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

    @SneakyThrows
    public static ObjectNode parseXMLFile(File file) {
        Document document = SAFE_SAX_READER.read(file);
        return parseXML(document);
    }

    @SneakyThrows
    public static ObjectNode parseXMLInputStream(InputStream in) {
        Document document = SAFE_SAX_READER.read(in);
        return parseXML(document);
    }

    @SneakyThrows
    public static ObjectNode parseXML(String xml) {
        Document document = DocumentHelper.parseText(xml);
        return parseXML(document);
    }

    @NotNull
    public static ObjectNode parseXML(Document document) {
        ObjectNode json = JacksonUtils.getObjectMapper().createObjectNode();
        Element root = document.getRootElement();
        iterNodes(root, json);
        return json;
    }

    private static void iterNodes(Element node, ObjectNode json) {
        String nodeName = node.getName();
        if (JacksonUtils.getKeySet(json).contains(nodeName)) {
            JsonNode o = json.get(nodeName);
            ArrayNode array;
            if (o instanceof ArrayNode) {
                array = (ArrayNode) o;
            } else {
                array = JacksonUtils.getObjectMapper().createArrayNode();
                array.add(o);
            }
            List<Element> listElement = node.elements();
            if (listElement.isEmpty()) {
                String nodeValue = node.getTextTrim();
                array.add(nodeValue);
                json.set(nodeName, array);
                return;
            }
            ObjectNode newJson = JacksonUtils.getObjectMapper().createObjectNode();
            for (Element e : listElement) {
                iterNodes(e, newJson);
            }
            array.add(newJson);
            json.set(nodeName, array);
            return;
        }
        List<Element> listElement = node.elements();
        if (listElement.isEmpty()) {
            String nodeValue = node.getTextTrim();
            json.put(nodeName, nodeValue);
            return;
        }
        ObjectNode object = JacksonUtils.getObjectMapper().createObjectNode();
        for (Element e : listElement) {
            iterNodes(e, object);
        }
        json.set(nodeName, object);
    }

    @SneakyThrows
    private static SAXReader safeSaxReader() {
        SAXReader saxReader = new SAXReader();
        saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return saxReader;
    }
}