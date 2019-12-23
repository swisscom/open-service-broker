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

package com.swisscom.cloud.sb.broker.services.credential

import com.swisscom.cloud.sb.broker.model.ServiceDetail

class BindResponse {
    BindResponseDto credentials
    Collection<ServiceDetail> details

    /*
    Some services such as Atmos or Elk might have non-unique credentials.
    When this is the case, a 200 should be returned instead of 201.
    With the isUniqueCredentials flag, this behavior can be controlled.
    (see http://docs.cloudfoundry.org/services/api.html#binding for details)
    */
    boolean isUniqueCredentials = true
}
