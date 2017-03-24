package com.swisscom.cf.broker.services.ecs.facade.filters

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.util.StringGenerator

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
        //TODO StringGenerator.randomAlphaNumeric(26)
        return "1fe5ba4816123317e943579636e88e29"
    }

    String getRandomlyUserGenerated() {
        //TODO
        return "user-1"
    }
}
