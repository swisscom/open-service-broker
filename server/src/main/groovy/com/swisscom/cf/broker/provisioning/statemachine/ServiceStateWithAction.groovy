package com.swisscom.cf.broker.provisioning.statemachine


trait ServiceStateWithAction<T extends StateMachineContext> implements ServiceState,OnStateChange<T>{

}