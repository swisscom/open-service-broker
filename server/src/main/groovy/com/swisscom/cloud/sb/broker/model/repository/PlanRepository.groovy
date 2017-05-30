package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.Plan


interface PlanRepository extends BaseRepository<Plan, Integer> {
    Plan findByGuid(String guid)
}
