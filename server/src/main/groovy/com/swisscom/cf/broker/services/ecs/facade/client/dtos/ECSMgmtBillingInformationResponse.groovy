package com.swisscom.cf.broker.services.ecs.facade.client.dtos

import groovy.transform.ToString

@ToString
class ECSMgmtBillingInformationResponse {
    String next_marker
    String total_objects
    String sample_time
    String total_size
    String namespace
    String total_size_unit
}
