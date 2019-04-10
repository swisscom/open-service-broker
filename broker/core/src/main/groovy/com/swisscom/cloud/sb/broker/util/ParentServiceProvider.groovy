package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import org.springframework.beans.factory.annotation.Autowired

trait ParentServiceProvider {
    private static final String MAX_CHILDREN = "max_children"

    @Autowired(required = true)
    LastOperationRepository lastOperationRepository

    boolean hasActiveChildren(ServiceInstance serviceInstance) {
        return getActiveChildrenCount(serviceInstance) > 0
    }

    boolean isFull(ServiceInstance serviceInstance) {
        Parameter param = serviceInstance.plan.parameters.find { it.name == MAX_CHILDREN }
        if (param == null) {
            return false
        } else {
            return (param.value as int) <= getActiveChildrenCount(serviceInstance)
        }
    }

    int getActiveChildrenCount(ServiceInstance serviceInstance) {
        List<ServiceInstance> undeletedChildren = serviceInstance.childs.findAll { si -> !si.deleted }
        int completedUndeletedChildrenCount = undeletedChildren.count { si -> si.completed }

        List<ServiceInstance> uncompletedChildren = undeletedChildren.findAll { si -> !si.completed }

        int childrenProvisionInProgress = 0
        uncompletedChildren.each {
            si ->
                def lastOperation = lastOperationRepository.findByGuid(si.guid)
                if (lastOperation.status == LastOperation.Status.IN_PROGRESS) {
                    childrenProvisionInProgress++
                }
        }

        return completedUndeletedChildrenCount + childrenProvisionInProgress
    }
}