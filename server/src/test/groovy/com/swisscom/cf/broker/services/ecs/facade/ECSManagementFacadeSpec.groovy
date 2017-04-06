package com.swisscom.cf.broker.services.ecs.facade

import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtBillingInformationResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputDecorator
import spock.lang.Specification

class ECSManagementFacadeSpec extends Specification {

    ECSManagementFacade ecsManagementFacade
    ECSManagementInputDecorator ecsManagementInputFilter
    ECSMgmtNamespacePayload ecsMgmtNamespacePayload
    ECSManagementClient ecsManagementClient

    def setup() {
        ecsManagementInputFilter = Mock()
        ecsManagementClient = Stub()
        ecsMgmtNamespacePayload = Mock()
        ecsManagementFacade = new ECSManagementFacade(ecsManagementInputFilter, ecsManagementClient)
    }

    def "usage info return right data"() {
        given:
        ECSMgmtBillingInformationResponse ecsMgmtBillingInformationResponse = new ECSMgmtBillingInformationResponse(total_size: 23535453)
        ecsManagementClient.getUsage(ecsMgmtNamespacePayload) >> ecsMgmtBillingInformationResponse
        expect:
        ecsManagementFacade.getUsageInformationInBytes(ecsMgmtNamespacePayload) == "24100303872"
    }


}
