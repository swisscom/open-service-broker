package com.swisscom.cf.broker.services.ecs.facade.filters

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.User;


class ECSManagementInputDecorator {

    ECSConfig ecsConfig

    def decorate(Namespace namespace) {
        namespace.namespace = ecsConfig.getEcsManagementNamespacePrefix()
        namespace.defaultDataServicesVpool = ecsConfig.getEcsDefaultDataServicesVpool()
        namespace.isEncryptionEnabled = false
        namespace.defaultBucketBlockSize = -1
        namespace.isStaleAllowed = true
        namespace.complianceEnabled = false
    }

    def decorate(User namespace) {
        
    }
}
