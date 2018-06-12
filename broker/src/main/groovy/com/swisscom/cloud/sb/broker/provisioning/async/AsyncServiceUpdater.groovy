package com.swisscom.cloud.sb.broker.provisioning.async

import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext

interface AsyncServiceUpdater {
    AsyncOperationResult requestUpdate(LastOperationJobContext context)
}
