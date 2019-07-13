package com.swisscom.cloud.sb.broker.backup.shield;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.StringJoiner;

public class ShieldApiException extends RuntimeException {
    private final String description;
    private final HttpStatus httpStatus;
    private final String[] arguments;

    private ShieldApiException(String description, Throwable ex, String... arguments) {
        super(description, ex);
        this.description = description;
        this.arguments = arguments;
        httpStatus = (ex instanceof HttpStatusCodeException) ? ((HttpStatusCodeException) ex).getStatusCode() : null;
    }

    public static ShieldApiException of(String description, Throwable ex, String... arguments) {
        return new ShieldApiException(description, ex, arguments);
    }

    public boolean isNotFound() {
        return isHttpStatusCodeException() && httpStatus.value() == 404;
    }

    public String toString() {
        return String.format("ShieldApiException[%s %s %s: %s]",
                             isHttpStatusCodeException() ? httpStatus.value() : "-",
                             arguments == null || arguments.length == 0
                             ? "-"
                             : new StringJoiner(" ", "with parameters:{", "}")
                                     .add(String.join(",", arguments)),
                             description != null ? description : "no description",
                             getCause() != null ? getCause().getMessage() : "no cause");
    }


    private boolean isHttpStatusCodeException() {
        return httpStatus != null;
    }
}
