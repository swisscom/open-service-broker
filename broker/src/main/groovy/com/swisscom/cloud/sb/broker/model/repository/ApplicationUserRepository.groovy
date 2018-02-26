package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.ApplicationUser

interface ApplicationUserRepository extends BaseRepository<ApplicationUser, Integer> {
    ApplicationUser findByUsername(String username)
}