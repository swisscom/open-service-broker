package com.swisscom.cf.broker.services.bosh.statemachine

import com.swisscom.cf.broker.provisioning.statemachine.StateMachineContext
import com.swisscom.cf.broker.services.bosh.BoshFacade
import com.swisscom.cf.broker.services.bosh.BoshTemplateCustomizer
import groovy.transform.CompileStatic

@CompileStatic
class BoshStateMachineContext extends StateMachineContext {
    BoshFacade boshFacade
    BoshTemplateCustomizer boshTemplateCustomizer
}
