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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine

import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceDetailKey
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseFreePortFinder
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation.BackupConfigDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import spock.lang.Specification

class MongoDbEnterpriseDeprovisionStateSpec extends Specification {
    private MongoDbEnterperiseStateMachineContext context

    def setup() {
        context = new MongoDbEnterperiseStateMachineContext()
        context.opsManagerFacade = Mock(OpsManagerFacade)
        context.mongoDbEnterpriseFreePortFinder = Mock(MongoDbEnterpriseFreePortFinder)
        context.mongoDbEnterpriseConfig = Stub(MongoDbEnterpriseConfig)
    }

    def "DISABLE_BACKUP_IF_ENABLED"() {
        given:
        def groupId = 'GroupId'
        def replicaset = 'replicaset'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId),
                                                                                                                     ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET, replicaset)]))

        and:
        1 * context.opsManagerFacade.isBackupInInactiveState(groupId, replicaset) >> true

        when:
        def result = MongoDbEnterpriseDeprovisionState.DISABLE_BACKUP_IF_ENABLED.triggerAction(context)

        then:
        1 * context.opsManagerFacade.disableAndTerminateBackup(groupId, replicaset)
        result.go2NextState
    }

    def "IF_BACKUP_NOT_INACTIVE_WAIT_FOR_NEXT_STATE"() {
        given:
        def groupId = 'GroupId'
        def replicaset = 'replicaset'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId),
                                                                                                                     ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET, replicaset)]))

        and:
        1 * context.opsManagerFacade.isBackupInInactiveState(groupId, replicaset) >> false

        when:
        def result = MongoDbEnterpriseDeprovisionState.DISABLE_BACKUP_IF_ENABLED.triggerAction(context)

        then:
        1 * context.opsManagerFacade.disableAndTerminateBackup(groupId, replicaset)
        !result.go2NextState
    }

    def "UPDATE_AUTOMATION_CONFIG"() {
        given:
        def groupId = 'GroupId'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId)]))

        when:
        def result = MongoDbEnterpriseDeprovisionState.UPDATE_AUTOMATION_CONFIG.triggerAction(context)

        then:
        1 * context.opsManagerFacade.undeploy(groupId)
        result.go2NextState
    }

    def "CHECK_AUTOMATION_CONFIG_STATE"() {
        given:
        def groupId = 'GroupId'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId)]))

        and:
        context.opsManagerFacade.isAutomationUpdateComplete(groupId) >> opsManagerResponse

        when:
        def result = MongoDbEnterpriseDeprovisionState.CHECK_AUTOMATION_CONFIG_STATE.triggerAction(context)

        then:
        result.go2NextState == go2NextState

        where:
        opsManagerResponse | go2NextState
        true               | true
        false              | false
    }

    def "DELETE_HOSTS_ON_OPS_MANAGER"() {
        given:
        def groupId = 'GroupId'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId)]))

        when:
        def result = MongoDbEnterpriseDeprovisionState.DELETE_HOSTS_ON_OPS_MANAGER.triggerAction(context)

        then:
        1 * context.opsManagerFacade.deleteAllHosts(groupId)
        result.go2NextState
    }

    def "CLEAN_UP_GROUP"() {
        given:
        def groupId = 'GroupId'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId)]))

        when:
        def result = MongoDbEnterpriseDeprovisionState.CLEAN_UP_GROUP.triggerAction(context)

        then:
        1 * context.opsManagerFacade.deleteGroup(groupId)
        result.go2NextState
    }

    def "DEPROVISION_SUCCESS"() {
        when:
        MongoDbEnterpriseDeprovisionState.DEPROVISION_SUCCESS.triggerAction(null)

        then:
        0 * _._
    }
}
