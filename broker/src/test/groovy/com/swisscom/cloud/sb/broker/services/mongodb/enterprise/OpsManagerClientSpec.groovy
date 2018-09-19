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

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.GroupDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.OpsManagerUserDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation.*
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerClient
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class OpsManagerClientSpec extends Specification {
    public static final String URL = 'http://someurl.com:1234'
    public static final String GROUP_ID = '5196d3628d022db4cbc26d9e'
    public static final String CLUSTER_ID = 'RS_1234'
    public static final String GROUP_NAME = 'pub'

    OpsManagerClient opsManagerClient
    MockRestServiceServer mockServer
    RestTemplateBuilder restTemplateBuilder

    def setup() {
        RestTemplate restTemplate = new RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        restTemplateBuilder = Mock(RestTemplateBuilder)
        restTemplateBuilder.build() >> restTemplate

        and:
        restTemplateBuilder.withDigestAuthentication(_, _) >> restTemplateBuilder

        and:
        opsManagerClient = new OpsManagerClient(restTemplateBuilder, new MongoDbEnterpriseConfig(opsManagerUrl: URL))
    }

    private String baseUrl() {
        return URL + OpsManagerClient.API_V1_CONTEXT_PATH
    }

    def "AA related http status codes are handled correctly"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + '/' + GROUP_ID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(status))

        when:
        opsManagerClient.getGroup(GROUP_ID)
        then:
        Exception ex = thrown(ServiceBrokerException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.MONGODB_OPS_MANAGER_AUTHENTICATION_FAILED)
        where:
        status << [HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN]
    }

    def "getAutomationAgents functions correctly"() {
        given:
        def response = """{
                              "links": [
                                {
                                  "href": "https://cloud.mongodb.com/api/public/v1.0/groups/xxxxxxxxxxxxxxxxxxxxxx/agents/AUTOMATION?null&pageNum=1&itemsPerPage=100",
                                  "rel": "self"
                                }
                              ],
                              "results": [
                                {
                                  "confCount": 141,
                                  "hostname": "example-1",
                                  "lastConf": "2015-06-18T14:27:42Z",
                                  "stateName": "ACTIVE",
                                  "typeName": "AUTOMATION"
                                },
                                {
                                  "confCount": 353,
                                  "hostname": "example",
                                  "lastConf": "2015-06-18T14:27:37Z",
                                  "stateName": "ACTIVE",
                                  "typeName": "AUTOMATION"
                                }
                              ],
                              "totalCount": 2
                            }"""
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.AGENTS + OpsManagerClient.AUTOMATION))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))
        when:
        def agents = opsManagerClient.listAutomationAgents(GROUP_ID)
        then:
        agents.size() == 2
        agents[0].confCount == 141
        agents[0].hostname == "example-1"
        agents[0].stateName == "ACTIVE"
        agents[0].typeName == "AUTOMATION"
        and:
        mockServer.verify()
    }

    def "updateAutomationConfig functions correctly"() {
        given:
        def config = new AutomationConfigDto()
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.AUTOMATION_CONFIG))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andExpect(MockRestRequestMatchers.content().string(serializeAsJson(config)))

                .andRespond(MockRestResponseCreators.withSuccess('', MediaType.APPLICATION_JSON))

        when:
        opsManagerClient.updateAutomationConfig(GROUP_ID, config)
        then:
        mockServer.verify()
    }


    def "getAutomationConfig functions correctly"() {
        given:
        def automationConfigResponse = '{}'
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.AUTOMATION_CONFIG))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(automationConfigResponse, MediaType.APPLICATION_JSON))

        when:
        opsManagerClient.getAutomationConfig(GROUP_ID)
        then:
        mockServer.verify()
    }

    def "getAutomationStatus functions correctly"() {
        given:
        def status = new AutomationStatusDto()
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.AUTOMATION_STATUS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(serializeAsJson(status), MediaType.APPLICATION_JSON))
        when:
        opsManagerClient.getAutomationStatus(GROUP_ID)
        then:
        mockServer.verify()
    }

    def "createGroup functions correctly"() {
        given:
        def groupDto = new GroupDto()
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(serializeAsJson(groupDto), MediaType.APPLICATION_JSON))

        when:
        opsManagerClient.createGroup(groupDto)
        then:
        mockServer.verify()
    }

    def "getGroup functions correctly"() {
        given:
        def group = new GroupDto()
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(serializeAsJson(group), MediaType.APPLICATION_JSON))

        when:
        opsManagerClient.getGroup(GROUP_ID)
        then:
        mockServer.verify()
    }

    def "deleteGroup functions correctly"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess())
        when:
        opsManagerClient.deleteGroup(GROUP_ID)
        then:
        mockServer.verify()
    }

    def "createUser functions correctly"() {
        given:
        def userDto = new OpsManagerUserDto()
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.USERS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(serializeAsJson(userDto), MediaType.APPLICATION_JSON))

        when:
        opsManagerClient.createUser(userDto)
        then:
        mockServer.verify()
    }

    def "delete user functions correctly"() {
        given:
        def userId = "userId"
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.USERS + "/" + userId))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess())
        when:
        opsManagerClient.deleteUser(userId)
        then:
        mockServer.verify()
    }

    def "delete hosts functions correctly"() {
        given:
        def hostId = 'hostId'
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.HOSTS + "/" + hostId))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess())
        when:
        opsManagerClient.deleteHost(GROUP_ID, hostId)
        then:
        mockServer.verify()
    }

    def "getUserByName functions correctly"() {
        given:
        def userName = 'userName'
        def response = """{"id": "xxx",
                          "username": "somebody",
                          "password": "abc123",
                          "emailAddress": "somebody@qa.example.com",
                          "mobileNumber": "2125551234",
                          "firstName": "John",
                          "lastName": "Doe",
                          "roles": [
                              {
                                "groupId": "8491812938cbda83918c",
                                "roleName": "GROUP_OWNER"
                              },
                              {
                                "groupId": "4829cbda839cbdac3819",
                                "roleName": "GROUP_READ_ONLY"
                              }
                          ]}
                                                          """
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.USERS + OpsManagerClient.BY_NAME + "/" + userName))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        when:
        def result = opsManagerClient.getUserByName(userName)
        then:
        mockServer.verify()

        result.id == 'xxx'
    }

    def "get user whiteList functions correctly"() {
        given:
        def userId = 'userName'
        def expectedResponse = """{
                                      "totalCount" : 1,
                                      "results" : [ {
                                        "ipAddress" : "12.34.56.78",
                                        "created" : "2014-04-23T16:17:44Z",
                                        "count" : 482
                                      } ]

                                    }"""
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.USERS + "/" + userId + OpsManagerClient.WHITE_LIST))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(expectedResponse, MediaType.APPLICATION_JSON))

        when:
        def result = opsManagerClient.getUserWhiteList(userId)
        then:
        mockServer.verify()

        result.results.size() == 1
        result.results.first().ipAddress == "12.34.56.78"
    }

    def "add white list functions correctly"() {
        given:
        def userId = 'userName'
        def list = [new WhiteListDto(ipAddress: '1.2.3.4')]
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.USERS + "/" + userId + OpsManagerClient.WHITE_LIST))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().string(new ObjectMapper().writeValueAsString(list)))
                .andRespond(MockRestResponseCreators.withSuccess())
        when:
        opsManagerClient.addUserWhiteList(userId, list)
        then:
        mockServer.verify()
    }

    def "getHostIds functions correctly"() {
        given:
        def response = new File(this.getClass().getResource('/mongodb/getHostsResult.json').getFile()).text

        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.HOSTS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))


        when:
        def result = opsManagerClient.getHostIds(GROUP_ID)
        then:
        mockServer.verify()
        result.size() == 3
    }

    def "get backupConfig functions correctly"() {
        given:
        def response = """{
                              "groupId" : "5196d3628d022db4cbc26d9e",
                              "clusterId" : "5196e5b0e4b0fca9cc88334a",
                              "statusName" : "STARTED",
                              "storageEngineName" : "WIRED_TIGER",
                              "sslEnabled" : false,
                              "excludedNamespaces" : [ ]
                            }"""
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.BACKUP_CONFIGS + "/" + CLUSTER_ID))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        when:
        def result = opsManagerClient.getBackupConfig(GROUP_ID, CLUSTER_ID)
        then:
        result.groupId == '5196d3628d022db4cbc26d9e'
        result.clusterId == '5196e5b0e4b0fca9cc88334a'
        result.statusName == 'STARTED'
        result.storageEngineName == 'WIRED_TIGER'
        result.excludedNamespaces == null || result.excludedNamespaces.size() == 0
    }

    def "update backupConfig functions correctly"() {
        given:
        def clusterId = 'clusterId'
        def expectedInput = new BackupConfigDto()
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.BACKUP_CONFIGS + "/" + clusterId))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PATCH))
                .andExpect(MockRestRequestMatchers.content().string(serializeAsJson(expectedInput)))
                .andRespond(MockRestResponseCreators.withSuccess(serializeAsJson(new BackupConfigDto()), MediaType.APPLICATION_JSON))
        when:
        def response = opsManagerClient.updateBackupConfig(GROUP_ID, clusterId, expectedInput)
        then:
        mockServer.verify()
    }

    def "delete backupConfig functions correctly"() {
        given:
        def clusterId = 'clusterId'
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.BACKUP_CONFIGS + "/" + clusterId))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess())
        when:
        opsManagerClient.deleteBackupConfig(GROUP_ID, clusterId)
        then:
        mockServer.verify()
    }

    def "get Snapshot functions correctly"() {
        given:
        def response = """{
                          "groupId" : "525ec8394f5e625c80c7404a",
                          "clusterId" : "53bc556ce4b049c88baec825",
                          "snapshotIntervalHours" : 6,
                          "snapshotRetentionDays" : 2,
                          "dailySnapshotRetentionDays" : 7,
                          "weeklySnapshotRetentionWeeks" : 4,
                          "monthlySnapshotRetentionMonths" : 13,
                          "pointInTimeWindowHours": 24
                          }
                      """
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.BACKUP_CONFIGS + "/" + CLUSTER_ID + OpsManagerClient.SNAPSHOT_SCHEDULE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))
        when:

        when:
        def result = opsManagerClient.getSnapshotSchedule(GROUP_ID, CLUSTER_ID)
        then:
        result.snapshotIntervalHours == 6
        result.snapshotRetentionDays == 2
        result.dailySnapshotRetentionDays == 7
        result.weeklySnapshotRetentionWeeks == 4
        result.monthlySnapshotRetentionMonths == 13
        result.pointInTimeWindowHours == 24
    }

    def "update Snapshot functions correctly"() {
        given:
        def input = new SnapshotScheduleDto(snapshotIntervalHours: 6)

        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.BACKUP_CONFIGS + "/" + CLUSTER_ID + OpsManagerClient.SNAPSHOT_SCHEDULE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PATCH))
                .andExpect(MockRestRequestMatchers.content().string(serializeAsJson(input)))
                .andRespond(MockRestResponseCreators.withSuccess(serializeAsJson(input), MediaType.APPLICATION_JSON))
        when:
        def result = opsManagerClient.updateSnapshotSchedule(GROUP_ID, CLUSTER_ID, input)
        then:
        mockServer.verify()
        result.snapshotIntervalHours == 6
    }

    def "delete Snapshot functions correctly"() {
        given:
        def clusterId = 'clusterId'
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.BACKUP_CONFIGS + "/" + clusterId + OpsManagerClient.SNAPSHOT_SCHEDULE))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess())
        when:
        opsManagerClient.deleteSnapshotSchedule(GROUP_ID, clusterId)
        then:
        mockServer.verify()
    }

    def "get clusters functions correctly"() {
        given:
        def response = """{
          "totalCount" : 3,
          "results" : [ {
            "id" : "533d7d4730040be257defe88",
            "typeName" : "SHARDED_REPLICA_SET",
            "clusterName" : "Animals",
            "lastHeartbeat" : "2014-04-03T15:26:58Z"
          }, {
            "id" : "533d7d4630040be257defe85",
            "typeName" : "REPLICA_SET",
            "clusterName" : "Animals",
            "shardName" : "cats",
            "replicaSetName" : "cats",
            "lastHeartbeat" : "2014-04-03T15:24:54Z"
          }, {
            "id" : "533d7d4630040be257defe83",
            "typeName" : "REPLICA_SET",
            "clusterName" : "Animals",
            "shardName" : "dogs",
            "replicaSetName" : "dogs",
            "lastHeartbeat" : "2014-04-03T15:26:30Z"
          } ]
          }"""
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.CLUSTERS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        when:
        ClustersDto result = opsManagerClient.getClusters(GROUP_ID)
        then:
        result.results.size() == 3
        result.results.first().id == '533d7d4730040be257defe88'
    }

    def "getClusterId functions correctly"() {
        given:
        def response = """{
                          "totalCount" : 3,
                          "results" : [ {
                            "id" : "533d7d4730040be257defe88",
                            "typeName" : "SHARDED_REPLICA_SET",
                            "clusterName" : "Animals1",
                            "lastHeartbeat" : "2014-04-03T15:26:58Z"
                          }, {
                            "id" : "533d7d4630040be257defe85",
                            "typeName" : "REPLICA_SET",
                            "clusterName" : "Animals2",
                            "shardName" : "cats",
                            "replicaSetName" : "cats",
                            "lastHeartbeat" : "2014-04-03T15:24:54Z"
                          }, {
                            "id" : "533d7d4630040be257defe83",
                            "typeName" : "REPLICA_SET",
                            "clusterName" : "Animals3",
                            "shardName" : "dogs",
                            "replicaSetName" : "dogs",
                            "lastHeartbeat" : "2014-04-03T15:26:30Z"
                          } ]
                          }"""

        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.CLUSTERS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))
        expect:
        opsManagerClient.getClusterId(GROUP_ID, 'cats') == "533d7d4630040be257defe85"
    }

    def "findClusterId functions correctly when cluster *not* found"() {
        given:
        def response = """{
                                                          "totalCount" : 0,
                                                          "results" : [  ]
                                                          }"""
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.CLUSTERS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))

        expect:
        opsManagerClient.findClusterId(GROUP_ID, 'cats') == Optional.absent()
    }

    def "findClusterId functions correctly when cluster is found"() {
        given:
        def response = """{
                          "totalCount" : 3,
                          "results" : [ {
                            "id" : "533d7d4730040be257defe88",
                            "typeName" : "SHARDED_REPLICA_SET",
                            "clusterName" : "Animals1",
                            "lastHeartbeat" : "2014-04-03T15:26:58Z"
                          }, {
                            "id" : "533d7d4630040be257defe85",
                            "typeName" : "REPLICA_SET",
                            "clusterName" : "Animals2",
                            "shardName" : "cats",
                            "replicaSetName" : "cats",
                            "lastHeartbeat" : "2014-04-03T15:24:54Z"
                          }, {
                            "id" : "533d7d4630040be257defe83",
                            "typeName" : "REPLICA_SET",
                            "clusterName" : "Animals3",
                            "shardName" : "dogs",
                            "replicaSetName" : "dogs",
                            "lastHeartbeat" : "2014-04-03T15:26:30Z"
                          } ]
                          }"""

        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.CLUSTERS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))
        expect:
        opsManagerClient.findClusterId(GROUP_ID, 'cats').get() == "533d7d4630040be257defe85"
    }

    def "listAlerts functions correctly"() {
        given:
        def response = """{
                              "links":[
                                {
                                  "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs?pageNum=1&itemsPerPage=100",
                                  "rel":"self"
                                }
                              ],
                              "results":[
                                {
                                  "created":"2018-08-16T10:52:51Z",
                                  "enabled":true,
                                  "eventTypeName":"HOST_SSL_CERTIFICATE_STALE",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b7557837497960c594ae6bc",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b7557837497960c594ae6bc",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailEnabled":true,
                                      "intervalMin":1440,
                                      "smsEnabled":false,
                                      "typeName":"GROUP"
                                    }
                                  ],
                                  "typeName":"HOST",
                                  "updated":"2018-08-16T10:52:51Z"
                                },
                                {
                                  "created":"2018-08-16T10:52:51Z",
                                  "enabled":true,
                                  "eventTypeName":"JOINED_GROUP",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b7557837497960c594ae6bd",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b7557837497960c594ae6bd",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailEnabled":true,
                                      "intervalMin":60,
                                      "smsEnabled":false,
                                      "typeName":"GROUP"
                                    }
                                  ],
                                  "typeName":"USER",
                                  "updated":"2018-08-16T10:52:51Z"
                                },
                                {
                                  "created":"2018-08-16T10:52:51Z",
                                  "enabled":true,
                                  "eventTypeName":"USERS_AWAITING_APPROVAL",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b7557837497960c594ae6be",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b7557837497960c594ae6be",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailEnabled":true,
                                      "intervalMin":60,
                                      "roles":[
                                        "GROUP_OWNER",
                                        "GROUP_USER_ADMIN"
                                      ],
                                      "smsEnabled":false,
                                      "typeName":"GROUP"
                                    }
                                  ],
                                  "typeName":"GROUP",
                                  "updated":"2018-08-16T10:52:51Z"
                                },
                                {
                                  "created":"2018-08-16T10:59:35Z",
                                  "enabled":true,
                                  "eventTypeName":"OPLOG_BEHIND",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b755917a7cfd30c6bf88391",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b755917a7cfd30c6bf88391",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailEnabled":true,
                                      "intervalMin":60,
                                      "smsEnabled":false,
                                      "typeName":"GROUP"
                                    }
                                  ],
                                  "typeName":"BACKUP",
                                  "updated":"2018-08-16T10:59:35Z"
                                },
                                {
                                  "created":"2018-08-16T10:59:35Z",
                                  "enabled":true,
                                  "eventTypeName":"CLUSTER_MONGOS_IS_MISSING",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b755917a7cfd30c6bf88392",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b755917a7cfd30c6bf88392",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailEnabled":true,
                                      "intervalMin":60,
                                      "smsEnabled":false,
                                      "typeName":"GROUP"
                                    }
                                  ],
                                  "typeName":"CLUSTER",
                                  "updated":"2018-08-16T10:59:35Z"
                                },
                                {
                                  "created":"2018-08-16T10:59:35Z",
                                  "enabled":true,
                                  "eventTypeName":"RESYNC_REQUIRED",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b755917a7cfd30c6bf88396",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b755917a7cfd30c6bf88396",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailEnabled":true,
                                      "intervalMin":60,
                                      "smsEnabled":false,
                                      "typeName":"GROUP"
                                    }
                                  ],
                                  "typeName":"BACKUP",
                                  "updated":"2018-08-16T10:59:35Z"
                                },
                                {
                                  "created":"2018-08-16T10:59:35Z",
                                  "enabled":true,
                                  "eventTypeName":"BACKUP_AGENT_DOWN",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b755917a7cfd30c6bf88397",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b755917a7cfd30c6bf88397",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailEnabled":true,
                                      "intervalMin":5,
                                      "smsEnabled":false,
                                      "typeName":"GROUP"
                                    }
                                  ],
                                  "typeName":"AGENT",
                                  "updated":"2018-08-16T10:59:35Z"
                                },
                                {
                                  "created":"2018-08-16T10:59:35Z",
                                  "enabled":true,
                                  "eventTypeName":"INCONSISTENT_BACKUP_CONFIGURATION",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b755917a7cfd30c6bf88398",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b755917a7cfd30c6bf88398",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailEnabled":true,
                                      "intervalMin":1440,
                                      "roles":[
                                        "GROUP_OWNER",
                                        "GROUP_BACKUP_ADMIN"
                                      ],
                                      "smsEnabled":false,
                                      "typeName":"GROUP"
                                    }
                                  ],
                                  "typeName":"BACKUP",
                                  "updated":"2018-08-16T10:59:35Z"
                                },
                                {
                                  "created":"2018-08-22T10:14:42Z",
                                  "enabled":false,
                                  "eventTypeName":"NO_PRIMARY",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b7d379256da750df55249fe",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b7d379256da750df55249fe",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "emailAddress":"opsmanager-pub-prd@swisscomappcloud.opsgenie.net",
                                      "intervalMin":60,
                                      "typeName":"EMAIL"
                                    }
                                  ],
                                  "typeName":"REPLICA_SET",
                                  "updated":"2018-08-23T09:12:55Z"
                                },
                                {
                                  "created":"2018-08-23T10:58:10Z",
                                  "enabled":true,
                                  "eventTypeName":"HOST_DOWN",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b7e934256da750df598747f",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b7e934256da750df598747f",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "intervalMin":60,
                                      "typeName":"WEBHOOK"
                                    }
                                  ],
                                  "typeName":"HOST",
                                  "updated":"2018-08-23T10:58:10Z"
                                },
                                {
                                  "created":"2018-08-23T11:02:44Z",
                                  "enabled":true,
                                  "eventTypeName":"NO_PRIMARY",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b7e9454e72ba10c62bc93fa",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b7e9454e72ba10c62bc93fa",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "intervalMin":60,
                                      "typeName":"WEBHOOK"
                                    }
                                  ],
                                  "typeName":"REPLICA_SET",
                                  "updated":"2018-08-23T11:02:44Z"
                                },
                                {
                                  "created":"2018-08-23T11:33:24Z",
                                  "enabled":true,
                                  "eventTypeName":"OPLOG_BEHIND",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b7e9b8456da750df59a582d",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b7e9b8456da750df59a582d",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "intervalMin":60,
                                      "typeName":"WEBHOOK"
                                    }
                                  ],
                                  "typeName":"BACKUP",
                                  "updated":"2018-08-23T11:33:24Z"
                                },
                                {
                                  "created":"2018-08-23T11:34:06Z",
                                  "enabled":true,
                                  "eventTypeName":"BACKUP_AGENT_DOWN",
                                  "groupId":"5b7557837497960c594ae6ae",
                                  "id":"5b7e9bae56da750df59a6171",
                                  "links":[
                                    {
                                      "href":"http://opsmanager.service.consul:8080/api/public/v1.0/groups/5b7557837497960c594ae6ae/alertConfigs/5b7e9bae56da750df59a6171",
                                      "rel":"self"
                                    }
                                  ],
                                  "matchers":[
                            
                                  ],
                                  "notifications":[
                                    {
                                      "delayMin":0,
                                      "intervalMin":60,
                                      "typeName":"WEBHOOK"
                                    }
                                  ],
                                  "typeName":"AGENT",
                                  "updated":"2018-08-23T11:34:06Z"
                                }
                              ],
                              "totalCount":13
                            }"""
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/" + GROUP_ID + OpsManagerClient.ALERT_CONFIGS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))
        when:
        def alertConfigs = opsManagerClient.listAlerts(GROUP_ID)
        then:
        alertConfigs.totalCount == 13
        alertConfigs.results.size() == 13
        alertConfigs.results.forEach { it.id != null }
        and:
        mockServer.verify()
    }

    def "deleteAlertConfig functions correctly"() {
        given:
        String groupId = "5b7557837497960c594ae6ae"
        String alertId = "5b7e9b8456da750df59a582d"
        and:
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS + "/${groupId}" + OpsManagerClient.ALERT_CONFIGS + "/${alertId}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess())
        when:
        opsManagerClient.deleteAlertConfig(groupId, alertId)
        then:
        mockServer.verify()
    }

    private void setupMockServerForGroups(MockRestServiceServer mockServer) {
        def response = """
                            {
                              "totalCount" : 1,
                              "results" : [ {
                                "id" : ${GROUP_ID},
                                "name" : ${GROUP_NAME},
                                "hostCounts" : {
                                  "arbiter" : 0,
                                  "config" : 1,
                                  "primary" : 3,
                                  "secondary" : 4,
                                  "mongos" : 2,
                                  "master" : 0,
                                  "slave" : 0
                                },
                                "lastActiveAgent" : "2014-04-03T18:18:12Z",
                                "activeAgentCount" : 1,
                                "replicaSetCount" : 3,
                                "shardCount" : 2
                              }]
                            }"""
        mockServer.expect(MockRestRequestMatchers.requestTo(baseUrl() + OpsManagerClient.GROUPS))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON))
    }

    private static String serializeAsJson(Object obj) {
        return new ObjectMapper().writeValueAsString(obj)
    }
}
