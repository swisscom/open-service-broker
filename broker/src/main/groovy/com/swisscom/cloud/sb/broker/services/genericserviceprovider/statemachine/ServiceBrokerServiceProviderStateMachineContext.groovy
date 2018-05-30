package com.swisscom.cloud.sb.broker.services.genericserviceprovider.statemachine

import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineContext

import com.swisscom.cloud.sb.broker.services.genericserviceprovider.client.ServiceBrokerServiceProviderFacade
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.client.ServiceBrokerServiceProviderRestClient
import groovy.transform.CompileStatic

@CompileStatic
class ServiceBrokerServiceProviderStateMachineContext extends StateMachineContext{
    ServiceBrokerServiceProviderFacade sbspFacade
    ServiceBrokerServiceProviderRestClient sbspRestClient
}
