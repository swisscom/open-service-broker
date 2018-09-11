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

package com.swisscom.cloud.sb.broker.cfapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.CompileStatic

@CompileStatic
class CFServiceDto implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String id
    String name
    String description
    boolean bindable
    boolean active
    List<PlanDto> plans
    List<String> tags
    List<String> requires
    Map<String, Object> metadata = new HashMap<String, Object>()
    DashboardClientDto dashboard_client
    boolean plan_updateable
    Boolean instancesRetrievable
    Boolean bindingsRetrievable
}
