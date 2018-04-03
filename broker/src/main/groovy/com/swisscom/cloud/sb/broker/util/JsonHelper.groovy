package com.swisscom.cloud.sb.broker.util

import com.fasterxml.jackson.databind.ObjectMapper

abstract class JsonHelper {
    static final ObjectMapper objectMapper = new ObjectMapper()

    static Object parse(String json, Class cls) {
        if (json == null) {
            return null
        }
        objectMapper.readValue(json, cls)
    }

    static String toJsonString(Object o) {
        if (o == null) {
            return null
        }
        objectMapper.writeValueAsString(o)
    }

}
