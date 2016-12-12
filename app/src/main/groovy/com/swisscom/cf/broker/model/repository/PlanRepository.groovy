package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.Plan


interface PlanRepository extends BaseRepository<Plan, Integer> {
    Plan findByGuid(String guid)
}
