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

package com.swisscom.cloud.sb.broker.repository

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface ServiceInstanceRepository extends BaseRepository<ServiceInstance, Integer>, ServiceInstanceRepositoryCustom {
    ServiceInstance findByGuid(String guid)

    @Query("SELECT s FROM ServiceInstance s LEFT JOIN FETCH s.childs LEFT JOIN FETCH s.details LEFT JOIN FETCH s.bindings WHERE s.guid = (:guid)")
    ServiceInstance findByGuidAndFetchChildsAndDetailsEagerly(@Param("guid") String guid)

    List<ServiceInstance> findByPlanIdIn(List<Integer> planIds)

    List<ServiceInstance> findByPlan(Plan plan)

    @Transactional
    Integer deleteByGuid(String guid)
}
