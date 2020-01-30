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

package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.backup.shield.dto.*

interface ShieldRestClient {
    Object getStatus()

    StoreDto getStoreByName(String name)

    RetentionDto getRetentionByName(String name)

    ScheduleDto getScheduleByName(String name)

    TargetDto getTargetByName(String name)

    Collection<TargetDto> getTargetsByName(String name)

    UUID createTarget(String targetName, ShieldTarget target, String agent)

    UUID updateTarget(TargetDto existingTarget, ShieldTarget target, String agent)

    void deleteTarget(UUID uuid)

    JobDto getJobByName(String name)

    JobDto getJobByUuid(UUID uuid)

    Collection<JobDto> getJobsByName(String name)

    UUID createJob(String jobName,
                     UUID targetUuid,
                     UUID storeUuid,
                     UUID retentionUuid,
                     UUID scheduleUuid,
                     boolean paused)

    UUID updateJob(JobDto existingJob,
                     UUID targetUuid,
                     UUID storeUuid,
                     UUID retentionUuid,
                     UUID scheduleUuid,
                     boolean paused)

    UUID runJob(UUID uuid)

    void deleteJob(UUID uuid)

    TaskDto getTaskByUuid(UUID uuid)

    void deleteTaskByUuid(UUID uuid)

    ArchiveDto getArchiveByUuid(UUID uuid)

    String restoreArchive(UUID uuid)

    void deleteArchive(UUID uuid)
}
