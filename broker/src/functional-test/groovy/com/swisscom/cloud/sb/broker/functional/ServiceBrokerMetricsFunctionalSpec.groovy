package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.broker.util.measurements.*
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

import static junit.framework.Assert.assertEquals

class ServiceBrokerMetricsFunctionalSpec extends BaseFunctionalSpec {

    final String DB_NAME = "mydb"
    final String SERVICE_NAME = "service1Name"
    final String SERVICE_GUID = "service1GuidIntegrationTest"
    final String PLAN_NAME = "small"
    final String PLAN_GUID = "plan1GuidIntegrationTest"
    final int TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS = 10000
    final int LIFECYCLE_TIME_IN_MILLISECONDS = 6000
    final int WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS = 1000

    ServiceBrokerClientExtended serviceBrokerClientExtended

    InfluxDB influxDB
    InfluxDBResultMapper influxDBResultMapper

    @Autowired
    CFServiceRepository cfServiceRepository
    @Autowired
    ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    ServiceBindingRepository serviceBindingRepository
    @Autowired
    PlanRepository planRepository

    def setup() {
        serviceBrokerClientExtended = new ServiceBrokerClientExtended(new RestTemplate(), "http://localhost:8080", serviceLifeCycler.cfAdminUser.username, serviceLifeCycler.cfAdminUser.password, serviceLifeCycler.cfExtUser.username, serviceLifeCycler.cfExtUser.password)

        influxDB = InfluxDBFactory.connect("http://localhost:8086", "admin", "admin")
        if (!influxDB.databaseExists(DB_NAME)) {
            influxDB.createDatabase(DB_NAME)
        }
        influxDBResultMapper = new InfluxDBResultMapper()
    }

    def cleanup() {
        influxDB.deleteDatabase(DB_NAME)
        influxDB.close()
    }

    def "only measurements that are retrievable from the database are recorded when db is empty and initialized with 0"() {
        given:
        def metricsRetrievableFromDB = [
                new MeasurementPoint("binding_total_total", BindingTotalTotalPoint),
                new MeasurementPoint("provisionRequest_fail_ratio", ProvisionRequestFailRatioPoint),
                new MeasurementPoint("provisionRequest_success_ratio", ProvisionRequestSuccessRatioPoint),
                new MeasurementPoint("provisionRequest_total_fail", ProvisionRequestTotalFailPoint),
                new MeasurementPoint("provisionRequest_total_success", ProvisionRequestTotalSuccessPoint),
                new MeasurementPoint("provisionRequest_total_total", ProvisionRequestTotalTotalPoint),
                new MeasurementPoint("provisionedInstances_fail_ratio", ProvisionedInstancesFailRatioPoint),
                new MeasurementPoint("provisionedInstances_success_ratio", ProvisionedInstancesSuccessRatioPoint),
                new MeasurementPoint("provisionedInstances_total_fail", ProvisionedInstancesTotalFailPoint),
                new MeasurementPoint("provisionedInstances_total_success", ProvisionedInstancesTotalSuccessPoint),
                new MeasurementPoint("provisionedInstances_total_total", ProvisionedInstancesTotalTotalPoint)
        ]

        and:
        def dynamicallyGeneratedMetrics = [
                new MeasurementPoint("bindingRequest_service_fail_${SERVICE_NAME}", BindingRequestServiceFailServicePoint),
                new MeasurementPoint("bindingRequest_service_success_${SERVICE_NAME}", BindingRequestServiceSuccessServicePoint),
                new MeasurementPoint("bindingRequest_service_total_${SERVICE_NAME}", BindingRequestServiceTotalServicePoint),
                new MeasurementPoint("binding_service_total_${SERVICE_NAME}", BindingServiceTotalServicePoint),
                new MeasurementPoint("lifecycleTime_service_total_${SERVICE_NAME}", LifecycleTimeServiceTotalServicePoint)
        ]

        and:
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        when:
        Query query = new Query("show measurements", DB_NAME)
        def queryResult = influxDB.query(query)

        and:
        def influxDBResultMapper = new InfluxDBResultMapper()
        def results = influxDBResultMapper.toPOJO(queryResult, MeasurementPoint.class)

        and:
        def measurementValueList = new ArrayList()
        results.name.each { measurement ->
            query = new Query("select value from ${measurement}", "mydb")
            def valueQueryResult = influxDB.query(query)
            measurementValueList.add(valueQueryResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(1))
        }

        then:
        assert (results.size() == metricsRetrievableFromDB.size())
        assert (results.name.containsAll(metricsRetrievableFromDB.name))
        assert (!results.name.containsAll(dynamicallyGeneratedMetrics.name))

        and:
        assert (measurementValueList.each { result ->
            result == 0.0
        })
    }

    def "write dynamically generated metrics upon service definition and initialize with zero"() {
        given:
        def metrics = [
                new MeasurementPoint("bindingRequest_service_fail_${SERVICE_NAME}", BindingRequestServiceFailServicePoint, 0),
                new MeasurementPoint("bindingRequest_service_success_${SERVICE_NAME}", BindingRequestServiceSuccessServicePoint, 0),
                new MeasurementPoint("bindingRequest_service_total_${SERVICE_NAME}", BindingRequestServiceTotalServicePoint, 0),
                new MeasurementPoint("binding_service_total_${SERVICE_NAME}", BindingServiceTotalServicePoint, 0),
                new MeasurementPoint("lifecycleTime_service_total_${SERVICE_NAME}", LifecycleTimeServiceTotalServicePoint, 0),
                new MeasurementPoint("provisionRequest_service_total_${SERVICE_NAME}", ProvisionRequestServiceTotalServicePoint, 0),
                new MeasurementPoint("provisionRequest_service_success_${SERVICE_NAME}", ProvisionRequestServiceSuccessServicePoint, 0),
                new MeasurementPoint("provisionRequest_service_fail_${SERVICE_NAME}", ProvisionRequestServiceFailServicePoint, 0),
                new MeasurementPoint("provisionRequest_plan_total_${PLAN_NAME}", ProvisionRequestPlanTotalPlanPoint, 0),
                new MeasurementPoint("provisionRequest_plan_success_${PLAN_NAME}", ProvisionRequestPlanSuccessPlanPoint, 0),
                new MeasurementPoint("provisionRequest_plan_fail_${PLAN_NAME}", ProvisionRequestPlanFailPlanPoint, 0),
                new MeasurementPoint("provisionedInstances_service_total_${SERVICE_NAME}", ProvisionedInstancesServiceTotalServicePoint, 0),
                new MeasurementPoint("provisionedInstances_service_success_${SERVICE_NAME}", ProvisionedInstancesServiceSuccessServicePoint, 0),
                new MeasurementPoint("provisionedInstances_service_fail_${SERVICE_NAME}", ProvisionedInstancesServiceFailServicePoint, 0),
                new MeasurementPoint("provisionedInstances_plan_total_${PLAN_NAME}", ProvisionedInstancesPlanTotalPlanPoint, 0),
                new MeasurementPoint("provisionedInstances_plan_success_${PLAN_NAME}", ProvisionedInstancesPlanSuccessPlanPoint, 0),
                new MeasurementPoint("provisionedInstances_plan_fail_${PLAN_NAME}", ProvisionedInstancesPlanFailPlanPoint, 0)
        ]

        and:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", DB_NAME)
            def queryResult = influxDB.query(query)
            def result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assert (result.size() == metric.expectedReturnValue)
        }

        when:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", DB_NAME)
            def queryResult = influxDB.query(query)
            List<GeneralMeasurement> result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assert (result.get(result.size() - 1).value == metric.expectedReturnValue)
        }

        cleanup:
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
    }

    def "update value for total bindings and bindings per service upon binding a service"() {
        given:
        def metrics = [
                new MeasurementPoint("bindingRequest_service_fail_${SERVICE_NAME}", BindingRequestServiceFailServicePoint, 0),
                new MeasurementPoint("bindingRequest_service_success_${SERVICE_NAME}", BindingRequestServiceSuccessServicePoint, 1),
                new MeasurementPoint("bindingRequest_service_total_${SERVICE_NAME}", BindingRequestServiceTotalServicePoint, 1),
                new MeasurementPoint("binding_service_total_${SERVICE_NAME}", BindingServiceTotalServicePoint, 1),
                new MeasurementPoint("binding_total_total", BindingTotalTotalPoint, 1),
        ]

        when:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)

        and:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        def serviceBindingId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstanceBinding(new CreateServiceInstanceBindingRequest(SERVICE_GUID, PLAN_GUID, null, null).withServiceInstanceId(serviceInstanceGuid).withBindingId(serviceBindingId))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", DB_NAME)
            def queryResult = influxDB.query(query)
            List<GeneralMeasurement> result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assert (result.get(result.size() - 1).value == metric.expectedReturnValue)
        }

        cleanup:
        serviceBindingRepository.delete(serviceBindingRepository.findByGuid(serviceBindingId))
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
    }

    def "update value for total lifecycle time per service upon deprovisioning a service instance"() {
        given:
        def metrics = [
                new MeasurementPoint("lifecycleTime_service_total_${SERVICE_NAME}", LifecycleTimeServiceTotalServicePoint, LIFECYCLE_TIME_IN_MILLISECONDS)
        ]

        when:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)

        and:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        Thread.sleep(LIFECYCLE_TIME_IN_MILLISECONDS)

        and:
        serviceBrokerClientExtended.deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceGuid, SERVICE_GUID, PLAN_GUID, true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", DB_NAME)
            def queryResult = influxDB.query(query)
            List<GeneralMeasurement> result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assertEquals (result.get(result.size() - 1).value, metric.expectedReturnValue, 1000)
        }

        cleanup:
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
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
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)

        and:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", DB_NAME)
            def queryResult = influxDB.query(query)
            List<GeneralMeasurement> result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assert (result.get(result.size() - 1).value == metric.expectedReturnValue)
        }

        cleanup:
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
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
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)

        and:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))

        and:
        serviceBrokerClientExtended.deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceGuid, SERVICE_GUID, PLAN_GUID, true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        metrics.each { metric ->
            def query = new Query("select value from ${metric.name}", DB_NAME)
            def queryResult = influxDB.query(query)
            List<GeneralMeasurement> result = influxDBResultMapper.toPOJO(queryResult, metric.measurementPointClass)
            assert (result.get(result.size() - 1).value == metric.expectedReturnValue)
        }

        cleanup:
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
    }
}
