package com.swisscom.cf.broker.services.mongodb.enterprise

import com.swisscom.cf.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cf.broker.services.common.FreePortFinder
import com.swisscom.cf.broker.services.mongodb.enterprise.v2.MongoDbEnterpriseV2ServiceProvider
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Log4j
class MongoDbEnterpriseFreePortFinder extends FreePortFinder<MongoDbEnterpriseV2ServiceProvider> {

    @Autowired
    MongoDbEnterpriseFreePortFinder(MongoDbEnterpriseConfig mongoDbEnterpriseConfig, ServiceInstanceRepository serviceInstanceRepository) {
        super(mongoDbEnterpriseConfig.portRange, serviceInstanceRepository)
    }
}
