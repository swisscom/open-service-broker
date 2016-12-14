package com.swisscom.cf.broker.services.mongodb.enterprise

import com.swisscom.cf.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cf.broker.services.common.FreePortFinder
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Log4j
class MongoDbEnterpriseFreePortFinder extends FreePortFinder<MongoDbEnterpriseServiceProvider> {

    @Autowired
    MongoDbEnterpriseFreePortFinder(MongoDbEnterpriseConfig mongoDbEnterpriseConfig, ServiceInstanceRepository serviceInstanceRepository) {
        super(mongoDbEnterpriseConfig.portRange, serviceInstanceRepository)
    }
}
