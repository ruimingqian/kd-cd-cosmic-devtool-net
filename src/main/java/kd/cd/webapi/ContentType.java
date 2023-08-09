package kd.cd.webapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ContentType
 *
 * @author qrm
 * @version 1.0
 */
@AllArgsConstructor
@Getter
public enum ContentType {
    /**
     * application/x-www-form-urlencoded
     */
    APPLICATION_URLENCODED("application/x-www-form-urlencoded"),

    /**
     * application/json
     */
    APPLICATION_JSON("application/json"),

    /**
     * application/xml
     */
    APPLICATION_XML("application/xml"),

    /**
     * application/javascript
     */
    APPLICATION_JAVASCRIPT("application/javascript"),

    /**
     * multipart/form-data
     */
    MUTIPART_FORMDATA("multipart/form-data"),

    /**
     * text/plain
     */
    TEXT_PLAIN("text/plain"),

    /**
     * text/html
     */
    TEXT_HTML("text/html");

    private final String name;
}
