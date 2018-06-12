package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.async.job.JobStatus
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionProvider
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Status

class DummyExtension implements ExtensionProvider {

    @Override
    Collection<Extension> buildExtensions() {
        return [new Extension(discovery_url: "DummyExtensionURL")]
    }

    String lockUser(String id) {
        return "User locked with id = ${id}"
    }

    String unlockUser(String id) {
        queueExtension(new DummyJobConfig(DummyJob.class, id, 10, 300))
        getJobStatus(DummyStatus.SUCCESS)
    }

    @Override
    JobStatus getJobStatus(Status dummyStatus) {
        switch (dummyStatus) {
            case DummyStatus.SUCCESS:
                return JobStatus.SUCCESSFUL
            case DummyStatus.FAILED:
                return JobStatus.FAILED
            case DummyStatus.IN_PROGRESS:
                return JobStatus.RUNNING
            default:
                throw new RuntimeException("Unknown enum type: ${dummyStatus.toString()}")
        }

    }
}
