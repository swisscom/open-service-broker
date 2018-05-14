package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import org.springframework.data.jpa.repository.Query

interface ServiceBindingRepository extends BaseRepository<ServiceBinding, Integer> {
    ServiceBinding findByGuid(String guid)

    @Query("select sb from ServiceBinding sb where sb.credhubCredentialId is null")
    List<ServiceBinding> findNotMigratedCredHubBindings()
}
