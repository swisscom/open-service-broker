package com.swisscom.cf.broker.services.ecs.facade.client.dtos

import groovy.transform.ToString

@ToString
class ECSMgmtSharedSecretKeyPayload implements Serializable {
    String namespace
}
