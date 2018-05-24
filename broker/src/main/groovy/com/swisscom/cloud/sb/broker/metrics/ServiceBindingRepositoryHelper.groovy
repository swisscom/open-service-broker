package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class ServiceBindingRepositoryHelper {

    @Autowired
    ServiceBindingRepository serviceBindingRepository
}
