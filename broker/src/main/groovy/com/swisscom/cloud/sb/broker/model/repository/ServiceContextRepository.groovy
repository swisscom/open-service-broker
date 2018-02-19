package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.ServiceContext

interface ServiceContextRepository extends BaseRepository<ServiceContext, Integer> {
    ServiceContext findByPlatform(String platform)
}
