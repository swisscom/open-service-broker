/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractJob
import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import groovy.transform.CompileStatic

@CompileStatic
class ProvisioningJobConfig extends JobConfig {

    final ProvisionRequest provisionRequest

    ProvisioningJobConfig(Class<? extends AbstractJob> jobClass, ProvisionRequest provisionRequest, int retryIntervalInSeconds, double maxRetryDurationInMinutes) {
        super(jobClass, provisionRequest.serviceInstanceGuid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.provisionRequest = provisionRequest
    }

    ProvisioningJobConfig(Class<? extends AbstractJob> jobClass, ProvisionRequest provisionRequest) {
        this(jobClass, provisionRequest, JobConfig.RETRY_INTERVAL_IN_SECONDS, JobConfig.MAX_RETRY_DURATION_IN_MINUTES)
    }

}
