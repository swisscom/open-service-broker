package com.swisscom.cf.broker.services.ecs.facade.client.dtos

import groovy.transform.ToString

@ToString
class ECSMgmtNamespacePayload implements Serializable {

    String namespace
    String default_data_services_vpool
    Boolean is_encryption_enabled
    Integer default_bucket_block_size
    Boolean is_stale_allowed
    Boolean compliance_enabled

}
