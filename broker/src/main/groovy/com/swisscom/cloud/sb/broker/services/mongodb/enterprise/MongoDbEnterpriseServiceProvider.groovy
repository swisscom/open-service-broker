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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachine
import com.swisscom.cloud.sb.broker.services.AsyncServiceProvider
import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.bosh.BoshFacadeFactory
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplate
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplateCustomizer
import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshStateMachineFactory
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.DbUserCredentials
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine.MongoDbEnterperiseStateMachineContext
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine.MongoDbEnterpriseDeprovisionState
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine.MongoDbEnterpriseProvisionState
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException

import javax.annotation.PostConstruct

import static com.google.common.base.Strings.isNullOrEmpty
import static com.swisscom.cloud.sb.broker.model.ServiceDetail.from
import static com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine.MongoDbEnterpriseProvisionState.*

@Component
@CompileStatic
@Slf4j
class MongoDbEnterpriseServiceProvider
        extends AsyncServiceProvider<MongoDbEnterpriseConfig>
        implements BoshTemplateCustomizer {
    public static final String PARAM_MMS_BASE_URL = "mms-base-url"
    public static final String PARAM_MMS_API_KEY = "mms-api-key"
    public static final String MMS_GROUP_ID = "mms-group-id"

    public static final String PORT = 'port'
    public static final String MONGODB_BINARY_PATH = 'mongodb-binary-path'
    public static final String HEALTH_CHECK_USER = 'health-check-user'
    public static final String HEALTH_CHECK_PASSWORD = 'health-check-password'

    @Autowired
    protected OpsManagerFacade opsManagerFacade

    @Autowired
    protected MongoDbEnterpriseFreePortFinder mongoDbEnterpriseFreePortFinder

    @Autowired
    protected BoshFacadeFactory boshFacadeFactory

    @PostConstruct
    void init() {
        log.info(serviceConfig.toString())
    }

    @Override
    Collection<ServiceDetail> customizeBoshTemplate(BoshTemplate template, ProvisionRequest provisionRequest) {
        ServiceInstance serviceInstance = provisioningPersistenceService.getServiceInstance(provisionRequest.serviceInstanceGuid)

        String opsManagerGroupId = ServiceDetailsHelper.from(serviceInstance.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID)
        Preconditions.checkArgument(!isNullOrEmpty(opsManagerGroupId), "A valid OpsManager GroupId is required")

        String agentApiKey = ServiceDetailsHelper.from(serviceInstance.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_AGENT_API_KEY)
        Preconditions.checkArgument(!isNullOrEmpty(agentApiKey), "A valid AgentApiKey is required")

        template.replace(PARAM_MMS_BASE_URL, getOpsManagerUrl())
        template.replace(PARAM_MMS_API_KEY, agentApiKey)
        template.replace(MMS_GROUP_ID, opsManagerGroupId)
        template.replace(MongoDbEnterpriseServiceProvider.PORT, ServiceDetailsHelper.from(serviceInstance.details).getValue(PORT))
        template.replace(MONGODB_BINARY_PATH, getMongoDbBinaryPath())
        template.replace(HEALTH_CHECK_USER, ServiceDetailsHelper.from(serviceInstance.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_USER))
        template.replace(HEALTH_CHECK_PASSWORD, ServiceDetailsHelper.from(serviceInstance.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD))

        return [from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_TARGET_AGENT_COUNT, template.instanceCount() as String)]
    }

    @VisibleForTesting
    private String getMongoDbBinaryPath() {
        ((MongoDbEnterpriseConfig) serviceConfig).libFolder + '/bin/'
    }

    @VisibleForTesting
    private String getOpsManagerUrl() {
        def mongodbConfig = (MongoDbEnterpriseConfig) serviceConfig
        return mongodbConfig.opsManagerUrlForAutomationAgent ?: mongodbConfig.opsManagerUrl
    }

    @Override
    AsyncOperationResult requestUpdate(LastOperationJobContext context) {
        // verify upgrade possible
        if (context.updateRequest.plan == context.updateRequest.previousPlan) {
            // perform upgrade
            StateMachine stateMachine = createUpdateStateMachine()
            ServiceStateWithAction currentState = getUpdateState(context)
            def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(context))
            return AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState, actionResult.details)
        } else {
            // throw error for impossible upgrade
            ErrorCode.SERVICE_UPDATE_NOT_ALLOWED.throwNew()
            return null
        }
    }

    @VisibleForTesting
    private StateMachine createUpdateStateMachine() {
        StateMachine stateMachine = new StateMachine([CHECK_AGENTS, REQUEST_AUTOMATION_UPDATE, CHECK_AUTOMATION_UPDATE_STATUS, PROVISION_SUCCESS])
        return stateMachine
    }

    @VisibleForTesting
    private ServiceStateWithAction getUpdateState(LastOperationJobContext context) {
        ServiceStateWithAction provisionState = null
        if (!context.lastOperation.internalState) {
            provisionState = MongoDbEnterpriseProvisionState.CHECK_AGENTS
        } else {
            provisionState = MongoDbEnterpriseProvisionState.of(context.lastOperation.internalState)
        }
        return provisionState
    }

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        StateMachine stateMachine = createProvisionStateMachine(context)
        ServiceStateWithAction currentState = getProvisionState(context)
        def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(context))
        return AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState, actionResult.details)
    }

    @VisibleForTesting
    private MongoDbEnterperiseStateMachineContext createStateMachineContext(LastOperationJobContext context) {
        return new MongoDbEnterperiseStateMachineContext(mongoDbEnterpriseConfig: serviceConfig,
                mongoDbEnterpriseFreePortFinder: mongoDbEnterpriseFreePortFinder,
                opsManagerFacade: opsManagerFacade,
                boshFacade: getBoshFacade(),
                boshTemplateCustomizer: this,
                lastOperationJobContext: context)
    }

    @VisibleForTesting
    private StateMachine createProvisionStateMachine(LastOperationJobContext context) {
        StateMachine stateMachine = new StateMachine([CREATE_OPS_MANAGER_GROUP])
        stateMachine.addAllFromStateMachine(BoshStateMachineFactory.createProvisioningStateFlow(getBoshFacade().shouldCreateOpenStackServerGroup(context)))
        stateMachine.addAll([CHECK_AGENTS, REQUEST_AUTOMATION_UPDATE, CHECK_AUTOMATION_UPDATE_STATUS, DELETE_DEFAULT_ALERTS, ENABLE_BACKUP_IF_CONFIGURED, PROVISION_SUCCESS])
        return stateMachine
    }

    @VisibleForTesting
    private ServiceStateWithAction getProvisionState(LastOperationJobContext context) {
        ServiceStateWithAction provisionState = null
        if (!context.lastOperation.internalState) {
            provisionState = MongoDbEnterpriseProvisionState.CREATE_OPS_MANAGER_GROUP
        } else {
            provisionState = MongoDbEnterpriseProvisionState.of(context.lastOperation.internalState)
        }
        return provisionState
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        StateMachine stateMachine = createDeprovisionStateMachine(context)
        ServiceStateWithAction currentState = getDeprovisionState(context)
        def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(context))
        return Optional.of(AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState, actionResult.details))
    }

    @VisibleForTesting
    private StateMachine createDeprovisionStateMachine(LastOperationJobContext context) {
        StateMachine stateMachine = new StateMachine([MongoDbEnterpriseDeprovisionState.DISABLE_BACKUP_IF_ENABLED,
                                                      MongoDbEnterpriseDeprovisionState.UPDATE_AUTOMATION_CONFIG,
                                                      MongoDbEnterpriseDeprovisionState.CHECK_AUTOMATION_CONFIG_STATE,
                                                      MongoDbEnterpriseDeprovisionState.DELETE_HOSTS_ON_OPS_MANAGER])
        stateMachine.addAllFromStateMachine(BoshStateMachineFactory.createDeprovisioningStateFlow(getBoshFacade().shouldCreateOpenStackServerGroup(context)))
        stateMachine.addAll([MongoDbEnterpriseDeprovisionState.CLEAN_UP_GROUP, MongoDbEnterpriseDeprovisionState.DEPROVISION_SUCCESS])
    }

    @VisibleForTesting
    private ServiceStateWithAction getDeprovisionState(LastOperationJobContext context) {
        ServiceStateWithAction deprovisionState = null
        if (!context.lastOperation.internalState) {
            deprovisionState = MongoDbEnterpriseDeprovisionState.DISABLE_BACKUP_IF_ENABLED
        } else {
            deprovisionState = MongoDbEnterpriseDeprovisionState.of(context.lastOperation.internalState)
        }
        return deprovisionState
    }

    @Override
    BindResponse bind(BindRequest request) throws MongoDBAutomationConfigUpdateNotCompletedException {
        def database = ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.DATABASE)
        def hosts = ServiceDetailsHelper.from(request.serviceInstance.details).findAllWithServiceDetailType(ServiceDetailType.HOST)
        def groupId = getMongoDbGroupId(request.serviceInstance)
        // check for ongoing update isAutomationUpdateComplete(String groupId)
        checkForOnGoingChanges(groupId)
        DbUserCredentials dbUserCredentials = opsManagerFacade.createDbUser(groupId, database)
        checkForOnGoingChanges(groupId)
        def opsManagerCredentials = opsManagerFacade.createOpsManagerUser(groupId, request.serviceInstance.guid)
        return new BindResponse(details: [ServiceDetail.from(ServiceDetailKey.USER, dbUserCredentials.username),
                                          ServiceDetail.from(ServiceDetailKey.PASSWORD, dbUserCredentials.password),
                                          ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_NAME, opsManagerCredentials.user),
                                          ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_PASSWORD, opsManagerCredentials.password),
                                          ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_ID, opsManagerCredentials.userId)],
                credentials: new MongoDbEnterpriseBindResponseDto(
                        database: database,
                        username: dbUserCredentials.username,
                        password: dbUserCredentials.password,
                        hosts: hosts,
                        port: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(PORT),
                        opsManagerUrl: getOpsManagerUrl(),
                        opsManagerUser: opsManagerCredentials.user,
                        opsManagerPassword: opsManagerCredentials.password,
                        replicaSet: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET)))
    }

    @Override
    void unbind(UnbindRequest request) throws MongoDBAutomationConfigUpdateNotCompletedException {
        try {
            def groupId = getMongoDbGroupId(request.serviceInstance)
            // check for ongoing update isAutomationUpdateComplete(String groupId)
            checkForOnGoingChanges(groupId)
            opsManagerFacade.deleteDbUser(groupId,
                    ServiceDetailsHelper.from(request.binding.details).getValue(ServiceDetailKey.USER),
                    ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.DATABASE))
            opsManagerFacade.deleteOpsManagerUser(ServiceDetailsHelper.from(request.binding.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_ID))
            // check deletion of user isAutomationUpdateComplete(String groupId)
            checkForOnGoingChanges(groupId)
        } catch (HttpClientErrorException e) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                log.info(this.getClass().getSimpleName() + ".unbind(): Ignore 404 error during unbind")
            } else {
                throw e
            }
        }
    }

    @Retryable(
            value = MongoDBAutomationConfigUpdateNotCompletedException.class,
            maxAttempts = 10,
            backoff = @Backoff(delay = 5000l))
    void checkForOnGoingChanges(String groupId) throws MongoDBAutomationConfigUpdateNotCompletedException {
        log.info("Checking for on-going changes")
        if (!opsManagerFacade.isAutomationUpdateComplete(groupId))
            throw new MongoDBAutomationConfigUpdateNotCompletedException()
    }

    @Recover
    String recover(Throwable t) {
        log.info("Uable to create/delete user. OpsManager deployment still ongoing!");
        return "Error Class :: " + t.getClass().getName();
    }

    static String getMongoDbGroupId(LastOperationJobContext context) {
        return getMongoDbGroupId(context.serviceInstance)
    }

    static String getMongoDbGroupId(ServiceInstance serviceInstance) {
        return ServiceDetailsHelper.from(serviceInstance.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID)
    }

    BoshFacade getBoshFacade() {
        return boshFacadeFactory.build(serviceConfig)
    }
}