package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import org.springframework.transaction.annotation.Transactional

interface ServiceInstanceRepository extends BaseRepository<ServiceInstance, Integer>, ServiceInstanceRepositoryCustom {
    ServiceInstance findByGuid(String guid)

    List<ServiceInstance> findByPlanIdIn(List<Integer> planIds)

    List<ServiceInstance> findByPlan(Plan plan)

    @Transactional
    Integer deleteByGuid(String guid)
}
