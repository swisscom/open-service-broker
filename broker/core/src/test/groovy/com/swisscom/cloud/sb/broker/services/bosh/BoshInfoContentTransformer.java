package com.swisscom.cloud.sb.broker.services.bosh;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoshInfoContentTransformer extends ResponseTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(BoshInfoContentTransformer.class);

    private final String originalUaaUrl;
    private final String replacementUaaUrl;
    private final String name;

    private BoshInfoContentTransformer(String originalUaaUrl, String replacementUaaUrl, String name) {
        this.originalUaaUrl = originalUaaUrl;
        this.replacementUaaUrl = replacementUaaUrl;
        this.name = name;
    }

    public static BoshInfoContentTransformer of(String originalUaaUrl, String replacementUaaUrl, String name) {
        return new BoshInfoContentTransformer(originalUaaUrl, replacementUaaUrl, name);
    }

    @Override
    public Response transform(Request request,
                              Response responseDefinition,
                              FileSource files,
                              Parameters parameters) {
        if (request.getAbsoluteUrl().endsWith("/info")) {
            LOG.debug("transforming for url: " + request.getAbsoluteUrl());
            String body = new String(responseDefinition.getBody());
            return Response.Builder.like(responseDefinition)
                                   .but()
                                   .body(body.replaceAll('"' + originalUaaUrl + '"',
                                                         '"' + replacementUaaUrl + '"'))
                                   .build();
        }
        return responseDefinition;
    }

    @Override
    public String getName() {
        return name;
    }
}
