package com.swisscom.cloud.sb.broker.util

import groovy.util.logging.Slf4j
import org.slf4j.MDC
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
class LogContextEnrichInterceptor extends HandlerInterceptorAdapter {
    @Override
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            Map<String, String> pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
            if (pathVariables.containsKey("serviceInstanceGuid")) {
                MDC.put("serviceInstanceGuid", pathVariables.get("serviceInstanceGuid").toString())
            }
        } catch (Exception e) {
            log.debug("Error setting log context", e)
        }
        return true
    }
}
