package com.swisscom.cf.broker.services.ecs.facade.client.dtos

import groovy.transform.ToString

@ToString
class ECSMgmtUserPayload implements Serializable {

    String user
    String namespace
}
