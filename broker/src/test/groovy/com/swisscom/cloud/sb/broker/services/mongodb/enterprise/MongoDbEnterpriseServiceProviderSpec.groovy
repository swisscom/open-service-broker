package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.AbstractAsyncServiceProviderSpec
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplate
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.DbUserCredentials
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerCredentials
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine.MongoDbEnterpriseDeprovisionState
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine.MongoDbEnterpriseProvisionState
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.json.JsonSlurper

import static MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID
import static MongoDbEnterpriseDeprovisionState.DISABLE_BACKUP_IF_ENABLED
import static MongoDbEnterpriseDeprovisionState.UPDATE_AUTOMATION_CONFIG
import static MongoDbEnterpriseProvisionState.PROVISION_SUCCESS
import static ServiceDetail.from
import static ServiceDetailKey.DATABASE

class MongoDbEnterpriseServiceProviderSpec extends AbstractAsyncServiceProviderSpec<MongoDbEnterpriseServiceProvider> {
    private String serviceInstanceGuid = 'serviceInstanceGuid'


    def setup(){
        serviceProvider.serviceConfig = new MongoDbEnterpriseConfig(retryIntervalInSeconds: 1, maxRetryDurationInMinutes: 1)
        serviceProvider.opsManagerFacade = Mock(OpsManagerFacade)
    }

    def "template customization works correctly"(){
        given:
        String mongoOpsManagerGroupId = 'mongoOpsManagerGroupId'
        String mongoAgentApiKey = 'mongoAgentApiKey'
        String port = '666'
        String healthCheckUser = 'healthCheckUser'
        String healthCheckPassword = 'healthCheckPassword'

        and:

        def serviceInstance = new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID,mongoOpsManagerGroupId),
                                                            ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_AGENT_API_KEY, mongoAgentApiKey),
                                                            ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_USER, healthCheckUser),
                                                            ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD, healthCheckPassword),
                                                            ServiceDetail.from(ServiceDetailKey.PORT, port)])
        1 * serviceProvider.provisioningPersistenceService.getServiceInstance(serviceInstanceGuid) >> serviceInstance
        and:
        BoshTemplate template = Mock(BoshTemplate)
        def instanceCount = 3
        1 * template.instanceCount() >> instanceCount
        and:
        def request = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)

        when:
        def details = serviceProvider.customizeBoshTemplate(template,request)

        then:
        1 * template.replace(MongoDbEnterpriseServiceProvider.PARAM_MMS_BASE_URL,serviceProvider.getOpsManagerUrl())
        1 * template.replace(MongoDbEnterpriseServiceProvider.PARAM_MMS_API_KEY,mongoAgentApiKey)
        1 * template.replace(MongoDbEnterpriseServiceProvider.MMS_GROUP_ID,mongoOpsManagerGroupId)
        1 * template.replace(MongoDbEnterpriseServiceProvider.PORT,port)
        1 * template.replace(MongoDbEnterpriseServiceProvider.MONGODB_BINARY_PATH,serviceProvider.getMongoDbBinaryPath())
        1 * template.replace(MongoDbEnterpriseServiceProvider.HEALTH_CHECK_USER,healthCheckUser)
        1 * template.replace(MongoDbEnterpriseServiceProvider.HEALTH_CHECK_PASSWORD,healthCheckPassword)
        ServiceDetailsHelper.from(details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_TARGET_AGENT_COUNT) == instanceCount.toString()
    }

    def "StateMachineContext is created correctly"(){
        given:
        def context = new LastOperationJobContext()
        when:
        def stateMachineContext = serviceProvider.createStateMachineContext(context)
        then:
        stateMachineContext.lastOperationJobContext == context
        stateMachineContext.opsManagerFacade == serviceProvider.opsManagerFacade
    }

    def "Provisioning StateMachine is created correctly"(){
        when:
        def result = serviceProvider.createProvisionStateMachine(new LastOperationJobContext())
        then:
        result.states.first() == MongoDbEnterpriseProvisionState.CREATE_OPS_MANAGER_GROUP
        result.states.last() == MongoDbEnterpriseProvisionState.PROVISION_SUCCESS
    }

    def "provision state is initialized correctly if context does not contain any state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation())
        when:
        def state = serviceProvider.getProvisionState(context)
        then:
        state == MongoDbEnterpriseProvisionState.CREATE_OPS_MANAGER_GROUP
    }

    def "provision state is initialized correctly if context include some previous state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: MongoDbEnterpriseProvisionState.CHECK_AUTOMATION_UPDATE_STATUS.toString()))
        when:
        def state = serviceProvider.getProvisionState(context)
        then:
        state == MongoDbEnterpriseProvisionState.CHECK_AUTOMATION_UPDATE_STATUS
    }

    def "happy path: requestProvision"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: PROVISION_SUCCESS.toString()))
        when:
        def result=serviceProvider.requestProvision(context)
        then:
        result
    }

    def "deprovision state is initialized correctly if context does not contain any state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation())
        when:
        def state = serviceProvider.getDeprovisionState(context)
        then:
        state == DISABLE_BACKUP_IF_ENABLED
    }

    def "deprovision state is initialized correctly if context include some previous state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: UPDATE_AUTOMATION_CONFIG.toString()))
        when:
        def state = serviceProvider.getDeprovisionState(context)
        then:
        state == UPDATE_AUTOMATION_CONFIG
    }

    def "happy path: requestDeprovision"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: MongoDbEnterpriseDeprovisionState.DEPROVISION_SUCCESS.toString()))
        when:
        def result=serviceProvider.requestDeprovision(context)
        then:
        result
    }

    def "Bind functions correctly"() {
        given:
        def groupId = 'groupId'
        BindRequest request = new BindRequest(serviceInstance: new ServiceInstance(guid: 'guid',
                details: [from(ServiceDetailKey.DATABASE, 'db'),
                          from(MONGODB_ENTERPRISE_GROUP_ID, groupId),
                          from(ServiceDetailKey.HOST, 'host1'),
                          from(ServiceDetailKey.HOST, 'host2'),
                          from(ServiceDetailKey.PORT, '27000'),
                          from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET, 'replicaSet')]))
        and:
        def dbUser = 'dbUser'
        def dbPassword = 'dbPassword'
        1 * serviceProvider.opsManagerFacade.createDbUser(groupId, 'db') >> new DbUserCredentials(username: dbUser, password: dbPassword)

        and:
        def opsManagerUser = 'opsManagerUser'
        def opsManagerUserId = 'opsManagerUserId'
        def opsManagerPassword = 'opsManagerPassword'

        1 * serviceProvider.opsManagerFacade.createOpsManagerUser(groupId, 'guid') >> new OpsManagerCredentials(user: opsManagerUser, password: opsManagerPassword, userId: opsManagerUserId)
        when:
        def bindResult = serviceProvider.bind(request)
        then:
        def details = ServiceDetailsHelper.from(bindResult.details)
        details.getValue(ServiceDetailKey.USER) == dbUser
        details.getValue(ServiceDetailKey.PASSWORD) == dbPassword
        details.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_NAME) == opsManagerUser
        details.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_ID) == opsManagerUserId
        details.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_PASSWORD) == opsManagerPassword
        and:
        def credentials = bindResult.credentials.toJson()
        def json = new JsonSlurper().parseText(credentials)
        json.credentials.replica_set == 'replicaSet'
    }

    def "Unbind functions correctly"() {
        given:
        UnbindRequest request = new UnbindRequest(serviceInstance: new ServiceInstance(details: [from(MONGODB_ENTERPRISE_GROUP_ID, 'groupId'), from(DATABASE, 'db')]),
                binding: new ServiceBinding(details: [from(ServiceDetailKey.USER, 'dbUser'),
                                                      from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_ID, 'opsManagerUserId')]))
        when:
        serviceProvider.unbind(request)
        then:
        1 * serviceProvider.opsManagerFacade.deleteDbUser('groupId', 'dbUser', 'db')
        1 * serviceProvider.opsManagerFacade.deleteOpsManagerUser('opsManagerUserId')
    }
}
