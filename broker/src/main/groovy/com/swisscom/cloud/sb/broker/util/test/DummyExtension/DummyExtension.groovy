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

package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.async.job.JobStatus
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionProvider
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Status

class DummyExtension implements ExtensionProvider{

    @Override
    Collection<Extension> buildExtensions(){
        return [new Extension(discovery_url: "DummyExtensionURL")]
    }

    String lockUser(String id){
        return "User locked with id = ${id}"
    }

    String unlockUser(String id){
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
