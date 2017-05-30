package com.swisscom.cloud.sb.broker.provisioning.async

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import groovy.transform.CompileStatic

@CompileStatic
interface AsyncServiceDeprovisioner {
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context)
}