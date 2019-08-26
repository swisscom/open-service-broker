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
