package com.swisscom.cloud.sb.broker.services.inventory

import org.springframework.data.util.Pair

interface InventoryService {
    Pair<String, String> get(String serviceInstanceGuid, String key)
    Pair<String, String> get(String serviceInstanceGuid, String key, String defaultValue)
    List<Pair<String, String>> get(String serviceInstanceGuid)
    List<Pair<String, String>> set(String serviceInstanceGuid, Pair<String, String> data)
    List<Pair<String, String>> replace(String serviceInstanceGuid, List<Pair<String, String>> data)
    List<Pair<String, String>> append(String serviceInstanceGuid, List<Pair<String, String>> data)
    List<Pair<String, String>> delete(String serviceInstanceGuid, String key)
}
