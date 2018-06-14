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

class PreviousValuesDto implements Serializable {
    @Deprecated
    String service_id
    String plan_id
    @Deprecated
    String organization_id
    @Deprecated
    String space_id

    @Override
    String toString() {
        return "PreviousValuesDto{" +
                "service_id='" + service_id + '\'' +
                ", plan_id='" + plan_id + '\'' +
                ", organization_guid='" + organization_id + '\'' +
                ", space_guid='" + space_id + '\'' +
                '}'
    }
}
