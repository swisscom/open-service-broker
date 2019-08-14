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

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceState
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshDeprovisionState
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceDetailKey
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceProvider
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

@Slf4j
enum MongoDbEnterpriseDeprovisionState implements ServiceStateWithAction<MongoDbEnterperiseStateMachineContext> {
    DISABLE_BACKUP_IF_ENABLED(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            ignore404 {
                String groupId = MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext)
                Optional<String> optionalReplicaSet = ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).findValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET)
                if (optionalReplicaSet.present) {
                    context.opsManagerFacade.disableAndTerminateBackup(groupId, optionalReplicaSet.get())
                } else {
                    log.warn("ReplicaSet not found for LastOperation:${context.lastOperationJobContext.lastOperation.guid}, " +
                            "the previous provisioning attempt must have failed.")
                }
            }
            return new StateChangeActionResult(go2NextState: context.opsManagerFacade.isBackupInInactiveState(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext), ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).findValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET).get()))
        }
    }),
    UPDATE_AUTOMATION_CONFIG(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {

            ignore404 {
                context.opsManagerFacade.undeploy(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext))
            }
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    CHECK_AUTOMATION_CONFIG_STATE(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            boolean automationConfigComplete
            boolean is404 = ignore404 {
                automationConfigComplete = context.opsManagerFacade.isAutomationUpdateComplete(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext))
            }
            return new StateChangeActionResult(go2NextState: automationConfigComplete || is404)
        }
    }),
    DELETE_HOSTS_ON_OPS_MANAGER(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            ignore404 {
                context.opsManagerFacade.deleteAllHosts(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext))
            }
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    CLEAN_UP_GROUP(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            ignore404 {
                context.opsManagerFacade.deleteGroup(ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID))
            }
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    DEPROVISION_SUCCESS(LastOperation.Status.SUCCESS, new NoOp())

    public static final Map<String, ServiceState> map = new TreeMap<String, ServiceState>()

    static {
        for (ServiceState serviceState : values() + BoshDeprovisionState.values()) {
            if (map.containsKey(serviceState.getServiceInternalState())) {
                throw new RuntimeException("Enum:${serviceState.getServiceInternalState()} already exists in:${MongoDbEnterpriseDeprovisionState.class.getSimpleName()}!")
            } else {
                map.put(serviceState.getServiceInternalState(), serviceState);
            }
        }
    }
    private final LastOperation.Status status
    private final OnStateChange<MongoDbEnterperiseStateMachineContext> onStateChange

    static boolean ignore404(Closure c) {
        try {
            c()
        } catch (HttpClientErrorException ex ) {
            if (ex.statusCode != HttpStatus.NOT_FOUND) {
                throw ex
            }
            else {
                log.info("ignoring HttpStatus 404, msg: " + ex.getMessage())
                return true
            }
        }
        return false
    }

    MongoDbEnterpriseDeprovisionState(LastOperation.Status lastOperationStatus, OnStateChange<MongoDbEnterperiseStateMachineContext> onStateChange) {
        this.status = lastOperationStatus
        this.onStateChange = onStateChange
    }

    @Override
    LastOperation.Status getLastOperationStatus() {
        return status
    }

    @Override
    String getServiceInternalState() {
        return name()
    }

    public static ServiceStateWithAction of(String state) {
        return map.get(state);
    }

    @Override
    StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
        return onStateChange.triggerAction(context)
    }
}