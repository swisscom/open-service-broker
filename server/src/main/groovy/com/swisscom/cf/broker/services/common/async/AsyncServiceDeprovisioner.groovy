package com.swisscom.cf.broker.services.common.async

import com.google.common.base.Optional
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import groovy.transform.CompileStatic

@CompileStatic
interface AsyncServiceDeprovisioner {
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context)
}