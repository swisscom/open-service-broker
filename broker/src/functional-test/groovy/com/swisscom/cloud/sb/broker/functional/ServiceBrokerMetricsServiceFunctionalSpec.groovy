package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.metrics.ServiceBrokerMetricsConfig
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query
import org.influxdb.impl.InfluxDBResultMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.web.client.RestTemplate
import spock.lang.Ignore

import static junit.framework.Assert.assertEquals

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

        def nrOfServiceInstances = getValueFromDB("sum", "serviceInstances")
        def nrOfServiceBindings = getValueFromDB("sum", "ServiceBindings")
        def nrOfNewServiceBindings = getValueFromDB("sum", "NewServiceBindings", "", false)
        def nrOfDeletedServiceInstances = getValueFromDB("sum", "serviceInstances", "status = 'deleted' and")
        def lifecycleTimeInSeconds = getValueFromDB("max", "LifecycleTime")

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        def instantiationTime = System.currentTimeMillis()
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        def newNrOfServiceInstances = getValueFromDB("sum", "serviceInstances")
        assertEquals(newNrOfServiceInstances - nrOfServiceInstances, 1.0, 0.1)

        and:
        def serviceBindingId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstanceBinding(new CreateServiceInstanceBindingRequest(SERVICE_GUID, PLAN_GUID, null, null).withServiceInstanceId(serviceInstanceGuid).withBindingId(serviceBindingId))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        def newNrOfServiceBindings = getValueFromDB("sum", "ServiceBindings")
        assertEquals(newNrOfServiceBindings - nrOfServiceBindings, 1.0, 0.1)

        def newNrOfNewServiceBindings = getValueFromDB("sum", "NewServiceBindings", "", false)
        assertEquals(newNrOfNewServiceBindings - nrOfNewServiceBindings, 1.0, 0.1)

        and:
        def deletionTime = System.currentTimeMillis()
        serviceBrokerClientExtended.deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceGuid, SERVICE_GUID, PLAN_GUID, false))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        def newNrOfDeletedServiceInstances = getValueFromDB("sum", "serviceInstances", "status = 'deleted' and")
        assertEquals(newNrOfDeletedServiceInstances - nrOfDeletedServiceInstances, 1.0, 0.1)

        def newLifecycleTimeInSeconds = getValueFromDB("max", "LifecycleTime")
        assertEquals((deletionTime - instantiationTime) / 1000, newLifecycleTimeInSeconds - lifecycleTimeInSeconds, 1.0)

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
