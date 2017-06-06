package com.swisscom.cloud.sb.broker.services.bosh.statemachine

import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineContext
import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplateCustomizer
import groovy.transform.CompileStatic

@CompileStatic
class BoshStateMachineContext extends StateMachineContext {
    BoshFacade boshFacade
    BoshTemplateCustomizer boshTemplateCustomizer
}
