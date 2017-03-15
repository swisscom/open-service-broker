package com.swisscom.cf.broker.services.ecs.config

import com.swisscom.cf.broker.config.Config
import groovy.transform.CompileStatic

@CompileStatic
trait ECSConfig implements Config {
    String ecsManagementBaseUrl
    String ecsManagementUsername
    String ecsManagementPassword
    String ecsManagementNamespacePrefix
}
