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

package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.metrics.BindingMetricsService
import com.swisscom.cloud.sb.broker.metrics.LifecycleTimeMetricsService
import com.swisscom.cloud.sb.broker.metrics.ServiceBrokerMetricsConfig
import com.swisscom.cloud.sb.broker.metrics.ServiceInstanceMetricsService
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query
import org.influxdb.impl.InfluxDBResultMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.web.client.RestTemplate
import spock.lang.Ignore
import spock.lang.IgnoreIf

import static junit.framework.Assert.assertEquals

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class ServiceBrokerMetricsServiceFunctionalSpec extends BaseFunctionalSpec {

    final String SERVICE_GUID = "metricsServiceFunctionalTest"
    final String PLAN_GUID = "metricsPlanFunctionalTest"
    final int TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS = 10000

    ServiceBrokerClientExtended serviceBrokerClientExtended

    InfluxDB influxDB
    InfluxDBResultMapper influxDBResultMapper

    @Autowired
    ServiceBrokerMetricsConfig serviceBrokerMetricsConfig

    def setup() {
        serviceBrokerClientExtended = new ServiceBrokerClientExtended(
                new RestTemplate(),
                "http://localhost:8080",
                serviceLifeCycler.cfAdminUser.username,
                serviceLifeCycler.cfAdminUser.password,
                serviceLifeCycler.cfExtUser.username,
                serviceLifeCycler.cfExtUser.password)

        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/metricsService.json"))

        if (serviceBrokerMetricsConfig.userName && serviceBrokerMetricsConfig.password) {
            influxDB = InfluxDBFactory.connect(
                    serviceBrokerMetricsConfig.uri,
                    serviceBrokerMetricsConfig.userName,
                    serviceBrokerMetricsConfig.password)
        } else {
            influxDB = InfluxDBFactory.connect(serviceBrokerMetricsConfig.uri)
        }

        if (!influxDB.databaseExists(serviceBrokerMetricsConfig.db)) {
            influxDB.createDatabase(serviceBrokerMetricsConfig.db)
        }

        influxDBResultMapper = new InfluxDBResultMapper()
    }

    def cleanup() {
        influxDB.close()
    }

    @Ignore
    def "delete Database"() {
        when:
        influxDB.deleteDatabase(serviceBrokerMetricsConfig.db)

        then:
        noExceptionThrown()
    }

    def "test metrics collection"() {
        given:
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        def nrOfServiceInstances = getValueFromDB("sum", ServiceInstanceMetricsService.SERVICE_INSTANCES_KEY)
        def nrOfServiceBindings = getValueFromDB("sum", BindingMetricsService.BINDING_SERVICE_KEY)
        def nrOfNewServiceBindings = getValueFromDB("sum", BindingMetricsService.NEW_SERVICE_BINDINGS_KEY, "", false)
        def nrOfDeletedServiceInstances = getValueFromDB("sum", ServiceInstanceMetricsService.SERVICE_INSTANCES_KEY, "status = 'deleted' and")
        def lifecycleTimeInSeconds = getValueFromDB("max", LifecycleTimeMetricsService.LIFECYCLE_TIME_KEY)

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(CreateServiceInstanceRequest.
                builder().
                serviceDefinitionId(SERVICE_GUID).
                planId(PLAN_GUID).
                serviceInstanceId(serviceInstanceGuid).
                asyncAccepted(true).
                build())
        def instantiationTime = System.currentTimeMillis()
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        def newNrOfServiceInstances = getValueFromDB("sum", ServiceInstanceMetricsService.SERVICE_INSTANCES_KEY)
        assertEquals(newNrOfServiceInstances - nrOfServiceInstances, 1.0, 0.1)

        and:
        def serviceBindingId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstanceBinding(CreateServiceInstanceBindingRequest.
                builder().
                serviceDefinitionId(SERVICE_GUID).
                planId(PLAN_GUID).
                bindingId(serviceBindingId).
                build()
        )
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        def newNrOfServiceBindings = getValueFromDB("sum", BindingMetricsService.BINDING_SERVICE_KEY)
        assertEquals(newNrOfServiceBindings - nrOfServiceBindings, 1.0, 0.1)

        def newNrOfNewServiceBindings = getValueFromDB("sum", BindingMetricsService.NEW_SERVICE_BINDINGS_KEY, "", false)
        assertEquals(newNrOfNewServiceBindings - nrOfNewServiceBindings, 1.0, 0.1)

        and:
        def deletionTime = System.currentTimeMillis()
        serviceBrokerClientExtended.deleteServiceInstance(org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest.
                builder().
                serviceDefinitionId(SERVICE_GUID).
                planId(PLAN_GUID).
                serviceInstanceId(serviceInstanceGuid).
                asyncAccepted(false).
                build())
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        def newNrOfDeletedServiceInstances = getValueFromDB("sum", ServiceInstanceMetricsService.SERVICE_INSTANCES_KEY, "status = 'deleted' and")
        assertEquals(newNrOfDeletedServiceInstances - nrOfDeletedServiceInstances, 1.0, 0.1)

        def newLifecycleTimeInSeconds = getValueFromDB("max", LifecycleTimeMetricsService.LIFECYCLE_TIME_KEY)
        assertEquals((deletionTime - instantiationTime) / 1000, newLifecycleTimeInSeconds - lifecycleTimeInSeconds, 50.0)

        then:
        noExceptionThrown()
    }

    private double getValueFromDB(String aggregation, String measurementName, String queryExtension = "", boolean groupedByTime = true) {
        def queryString = "select ${aggregation}(value) from ${measurementName} WHERE ${queryExtension} time > now() - 1h "
        if (groupedByTime) {
            queryString += "group by time(5s) "
        }
        queryString += "fill(none)"

        def query = new Query(queryString, serviceBrokerMetricsConfig.db)
        def queryResult = influxDB.query(query)
        Double.parseDouble(queryResult.results.last().series.last().values.last().last().toString())
    }
}
