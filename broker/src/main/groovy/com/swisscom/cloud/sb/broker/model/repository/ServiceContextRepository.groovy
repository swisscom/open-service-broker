package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.ServiceContext
import org.springframework.data.jpa.repository.Query

interface ServiceContextRepository extends BaseRepository<ServiceContext, Integer> {

    @Query("""select sc from ServiceContext sc 
             join sc.details scd
             join sc.details scd2
             where sc.platform='cloudfoundry' 
             and scd.key='organization_guid' 
             and scd.value=?1 
             and scd2.key='space_guid' 
             and scd2.value=?2
             and scd.serviceContext.id = sc.id
             and scd2.serviceContext.id = sc.id""")
    ServiceContext findCloudFoundryServiceContext(String orgGuid, String spaceGuid)

    @Query("""select sc from ServiceContext sc 
             join sc.details scd
             where sc.platform='kubernetes' 
             and scd.key='namespace' 
             and scd.value=?1
             and scd.serviceContext.id = sc.id""")
    ServiceContext findKubernetesServiceContext(String namespaceValue)
}
