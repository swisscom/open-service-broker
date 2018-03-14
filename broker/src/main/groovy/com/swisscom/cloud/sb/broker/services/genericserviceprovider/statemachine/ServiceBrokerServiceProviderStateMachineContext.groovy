package com.swisscom.cloud.sb.broker.services.genericserviceprovider.statemachine

import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineContext
import groovy.transform.CompileStatic

@CompileStatic
class ServiceBrokerServiceProviderStateMachineContext extends StateMachineContext{
    ServiceBrokerServiceProviderFacade sbspFacade
    ServiceBrokerServiceProviderRestClient sbspRestClient
}
