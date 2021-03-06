package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

import static com.swisscom.cloud.sb.broker.model.LastOperation.Status.IN_PROGRESS

@CompileStatic
trait ParentServiceProvider {
    private static final String MAX_CHILDREN = "max_children"

    @Autowired(required = true)
    LastOperationRepository lastOperationRepository

    boolean hasActiveChildren(ServiceInstance serviceInstance) {
        return getActiveChildrenCount(serviceInstance) > 0
    }

    boolean isFull(ServiceInstance serviceInstance) {
        Parameter param = serviceInstance.plan.parameters.find { p -> p.name == MAX_CHILDREN }
        if (param == null) {
            return false
        } else {
            return (param.value as int) <= getActiveChildrenCount(serviceInstance)
        }
    }

    int getActiveChildrenCount(ServiceInstance serviceInstance) {
        def undeletedChildren = serviceInstance.childs.findAll { si -> !si.deleted }
        def completedUndeletedChildrenCount = undeletedChildren.count { si -> si.completed }

        def uncompletedChildren = undeletedChildren.findAll { si -> !si.completed }

        int childrenProvisionInProgress = 0
        uncompletedChildren.each {
            si ->
                def lastOperation = lastOperationRepository.findByGuid(si.guid)
                if (lastOperation.status == IN_PROGRESS) {
                    childrenProvisionInProgress++
                }
        }

        return completedUndeletedChildrenCount + childrenProvisionInProgress
    }
}