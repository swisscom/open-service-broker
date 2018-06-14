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
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import groovy.transform.CompileStatic

@CompileStatic
class DeprovisioningJobConfig extends JobConfig {
    final DeprovisionRequest deprovisionRequest

    DeprovisioningJobConfig(Class<? extends AbstractJob> jobClass, DeprovisionRequest deprovisionRequest, int retryIntervalInSeconds, double maxRetryDurationInMinutes) {
        super(jobClass, deprovisionRequest.serviceInstance.guid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.deprovisionRequest = deprovisionRequest
    }

    DeprovisioningJobConfig(Class<? extends AbstractJob> jobClass, DeprovisionRequest deprovisionRequest) {
        this(jobClass, deprovisionRequest, JobConfig.RETRY_INTERVAL_IN_SECONDS, JobConfig.MAX_RETRY_DURATION_IN_MINUTES)
    }
}