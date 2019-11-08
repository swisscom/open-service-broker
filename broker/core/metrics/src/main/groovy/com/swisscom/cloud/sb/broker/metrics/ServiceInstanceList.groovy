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

package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.transform.CompileStatic

@CompileStatic
class ServiceInstanceList extends ArrayList<ServiceInstance> {

    Map<String, LastOperation> lastOperationMap = new HashMap<String, LastOperation>()

    ServiceInstanceList() { }

    ServiceInstanceList(Map<String, LastOperation> lastOperationMap, List<ServiceInstance> data = new ArrayList<ServiceInstance>()){
        super(data)

        this.refresh(lastOperationMap, data)
    }

    ServiceInstanceList refresh(Map<String, LastOperation> lastOperationMap, List<ServiceInstance> data) {
        this.clear()
        this.addAll(data)
        lastOperationMap.each { lOP -> this.lastOperationMap.put(lOP.key, lOP.value) }

        this
    }

    ServiceInstanceList byPlanId(String planGuid) {
        new ServiceInstanceList(lastOperationMap, this.findAll { serviceInstance -> serviceInstance.plan.guid == planGuid})
    }

    ServiceInstanceList inProgress() {
        def inProgressList = notCompleted().findAll { serviceInstance ->
            lastOperationMap.containsKey(serviceInstance.guid) &&
                    lastOperationMap.get(serviceInstance.guid).status == LastOperation.Status.IN_PROGRESS }

        new ServiceInstanceList(lastOperationMap, inProgressList)
    }

    private List<ServiceInstance> notCompleted() {
        this.findAll { serviceInstance -> serviceInstance.completed == false && serviceInstance.deleted == false}
    }

    ServiceInstanceList failed() {
        def inProgressList = notCompleted().findAll { serviceInstance ->
            lastOperationMap.containsKey(serviceInstance.guid) &&
                    lastOperationMap.get(serviceInstance.guid).status == LastOperation.Status.FAILED }

        new ServiceInstanceList(lastOperationMap, inProgressList)
    }

    ServiceInstanceList completed() {
        new ServiceInstanceList(lastOperationMap, this.findAll { serviceInstance -> serviceInstance.completed == true && serviceInstance.deleted == false})
    }

    ServiceInstanceList deleted() {
        new ServiceInstanceList(lastOperationMap, this.findAll { serviceInstance -> serviceInstance.deleted == true})
    }

    Double lifecycleTimeInSeconds() {
        Double totalLifecycleTimeInSeconds = 0.0F

        def now = new java.util.Date()
        this.each { sI -> totalLifecycleTimeInSeconds += (((sI.dateDeleted ?: now).getTime() - sI.dateCreated.getTime())/1000).toDouble() }

        return totalLifecycleTimeInSeconds
    }
}
