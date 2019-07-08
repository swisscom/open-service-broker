package com.swisscom.cloud.sb.broker.backup.shield;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class ShieldApiException extends RuntimeException {
    private final String description;
    private final HttpStatus httpStatus;

    private ShieldApiException(String description, Throwable ex) {
        super(description, ex);
        this.description = description;
        httpStatus = (ex instanceof HttpStatusCodeException) ? ((HttpStatusCodeException) ex).getStatusCode(): null;
    }

    public static ShieldApiException of(String description, Throwable ex) {
        return new ShieldApiException(description, ex);
    }

    public boolean isHttpStatusCodeException() {
        return httpStatus != null;
    }

    public boolean isNotFound() {
        return isHttpStatusCodeException() && (httpStatus.value() == 404) || httpStatus.value() == 501;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ShieldApiException");
        if (isHttpStatusCodeException()) sb.append(String.format(" with HTTP status code %d. ", httpStatus.value()));
        sb.append(description);
        return sb.toString();
    }


}
