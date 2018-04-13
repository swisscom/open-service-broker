package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.ServiceBinding

interface FetchServiceBindingProvider {
    ServiceInstanceBindingResponseDto fetchServiceBinding(ServiceBinding serviceBinding)
}