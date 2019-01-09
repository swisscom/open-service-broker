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

package com.swisscom.cloud.sb.broker.services.mariadb

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.relationaldb.RelationalDbBindResponseDto
import com.swisscom.cloud.sb.broker.services.relationaldb.RelationalDbFacade
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import spock.lang.Specification

import static com.swisscom.cloud.sb.broker.error.ErrorCode.RELATIONAL_DB_NOT_FOUND
import static com.swisscom.cloud.sb.broker.error.ErrorCode.RELATIONAL_DB_USER_ALREADY_EXISTS
import static com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper.create
import static com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper.assertServiceBrokerException

class MariaDBServiceProviderSpec extends Specification {
    private final static String serviceInstanceGuid = "serviceInstanceGuid"
    private final static String serviceInstanceWithSpecialClusterGuid = "serviceInstanceWithSpecialClusterGuid"

    MariaDBServiceProvider mariaDBServiceProvider

    MariaDBClient mariaDBClient
    MariaDBClientFactory mariaDBClientFactory
    RelationalDbFacade relationalDbFacade

    private ServiceInstanceRepository serviceInstanceRepository

    private String db_prefix = 'cfdb_'
    private String db = (db_prefix + serviceInstanceGuid).toUpperCase()

    private ServiceInstance serviceInstance
    private ServiceInstance serviceInstanceWithSpecialCluster

    def setup() {
        serviceInstance = new ServiceInstance(guid: serviceInstanceGuid, details: create()
                .add(ServiceDetailKey.DATABASE, db)
                .getDetails())
        serviceInstance.plan = new Plan(service: new CFService())

        serviceInstanceWithSpecialCluster = new ServiceInstance(guid: serviceInstanceWithSpecialClusterGuid, details: create()
                .add(ServiceDetailKey.DATABASE, db)
                .getDetails())
        serviceInstanceWithSpecialCluster.plan = new Plan(service: new CFService(
                metadata: [new CFServiceMetadata(key: MariaDBServiceProvider.CLUSTER_METADATA_KEY, value: "special")]))

        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceInstanceRepository.findByGuid(serviceInstance.guid) >> serviceInstance
        serviceInstanceRepository.findByGuid(serviceInstanceWithSpecialCluster.guid) >> serviceInstanceWithSpecialCluster

        mariaDBClient = Mock(MariaDBClient)
        mariaDBClientFactory = Mock(MariaDBClientFactory)
        relationalDbFacade = new RelationalDbFacade()
        mariaDBClientFactory.build() >> mariaDBClient
        mariaDBClientFactory.build(_, _, _, _, _, _) >> mariaDBClient
        mariaDBServiceProvider = new MariaDBServiceProvider(
                new MariaDBConfig(clusters: [
                        new MariaDBConnectionConfig(name: "default", databasePrefix: db_prefix, host: 'host', port: '1234', adminPassword: 'adminpw', adminUser: 'admin', driver: 'com.mysql.cj.jdbc.Driver', vendor: 'mysql'),
                        new MariaDBConnectionConfig(name: "special", databasePrefix: db_prefix, host: 'special', port: '1234', adminPassword: 'special', adminUser: 'special', driver: 'com.mysql.cj.jdbc.Driver', vendor: 'mysql')]),
                mariaDBClientFactory,
                serviceInstanceRepository,
                relationalDbFacade)
    }

    def "happy path: provision"() {
        given:
        def request = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)
        mariaDBClient.databaseExists(db) >>> [false, true]
        when:
        ProvisionResponse provisionResponse = mariaDBServiceProvider.provision(request)
        then:
        1 * mariaDBClient.createDatabase(db)
        provisionResponse.details.size() == 1
        provisionResponse.dashboardURL == null
    }

    def "happy path: provision returns dashboardUrl if configured"() {
        def dashboardPath = 'http://mydashboard'
        given:
        mariaDBServiceProvider = new MariaDBServiceProvider(
                new MariaDBConfig(clusters: [
                        new MariaDBConnectionConfig(name: "special", databasePrefix: db_prefix, host: 'special', port: '1234', adminPassword: 'special', adminUser: 'special', driver: 'com.mysql.cj.jdbc.Driver', vendor: 'mysql', dashboardPath: dashboardPath)
                ]),
                mariaDBClientFactory,
                serviceInstanceRepository,
                relationalDbFacade)

        and:
        def request = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid, serviceContext: new ServiceContext(
                platform: CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM,
                details: [
                        ServiceContextDetail.from(ServiceContextHelper.CF_ORGANIZATION_GUID, 'my-org'),
                        ServiceContextDetail.from(ServiceContextHelper.CF_SPACE_GUID, 'my-space')]
        ))
        mariaDBClient.databaseExists(db) >>> [false, true]
        when:
        ProvisionResponse provisionResponse = mariaDBServiceProvider.provision(request)
        then:
        1 * mariaDBClient.createDatabase(db)
        provisionResponse.dashboardURL == dashboardPath
    }

    def "provision request for db schema that already exists throws exception"() {
        given:
        def request = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)
        mariaDBClient.databaseExists(db) >> true
        when:
        ProvisionResponse result = mariaDBServiceProvider.provision(request)
        then:
        thrown(ServiceBrokerException)
    }

    def "happy path: deprovision"() {
        given:
        mariaDBClient.databaseExists(db) >>> [true, false]
        when:
        def result = mariaDBServiceProvider.deprovision(new DeprovisionRequest(serviceInstance: serviceInstance, serviceInstanceGuid: serviceInstance.guid))
        then:
        1 * mariaDBClient.dropDatabase(db)
    }

    def "happy path: bind"() {
        given:
        BindRequest request = bindRequest()
        request.plan.parameters = [new Parameter(name: RelationalDbFacade.MAX_CONNECTIONS, value: '10')]
        mariaDBClient.databaseExists(db) >> true
        mariaDBClient.userExists(_) >>> [false, true]
        mariaDBClient.createUserAndGrantRights(db, _, _, _) >> new RelationalDbBindResponseDto()
        when:
        def result = mariaDBServiceProvider.bind(request)
        then:
        ServiceDetailsHelper.from(result.details).getValue(ServiceDetailKey.USER)
        ServiceDetailsHelper.from(result.details).getValue(ServiceDetailKey.PASSWORD)
        noExceptionThrown()
    }

    def "bind request throws exception when service instance not found"() {
        given:
        BindRequest request = bindRequest()
        RelationalDbBindResponseDto credentials = new RelationalDbBindResponseDto(username: "user", password: "pw")
        mariaDBClient.databaseExists(db) >> false
        when:
        mariaDBServiceProvider.bind(request)
        then:
        Exception ex = thrown(ServiceBrokerException)
        assertServiceBrokerException(ex, RELATIONAL_DB_NOT_FOUND)
    }

    def "bind request throws exception when username is already in the db"() {
        given:
        BindRequest request = bindRequest()
        new RelationalDbBindResponseDto(username: "user", password: "pw")
        mariaDBClient.databaseExists(db) >> true
        mariaDBClient.userExists(_) >> true
        when:
        mariaDBServiceProvider.bind(request)
        then:
        Exception ex = thrown(ServiceBrokerException)
        assertServiceBrokerException(ex, RELATIONAL_DB_USER_ALREADY_EXISTS)
    }

    def "happy path: unbind"() {
        given:
        def binding = createServiceBinding()
        def request = new UnbindRequest(binding: binding, serviceInstance: serviceInstance)
        mariaDBClient.userExists("user") >>> [true, false]
        when:
        mariaDBServiceProvider.unbind(request)
        then:
        1 * mariaDBClient.revokeRightsAndDropUser(db, 'user')
    }

    def "unbind throws exception when the username is not in db"() {
        given:
        def binding = createServiceBinding()
        serviceInstance.details.add(new ServiceDetail(key: ServiceDetailKey.USER.key, value: 'user'))
        def request = new UnbindRequest(serviceInstance: serviceInstance, binding: binding)
        mariaDBClient.userExists("user") >> false
        when:
        mariaDBServiceProvider.unbind(request)
        then:
        noExceptionThrown()
    }

    def "happy path: service usage"() {
        given:
        mariaDBClient.getUsageInBytes(ServiceDetailsHelper.from(serviceInstance.details).getValue(ServiceDetailKey.DATABASE)) >> '5'
        when:
        def usage = mariaDBServiceProvider.findUsage(serviceInstance, Optional.absent())
        then:
        usage.value == "5"
        usage.type == ServiceUsageType.WATERMARK
    }

    def "default Configuration is taken if no cluster is configured in ServiceDefinition"() {
        when:
        def config = mariaDBServiceProvider.getConfiguration(serviceInstance)
        then:
        config.name == "default"
        config.adminUser == "admin"
    }

    def "special Configuration is used if cluster is configured in ServiceDefinition"() {
        when:
        def config = mariaDBServiceProvider.getConfiguration(serviceInstanceWithSpecialCluster)
        then:
        config.name == "special"
        config.adminUser == "special"
    }

    private def createServiceBinding() {
        def binding = new ServiceBinding()
        binding.details.add(new ServiceDetail(key: ServiceDetailKey.USER.key, value: 'user'))
        return binding
    }

    private BindRequest bindRequest() {
        return new BindRequest(serviceInstance: serviceInstance, plan: new Plan())
    }
}