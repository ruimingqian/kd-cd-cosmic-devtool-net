package kd.cd.net.internal;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <b>请求地址构造器</b>
 * <p>
 * <b>示例</b>
 * <pre> {@code
 *   String url = new UrlBuilder().doMain("http://xx.xx.com").port(8080).url("/api/doAction")
 *                   .param("beginDate", "2020-11-26")
 *                   .param("page", "2").build();
 * }</pre>
 *
 * @author qrm
 * @version 1.0
 * @see StringBuilder
 */
public class URLBuilder {
    private static final Pattern REGEX = Pattern.compile("&");
    private String url;

    public URLBuilder() {
    }

    public URLBuilder(String url) {
        this.url = url;
    }

    public <T extends Map<?, ?>> URLBuilder acceptParams(T t) {
        Set<?> keys = Optional.ofNullable(t).map(Map::keySet).orElseThrow(() -> new RuntimeException("null"));
        keys.forEach(k -> this.url = param(k.toString(), t.get(k)).url);
        return this;
    }

    public URLBuilder doMain(String doMainUrl) {
        return this.append(doMainUrl);
    }

    public URLBuilder port(int port) {
        if (port == 0 || StringUtils.isEmpty(String.valueOf(port))) {
            return this;
        }
        return append(":" + port);
    }

    public URLBuilder url(String url) {
        return this.append(url);
    }

    public URLBuilder param(String name, Object val) {
        val = Optional.ofNullable(val).orElse("");
        return append("&" + name + "=" + val);
    }

    public URLBuilder append(String val) {
        this.url = StringUtils.isEmpty(this.url) ? val : (this.url + val);
        return this;
    }

    public String build() {
        return REGEX.matcher(url).replaceFirst("?");
    }
}