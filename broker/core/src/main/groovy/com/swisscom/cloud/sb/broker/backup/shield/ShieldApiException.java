package com.swisscom.cloud.sb.broker.backup.shield;

import java.util.StringJoiner;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.join;

public class ShieldApiException extends RuntimeException {
/*
    private enum ErrorInformation {
        INVALID_PARAMETERS(3000, "Can't %s %s: %s"),
        ERROR_LIST(3001, "Can't list %s %s: %s"),
        ERROR_GET(3002, "Can't get %s %s: %s"),
        ERROR_CREATE(3003, "Can't create %s %s: %s"),
        ERROR_DELETE(3004, "Can't delete %s %s: %s");

        private final long errorCode;
        private final String format;

        ErrorInformation(long errorCode, String format) {
            this.errorCode = errorCode;
            this.format = format;
        }

        public long getErrorCode() {
            return errorCode;
        }

        public String getFormat() {
            return format;
        }

        public String formatExceptionMessage(Class clazz, Object[] params, String reason) {
            return format(format,
                          clazz.getSimpleName(),
                          params == null || params.length == 0
                          ? ""
                          : new StringJoiner(" ", "with parameters:{", "}")
                                  .add(join(params, ", "))
                                  .toString(),
                          reason);
        }
    }

    private final ErrorInformation errorInformation;


    static ShieldApiException of(ErrorInformation errorInformation,
                              Class clazz,
                              Throwable throwable,
                              Object[] params) {
        return Match(throwable).of(
                Case($(instanceOf(Error.class)), e -> shieldApiException(errorInformation, clazz, e, params)),
                Case($(), e -> otherException(errorInformation, clazz, e, params))
        );
    }

    static ShieldApiException listShieldApiException(Class clazz, Throwable throwable, Object... params) {
        return of(ErrorInformation.ERROR_LIST, clazz, throwable, params);
    }*/
}
