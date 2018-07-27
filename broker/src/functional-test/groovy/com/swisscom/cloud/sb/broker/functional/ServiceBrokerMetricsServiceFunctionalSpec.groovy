package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.metrics.BindingMetricsService
import com.swisscom.cloud.sb.broker.metrics.LastOperationMetricsService
import com.swisscom.cloud.sb.broker.metrics.LifecycleTimeMetricsService
import com.swisscom.cloud.sb.broker.metrics.ServiceBrokerMetricsConfig
import com.swisscom.cloud.sb.broker.metrics.ServiceInstanceMetricsService
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.broker.util.measurements.*
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import groovy.util.logging.Slf4j
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query
import org.influxdb.impl.InfluxDBResultMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.web.client.RestTemplate

import static junit.framework.Assert.assertEquals

class ServiceBrokerMetricsServiceFunctionalSpec extends BaseFunctionalSpec {

    final String SERVICE_NAME = "service1Name"
    final String SERVICE_GUID = "service1GuidIntegrationTest"
    final String PLAN_NAME = "small"
    final String PLAN_GUID = "plan1GuidIntegrationTest"
    final int TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS = 15000
    final int LIFECYCLE_TIME_IN_MILLISECONDS = 10000
    final int WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS = 1000

    ServiceBrokerClientExtended serviceBrokerClientExtended

    InfluxDB influxDB
    InfluxDBResultMapper influxDBResultMapper

    @Autowired
    ServiceBrokerMetricsConfig serviceBrokerMetricsConfig
    @Autowired
    CFServiceRepository cfServiceRepository
    @Autowired
    ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    ServiceBindingRepository serviceBindingRepository
    @Autowired
    PlanRepository planRepository

    def setup() {
        serviceBrokerClientExtended = new ServiceBrokerClientExtended(
                new RestTemplate(),
                "http://localhost:8080",
                serviceLifeCycler.cfAdminUser.username,
                serviceLifeCycler.cfAdminUser.password,
                serviceLifeCycler.cfExtUser.username,
                serviceLifeCycler.cfExtUser.password)

        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)

        influxDB = InfluxDBFactory.connect(
                serviceBrokerMetricsConfig.uri,
                serviceBrokerMetricsConfig.userName,
                serviceBrokerMetricsConfig.password)

        if (!influxDB.databaseExists(serviceBrokerMetricsConfig.db)) {
           influxDB.createDatabase(serviceBrokerMetricsConfig.db)
        }

        influxDBResultMapper = new InfluxDBResultMapper()
    }

    def cleanup() {
        influxDB.close()
    }

    def "only measurements that are retrievable from the database are recorded when db is empty and initialized with 0"() {
        given:
        def metricsRetrievableFromDB = [
                new MeasurementPoint(name: BindingMetricsService.BINDING_SERVICE_KEY),
                new MeasurementPoint(name: BindingMetricsService.NEW_SERVICE_BINDINGS_KEY),
                new MeasurementPoint(name: LastOperationMetricsService.LAST_OPERATION_EVENTS_KEY),
                new MeasurementPoint(name: LastOperationMetricsService.LAST_OPERATIONS_KEY),
                new MeasurementPoint(name: LifecycleTimeMetricsService.LIFECYCLE_TIME_KEY),
                new MeasurementPoint(name: ServiceInstanceMetricsService.SERVICE_INSTANCES_KEY),
        ]

        and:
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        when:
        Query query = new Query("show measurements", serviceBrokerMetricsConfig.db)
        def queryResult = influxDB.query(query)

        and:
        def influxDBResultMapper = new InfluxDBResultMapper()
        List<MeasurementPoint> results = influxDBResultMapper.toPOJO(queryResult, MeasurementPoint.class)

        then:
        assert (results.size() >= metricsRetrievableFromDB.size())
        assert (results.name.containsAll(metricsRetrievableFromDB.name))
    }

    def "update value for total bindings and bindings per service upon binding a service"() {
        given:

        def metricsRetrievableFromDB = [
                new MeasurementPoint(name: BindingMetricsService.BINDING_SERVICE_KEY, expectedReturnValue: 2),
                new MeasurementPoint(name: BindingMetricsService.NEW_SERVICE_BINDINGS_KEY, expectedReturnValue: 2)
        ]

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        def serviceBindingId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstanceBinding(new CreateServiceInstanceBindingRequest(SERVICE_GUID, PLAN_GUID, null, null).withServiceInstanceId(serviceInstanceGuid).withBindingId(serviceBindingId))
        def serviceBindingId2 = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstanceBinding(new CreateServiceInstanceBindingRequest(SERVICE_GUID, PLAN_GUID, null, null).withServiceInstanceId(serviceInstanceGuid).withBindingId(serviceBindingId2))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metricsRetrievableFromDB.each { metric ->
            def query = new Query("select value from ${metric.name} WHERE time >= time() - 1m GROUP BY time(5m)", serviceBrokerMetricsConfig.db)
            def queryResult = influxDB.query(query)
            List<GeneralMeasurement> result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)

            Double value = result.get(result.size() - 1).value
            println("#### Value is ${value}")
            assert (value >= metric.expectedReturnValue)
        }

        cleanup:
        Thread.sleep(10000)
        serviceBindingRepository.delete(serviceBindingRepository.findByGuid(serviceBindingId))
        serviceBindingRepository.delete(serviceBindingRepository.findByGuid(serviceBindingId2))
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
    }

    def "update value for total lifecycle time per service upon deprovisioning a service instance"() {
        given:
        def metrics = [
                new MeasurementPoint("lifecycleTime_service_total_${SERVICE_NAME}", LifecycleTimeServiceTotalServicePoint, LIFECYCLE_TIME_IN_MILLISECONDS)
        ]

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        Thread.sleep(LIFECYCLE_TIME_IN_MILLISECONDS)

        and:
        serviceBrokerClientExtended.deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceGuid, SERVICE_GUID, PLAN_GUID, true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", serviceBrokerMetricsConfig.db)
            def queryResult = influxDB.query(query)
            List<GeneralMeasurement> result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assertEquals(result.get(result.size() - 1).value, metric.expectedReturnValue, 1000)
        }

        cleanup:
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
    }

    def "update value for provision requests and provisioned instances including values for services and plans upon provisioning a service instance"() {
        given:
        def metrics = [
                new MeasurementPoint("provisionRequest_total_total", ProvisionRequestTotalTotalPoint, 1),
                new MeasurementPoint("provisionRequest_total_success", ProvisionRequestTotalSuccessPoint, 1),
                new MeasurementPoint("provisionRequest_total_fail", ProvisionRequestTotalFailPoint, 0),
                new MeasurementPoint("provisionRequest_success_ratio", ProvisionRequestSuccessRatioPoint, 100),
                new MeasurementPoint("provisionRequest_fail_ratio", ProvisionRequestFailRatioPoint, 0),
                new MeasurementPoint("provisionRequest_service_total_${SERVICE_NAME}", ProvisionRequestServiceTotalServicePoint, 1),
                new MeasurementPoint("provisionRequest_service_success_${SERVICE_NAME}", ProvisionRequestServiceSuccessServicePoint, 1),
                new MeasurementPoint("provisionRequest_service_fail_${SERVICE_NAME}", ProvisionRequestServiceFailServicePoint, 0),
                new MeasurementPoint("provisionRequest_plan_total_${PLAN_NAME}", ProvisionRequestPlanTotalPlanPoint, 1),
                new MeasurementPoint("provisionRequest_plan_success_${PLAN_NAME}", ProvisionRequestPlanSuccessPlanPoint, 1),
                new MeasurementPoint("provisionRequest_plan_fail_${PLAN_NAME}", ProvisionRequestPlanFailPlanPoint, 0),

                new MeasurementPoint("provisionedInstances_total_total", ProvisionedInstancesTotalTotalPoint, 1),
                new MeasurementPoint("provisionedInstances_total_success", ProvisionedInstancesTotalSuccessPoint, 1),
                new MeasurementPoint("provisionedInstances_total_fail", ProvisionedInstancesTotalFailPoint, 0),
                new MeasurementPoint("provisionedInstances_success_ratio", ProvisionedInstancesSuccessRatioPoint, 100),
                new MeasurementPoint("provisionedInstances_fail_ratio", ProvisionedInstancesFailRatioPoint, 0),
                new MeasurementPoint("provisionedInstances_service_total_${SERVICE_NAME}", ProvisionedInstancesServiceTotalServicePoint, 1),
                new MeasurementPoint("provisionedInstances_service_success_${SERVICE_NAME}", ProvisionedInstancesServiceSuccessServicePoint, 1),
                new MeasurementPoint("provisionedInstances_service_fail_${SERVICE_NAME}", ProvisionedInstancesServiceFailServicePoint, 0),
                new MeasurementPoint("provisionedInstances_plan_total_${PLAN_NAME}", ProvisionedInstancesPlanTotalPlanPoint, 1),
                new MeasurementPoint("provisionedInstances_plan_success_${PLAN_NAME}", ProvisionedInstancesPlanSuccessPlanPoint, 1),
                new MeasurementPoint("provisionedInstances_plan_fail_${PLAN_NAME}", ProvisionedInstancesPlanFailPlanPoint, 0)
        ]

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", serviceBrokerMetricsConfig.db)
            def queryResult = influxDB.query(query)
            List<GeneralMeasurement> result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assert (result.get(result.size() - 1).value == metric.expectedReturnValue)
        }

        cleanup:
        Thread.sleep(10000)
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
    }

    def "ignore deleted service instances in metrics regarding provisioned instances but include in provision requests"() {
        given:
        def metrics = [
                new MeasurementPoint("provisionRequest_total_total", ProvisionRequestTotalTotalPoint, 1),
                new MeasurementPoint("provisionRequest_total_success", ProvisionRequestTotalSuccessPoint, 1),
                new MeasurementPoint("provisionRequest_total_fail", ProvisionRequestTotalFailPoint, 0),
                new MeasurementPoint("provisionRequest_success_ratio", ProvisionRequestSuccessRatioPoint, 100),
                new MeasurementPoint("provisionRequest_fail_ratio", ProvisionRequestFailRatioPoint, 0),
                new MeasurementPoint("provisionRequest_service_total_${SERVICE_NAME}", ProvisionRequestServiceTotalServicePoint, 1),
                new MeasurementPoint("provisionRequest_service_success_${SERVICE_NAME}", ProvisionRequestServiceSuccessServicePoint, 1),
                new MeasurementPoint("provisionRequest_service_fail_${SERVICE_NAME}", ProvisionRequestServiceFailServicePoint, 0),
                new MeasurementPoint("provisionRequest_plan_total_${PLAN_NAME}", ProvisionRequestPlanTotalPlanPoint, 1),
                new MeasurementPoint("provisionRequest_plan_success_${PLAN_NAME}", ProvisionRequestPlanSuccessPlanPoint, 1),
                new MeasurementPoint("provisionRequest_plan_fail_${PLAN_NAME}", ProvisionRequestPlanFailPlanPoint, 0),

                new MeasurementPoint("provisionedInstances_total_total", ProvisionedInstancesTotalTotalPoint, 0),
                new MeasurementPoint("provisionedInstances_total_success", ProvisionedInstancesTotalSuccessPoint, 0),
                new MeasurementPoint("provisionedInstances_total_fail", ProvisionedInstancesTotalFailPoint, 0),
                new MeasurementPoint("provisionedInstances_success_ratio", ProvisionedInstancesSuccessRatioPoint, 0),
                new MeasurementPoint("provisionedInstances_fail_ratio", ProvisionedInstancesFailRatioPoint, 0),
                new MeasurementPoint("provisionedInstances_service_total_${SERVICE_NAME}", ProvisionedInstancesServiceTotalServicePoint, 0),
                new MeasurementPoint("provisionedInstances_service_success_${SERVICE_NAME}", ProvisionedInstancesServiceSuccessServicePoint, 0),
                new MeasurementPoint("provisionedInstances_service_fail_${SERVICE_NAME}", ProvisionedInstancesServiceFailServicePoint, 0),
                new MeasurementPoint("provisionedInstances_plan_total_${PLAN_NAME}", ProvisionedInstancesPlanTotalPlanPoint, 0),
                new MeasurementPoint("provisionedInstances_plan_success_${PLAN_NAME}", ProvisionedInstancesPlanSuccessPlanPoint, 0),
                new MeasurementPoint("provisionedInstances_plan_fail_${PLAN_NAME}", ProvisionedInstancesPlanFailPlanPoint, 0)
        ]

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))

        and:
        serviceBrokerClientExtended.deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceGuid, SERVICE_GUID, PLAN_GUID, true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", serviceBrokerMetricsConfig.db)
            def queryResult = influxDB.query(query)
            def result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assert (result.get(result.size() - 1).value == metric.expectedReturnValue)
        }

        cleanup:
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
    }
}
