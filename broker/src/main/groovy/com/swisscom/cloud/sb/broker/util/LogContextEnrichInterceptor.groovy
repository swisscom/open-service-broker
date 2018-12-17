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
            Map<String, String> pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
            if (pathVariables.containsKey(serviceInstanceKey)) {
                MDC.put(serviceInstanceKey, pathVariables.get(serviceInstanceKey).toString())
            }
        } catch (Exception e) {
            try{
                log.debug("Error setting log context", e)
                MDC.put(serviceInstanceKey. "00000000-0000-0000-0000-000000000000")
            }
            catch (Exception ex) {
                log.debug("Error setting log context", ex)
            }
        }
        return true
    }
}
