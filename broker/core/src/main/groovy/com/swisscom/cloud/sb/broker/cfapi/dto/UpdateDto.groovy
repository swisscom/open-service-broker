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

import org.hibernate.validator.constraints.NotBlank
import org.springframework.cloud.servicebroker.model.Context

class UpdateDto implements Serializable {
    @NotBlank(message = "service_id must not be blank")
    String service_id
    String plan_id
    Context context
    Map<String, Object> parameters
    PreviousValuesDto previous_values

    @Override
    String toString() {
        return "UpdateDto{" +
                "service_id='" + service_id + '\'' +
                ", plan_id='" + plan_id + '\'' +
                ", context='" + context + '\'' +
                ", parameters=" + parameters +
                ", previous_values=" + previous_values +
                '}'
    }
}
