package com.swisscom.cf.broker.services.ecs.facade.filters

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload

class ECSManagementInputDecorator {

    ECSConfig ecsConfig

    def decorate(ECSMgmtNamespacePayload namespace) {
        namespace.namespace = ecsConfig.getEcsManagementNamespacePrefix() + get26HexCharsRandomlyGenerated()
        namespace.default_data_services_vpool = ecsConfig.getEcsDefaultDataServicesVpool()
        namespace.is_encryption_enabled = false
        namespace.default_bucket_block_size = -1
        namespace.is_stale_allowed = true
        namespace.compliance_enabled = false
    }

    def decorate(ECSMgmtUserPayload user) {
        user.user = user.user + getRandomlyUserGenerated()
    }

    String get26HexCharsRandomlyGenerated() {
        return "4816123317e943579636e88e23"
    }

    String getRandomlyUserGenerated() {
        return "user-1"
    }
}
