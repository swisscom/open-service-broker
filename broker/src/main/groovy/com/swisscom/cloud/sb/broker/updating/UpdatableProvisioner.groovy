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