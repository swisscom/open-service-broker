package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.FreePortFinder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class MongoDbEnterpriseFreePortFinder extends FreePortFinder<MongoDbEnterpriseServiceProvider> {

    @Autowired
    MongoDbEnterpriseFreePortFinder(MongoDbEnterpriseConfig mongoDbEnterpriseConfig, ServiceInstanceRepository serviceInstanceRepository) {
        super(mongoDbEnterpriseConfig.portRange, serviceInstanceRepository)
    }
}
