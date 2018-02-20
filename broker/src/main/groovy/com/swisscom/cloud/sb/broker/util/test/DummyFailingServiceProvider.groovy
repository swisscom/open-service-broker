package com.swisscom.cloud.sb.broker.util.test

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Component
@Slf4j
class DummyFailingServiceProvider extends DummyServiceProvider {

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        throw new RuntimeException("This must crash")
        return new AsyncOperationResult()
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        return Optional.of(processOperationResultBasedOnIfEnoughTimeHasElapsed(context, DEFAULT_PROCESSING_DELAY_IN_SECONDS))
    }
}

