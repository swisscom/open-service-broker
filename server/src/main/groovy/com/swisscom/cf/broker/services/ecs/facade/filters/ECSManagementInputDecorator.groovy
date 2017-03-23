package com.swisscom.cf.broker.services.ecs.facade.filters

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload

class ECSManagementInputDecorator {

    ECSConfig ecsConfig

    def decorate(ECSMgmtNamespacePayload namespace) {
        namespace.namespace = ecsConfig.getEcsManagementNamespacePrefix()
        namespace.default_data_services_vpool = ecsConfig.getEcsDefaultDataServicesVpool()
        namespace.is_encryption_enabled = false
        namespace.default_bucket_block_size = -1
        namespace.is_stale_allowed = true
        namespace.compliance_enabled = false
    }

    def decorate(ECSMgmtUserPayload namespace) {

    }
}
