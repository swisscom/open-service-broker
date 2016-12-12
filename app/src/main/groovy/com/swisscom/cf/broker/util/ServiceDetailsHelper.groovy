package com.swisscom.cf.broker.util

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.ServiceBinding
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.model.ServiceInstance
import groovy.transform.CompileStatic

@CompileStatic
class ServiceDetailsHelper {
    private Collection<ServiceDetail> details = []

    private ServiceDetailsHelper() {}

    private ServiceDetailsHelper(Collection<ServiceDetail> details) { this.details = details }

    public static ServiceDetailsHelper create() {
        return new ServiceDetailsHelper()
    }

    public static ServiceDetailsHelper from(Collection<ServiceDetail> details) {
        return new ServiceDetailsHelper(details)
    }

    public static ServiceDetailsHelper from(ServiceInstance serviceInstance) {
        return new ServiceDetailsHelper(serviceInstance.details)
    }

    public static ServiceDetailsHelper from(ServiceBinding serviceBinding) {
        return new ServiceDetailsHelper(serviceBinding.details)
    }

    def add(String key, Object value, boolean uniqueKey = false) {
        details.add(new ServiceDetail(key: key, value: value.toString(), uniqueKey: uniqueKey))
        return this;
    }

    def add(ServiceDetailKey key, Object value, boolean uniqueKey = false) {
        details.add(new ServiceDetail(key: key.key, type: key.detailType().type, value: value.toString(), uniqueKey: uniqueKey))
        return this;
    }

    def String getValue(String key) {
        return details.find { it.key == key }.value
    }

    def String getValue(ServiceDetailKey detailKey) {
        return getValue(detailKey.key)
    }

    def Optional<String> findValue(String key) {
        ServiceDetail detail = details.find { it.key == key }
        Optional<String> result = Optional.absent()
        if (detail) {
            result = Optional.of(detail.value)
        }
        return result
    }

    def Optional<String> findValue(ServiceDetailKey detailKey) {
        return findValue(detailKey.key)
    }

    def List<String> findAllWithServiceDetailType(ServiceDetailType serviceDetailType) {
        return details.findAll() { it.type == serviceDetailType.type }.collect { it.value }
    }

    Collection<ServiceDetail> getDetails() {
        return details
    }
}
