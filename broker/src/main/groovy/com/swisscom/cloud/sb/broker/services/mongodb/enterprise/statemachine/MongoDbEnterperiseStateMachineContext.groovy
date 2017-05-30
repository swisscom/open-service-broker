package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine

import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshStateMachineContext
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseFreePortFinder
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import groovy.transform.CompileStatic


@CompileStatic
class MongoDbEnterperiseStateMachineContext extends BoshStateMachineContext {
    OpsManagerFacade opsManagerFacade
    MongoDbEnterpriseFreePortFinder mongoDbEnterpriseFreePortFinder
    MongoDbEnterpriseConfig mongoDbEnterpriseConfig
}
