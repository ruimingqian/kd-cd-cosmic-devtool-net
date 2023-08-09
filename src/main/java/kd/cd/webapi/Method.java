package kd.cd.webapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API Method
 *
 * @author qrm
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum Method {
    /**
     * POST
     */
    POST("POST"),

    /**
     * GET
     */
    GET("GET"),

    /**
     * COPY
     */
    COPY("COPY"),

    /**
     * PUT
     */
    PUT("PUT"),

    /**
     * DELETE
     */
    DELETE("DELETE");

    private final String name;
}
