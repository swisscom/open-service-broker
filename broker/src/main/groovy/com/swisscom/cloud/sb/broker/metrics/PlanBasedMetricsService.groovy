package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.Plan

interface PlanBasedMetricsService {
    void bindMetricsPerPlan(Plan plan)
}