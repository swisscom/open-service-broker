package com.swisscom.cf.broker.services.ecs.facade.dtos


class ECSMgmtNamespace {

    String namespace
    String default_data_services_vpool
    String namespace_admins
    Boolean is_encryption_enabled
    Integer default_bucket_block_size
    Boolean is_stale_allowed
    Boolean compliance_enabled

}
