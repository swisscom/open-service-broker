package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.openstack

import groovy.transform.CompileStatic
import org.springframework.stereotype.Component


@Component
@CompileStatic
class OpenStackClientFactory {
    OpenStackClient createOpenStackClient(String openstackkUrl, String openstackUsername, String openstackPassword, String openstackTenantName) {
        return new OpenStackClient(openstackkUrl, openstackUsername,
                openstackPassword, openstackTenantName)
    }
}
