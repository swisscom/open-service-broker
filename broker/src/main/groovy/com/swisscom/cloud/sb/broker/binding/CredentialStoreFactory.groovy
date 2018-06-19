package com.swisscom.cloud.sb.broker.binding

import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CredentialStoreFactory implements FactoryBean<CredentialStoreStrategy> {

    @Autowired
    private DefaultCredentialStoreStrategy defaultCredentialStoreStrategy
    @Autowired
    private CredHubCredentialStoreStrategy credHubCredentialStoreStrategy

    @Override
    CredentialStoreStrategy getObject() throws Exception {
        credHubCredentialStoreStrategy.isCredHubServiceAvailable() ? credHubCredentialStoreStrategy : defaultCredentialStoreStrategy
    }

    @Override
    Class<?> getObjectType() {
        return CredentialStoreStrategy
    }

    @Override
    boolean isSingleton() {
        return true
    }

}
