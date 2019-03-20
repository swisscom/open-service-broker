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

package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface ServiceInstanceRepositoryCustom {
    //TODO rename
    @Query("SELECT si FROM ServiceInstance si WHERE si.guid in (select lo.guid from LastOperation lo where lo.operation=:operation and lo.status=:status and lo.dateCreation<:olderThan)")
    public List<ServiceInstance> queryServiceInstanceForLastOperation(
            @Param("operation") LastOperation.Operation operation,
            @Param('status') LastOperation.Status status, @Param('olderThan') Date olderThan)

    @Query("SELECT si FROM ServiceInstance si WHERE si.plan.internalName=:internalName OR si.plan.service.internalName=:internalName")
    public List<ServiceInstance> listAllForInternalName(@Param("internalName") String internalName)
}