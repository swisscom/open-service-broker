package com.swisscom.cf.broker.provisioning.statemachine

import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.services.bosh.BoshFacade
import com.swisscom.cf.broker.services.bosh.BoshTemplateCustomizer
import groovy.transform.CompileStatic


@CompileStatic
abstract class StateMachineContext {
    LastOperationJobContext lastOperationJobContext
    BoshFacade boshFacade
    BoshTemplateCustomizer boshTemplateCustomizer
}
