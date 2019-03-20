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

package com.swisscom.cloud.sb.broker.updating

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import sun.reflect.generics.reflectiveObjects.NotImplementedException

trait UpdatableProvisioner {
    abstract UpdateResponse updateParameters(UpdateRequest updateRequest)

    private UpdateResponse validateAndUpdatePlanAndParameters(UpdateRequest updateRequest) {
        if (!isPlanChangeSupported(updateRequest)) {
            ErrorCode.PLAN_UPDATE_NOT_ALLOWED.throwNew()
        }
        return updatePlanAndParameters(updateRequest)
    }

    UpdateResponse updatePlanAndParameters(UpdateRequest updateRequest) {
        throw new NotImplementedException()
    }

    UpdateResponse update(UpdateRequest updateRequest) {
        return isPlanChanging(updateRequest) ? validateAndUpdatePlanAndParameters(updateRequest) : updateParameters(updateRequest)
    }

    private boolean isPlanChanging(UpdateRequest updateRequest) {
        return updateRequest.plan != null &&
                updateRequest.previousPlan != null &&
                updateRequest.plan.guid != updateRequest.previousPlan.guid
    }

    private boolean isPlanChangeSupported(UpdateRequest updateRequest) {
        return updateRequest.previousPlan.service.plan_updateable;
    }
}