package com.swisscom.cloud.sb.broker.util

import groovy.util.logging.Slf4j
import org.slf4j.MDC
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
class LogContextEnrichInterceptor extends HandlerInterceptorAdapter {
    static String serviceInstanceKey = "serviceInstanceGuid"

    @Override
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            Map<String, String> pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map
            if (pathVariables && pathVariables.containsKey(serviceInstanceKey)) {
                MDC.put(serviceInstanceKey, pathVariables.get(serviceInstanceKey).toString())
            }
        } catch (Exception e) {
            log.debug("Error setting log context", e)
        }

        return true
    }
}
