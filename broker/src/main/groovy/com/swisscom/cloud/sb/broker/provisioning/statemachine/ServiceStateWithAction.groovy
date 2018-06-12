package com.swisscom.cloud.sb.broker.provisioning.statemachine


trait ServiceStateWithAction<T extends StateMachineContext> implements ServiceState, OnStateChange<T> {

}