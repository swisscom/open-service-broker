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

class ServiceBrokerMetricsFunctionalSpec extends BaseFunctionalSpec {

    final String DB_NAME = "mydb"
    final String SERVICE_NAME = "service1Name"
    final String SERVICE_GUID = "service1GuidIntegrationTest"
    final String PLAN_NAME = "small"
    final String PLAN_GUID = "plan1GuidIntegrationTest"
    final String BINDING_TOTAL_TOTAL = "binding_total_total"
    final int TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS = 10000
    final int LIFECYCLE_TIME_IN_MILLISECONDS = 6000
    final int WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS = 1000
    final ArrayList<MeasurementPoint> metricsRetrievableFromDB = [new MeasurementPoint("binding_total_total"), new MeasurementPoint("provisionRequest_fail_ratio"), new MeasurementPoint("provisionRequest_success_ratio"), new MeasurementPoint("provisionRequest_total_fail"), new MeasurementPoint("provisionRequest_total_success"), new MeasurementPoint("provisionRequest_total_total"), new MeasurementPoint("provisionedInstances_fail_ratio"), new MeasurementPoint("provisionedInstances_success_ratio"), new MeasurementPoint("provisionedInstances_total_fail"), new MeasurementPoint("provisionedInstances_total_success"), new MeasurementPoint("provisionedInstances_total_total")]
    final ArrayList<MeasurementPoint> dynamicallyGeneratedMetrics = [new MeasurementPoint("bindingRequest_service_fail_${SERVICE_NAME}"), new MeasurementPoint("bindingRequest_service_success_${SERVICE_NAME}"), new MeasurementPoint("bindingRequest_service_total_${SERVICE_NAME}"), new MeasurementPoint("binding_service_total_${SERVICE_NAME}"), new MeasurementPoint("lifecycleTime_service_total_${SERVICE_NAME}")]

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
        def bindingServiceTotalServiceQuery = new Query("select value from binding_service_total_${SERVICE_NAME}", DB_NAME)
        def bindingServiceTotalServiceQueryResult = influxDB.query(bindingServiceTotalServiceQuery)
        def bindingServiceTotalServiceResult = influxDBResultMapper.toPOJO(bindingServiceTotalServiceQueryResult, BindingServiceTotalServicePoint.class)

        and:
        def bindingRequestServiceTotalServiceQuery = new Query("select value from bindingRequest_service_total_${SERVICE_NAME}", DB_NAME)
        def bindingRequestServiceTotalServiceQueryResult = influxDB.query(bindingRequestServiceTotalServiceQuery)
        def bindingRequestServiceTotalServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceTotalServiceQueryResult, BindingRequestServiceTotalServicePoint.class)

        and:
        def bindingRequestServiceSuccessServiceQuery = new Query("select value from bindingRequest_service_success_${SERVICE_NAME}", DB_NAME)
        def bindingRequestServiceSuccessServiceQueryResult = influxDB.query(bindingRequestServiceSuccessServiceQuery)
        def bindingRequestServiceSuccessServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceSuccessServiceQueryResult, BindingRequestServiceSuccessServicePoint.class)

        and:
        def bindingRequestServiceFailServiceQuery = new Query("select value from bindingRequest_service_fail_${SERVICE_NAME}", DB_NAME)
        def bindingRequestServiceFailServiceQueryResult = influxDB.query(bindingRequestServiceFailServiceQuery)
        def bindingRequestServiceFailServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceFailServiceQueryResult, BindingRequestServiceFailServicePoint.class)

        and:
        def lifecycleTimeServiceTotalServiceQuery = new Query("select value from lifecycleTime_service_total_${SERVICE_NAME}", DB_NAME)
        def lifecycleTimeServiceTotalServiceQueryResult = influxDB.query(lifecycleTimeServiceTotalServiceQuery)
        def lifecycleTimeServiceTotalServiceResult = influxDBResultMapper.toPOJO(lifecycleTimeServiceTotalServiceQueryResult, LifecycleTimeServiceTotalServicePoint.class)

        when:
        assert (bindingServiceTotalServiceResult.size() == 0)
        assert (bindingRequestServiceTotalServiceResult.size() == 0)
        assert (bindingRequestServiceSuccessServiceResult.size() == 0)
        assert (bindingRequestServiceFailServiceResult.size() == 0)
        assert (lifecycleTimeServiceTotalServiceResult.size() == 0)

        and:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        and:
        bindingServiceTotalServiceQueryResult = influxDB.query(bindingServiceTotalServiceQuery)
        bindingServiceTotalServiceResult = influxDBResultMapper.toPOJO(bindingServiceTotalServiceQueryResult, BindingServiceTotalServicePoint.class)

        and:
        bindingRequestServiceTotalServiceQueryResult = influxDB.query(bindingRequestServiceTotalServiceQuery)
        bindingRequestServiceTotalServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceTotalServiceQueryResult, BindingRequestServiceTotalServicePoint.class)

        and:
        bindingRequestServiceSuccessServiceQueryResult = influxDB.query(bindingRequestServiceSuccessServiceQuery)
        bindingRequestServiceSuccessServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceSuccessServiceQueryResult, BindingRequestServiceSuccessServicePoint.class)

        and:
        bindingRequestServiceFailServiceQueryResult = influxDB.query(bindingRequestServiceFailServiceQuery)
        bindingRequestServiceFailServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceFailServiceQueryResult, BindingRequestServiceFailServicePoint.class)

        and:
        lifecycleTimeServiceTotalServiceQueryResult = influxDB.query(lifecycleTimeServiceTotalServiceQuery)
        lifecycleTimeServiceTotalServiceResult = influxDBResultMapper.toPOJO(lifecycleTimeServiceTotalServiceQueryResult, LifecycleTimeServiceTotalServicePoint.class)

        then:
        assert (bindingServiceTotalServiceResult.get(bindingServiceTotalServiceResult.size() - 1).value == 0.0)
        assert (bindingRequestServiceTotalServiceResult.get(bindingServiceTotalServiceResult.size() - 1).value == 0.0)
        assert (bindingRequestServiceSuccessServiceResult.get(bindingRequestServiceSuccessServiceResult.size() - 1).value == 0.0)
        assert (bindingRequestServiceFailServiceResult.get(bindingRequestServiceFailServiceResult.size() - 1).value == 0.0)
        assert (lifecycleTimeServiceTotalServiceResult.get(lifecycleTimeServiceTotalServiceResult.size() - 1).value == 0.0)

        cleanup:
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
    }

    def "update value for total bindings and bindings per service upon binding a service"() {
        setup:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)
        and:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        def serviceBindingId = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstanceBinding(new CreateServiceInstanceBindingRequest(SERVICE_GUID, PLAN_GUID, null, null).withServiceInstanceId(serviceInstanceGuid).withBindingId(serviceBindingId))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        when:
        def bindingTotalTotalQuery = new Query("select value from ${BINDING_TOTAL_TOTAL}", DB_NAME)
        def bindingTotalTotalQueryResult = influxDB.query(bindingTotalTotalQuery)
        def bindingTotalTotalResult = influxDBResultMapper.toPOJO(bindingTotalTotalQueryResult, BindingTotalTotalPoint.class)

        and:
        def bindingServiceTotalServiceQuery = new Query("select value from binding_service_total_${SERVICE_NAME}", DB_NAME)
        def bindingServiceTotalServiceQueryResult = influxDB.query(bindingServiceTotalServiceQuery)
        def bindingServiceTotalServiceResult = influxDBResultMapper.toPOJO(bindingServiceTotalServiceQueryResult, BindingServiceTotalServicePoint.class)

        and:
        def bindingRequestServiceTotalServiceQuery = new Query("select value from bindingRequest_service_total_${SERVICE_NAME}", DB_NAME)
        def bindingRequestServiceTotalServiceQueryResult = influxDB.query(bindingRequestServiceTotalServiceQuery)
        def bindingRequestServiceTotalServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceTotalServiceQueryResult, BindingRequestServiceTotalServicePoint.class)

        and:
        def bindingRequestServiceSuccessServiceQuery = new Query("select value from bindingRequest_service_success_${SERVICE_NAME}", DB_NAME)
        def bindingRequestServiceSuccessServiceQueryResult = influxDB.query(bindingRequestServiceSuccessServiceQuery)
        def bindingRequestServiceSuccessServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceSuccessServiceQueryResult, BindingRequestServiceSuccessServicePoint.class)

        and:
        def bindingRequestServiceFailServiceQuery = new Query("select value from bindingRequest_service_fail_${SERVICE_NAME}", DB_NAME)
        def bindingRequestServiceFailServiceQueryResult = influxDB.query(bindingRequestServiceFailServiceQuery)
        def bindingRequestServiceFailServiceResult = influxDBResultMapper.toPOJO(bindingRequestServiceFailServiceQueryResult, BindingRequestServiceFailServicePoint.class)

        then:
        assert (bindingTotalTotalResult.get(bindingTotalTotalResult.size() - 1).value == 1.0)
        assert (bindingServiceTotalServiceResult.get(bindingServiceTotalServiceResult.size() - 1).value == 1.0)
        assert (bindingRequestServiceTotalServiceResult.get(bindingRequestServiceTotalServiceResult.size() - 1).value == 1.0)
        assert (bindingRequestServiceSuccessServiceResult.get(bindingRequestServiceSuccessServiceResult.size() - 1).value == 1.0)
        assert (bindingRequestServiceFailServiceResult.get(bindingRequestServiceFailServiceResult.size() - 1).value == 0.0)

        cleanup:
        serviceBindingRepository.delete(serviceBindingRepository.findByGuid(serviceBindingId))
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
    }

    def "update value for total lifecycle time per service upon deprovisioning a service instance"() {
        given:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        Thread.sleep(LIFECYCLE_TIME_IN_MILLISECONDS)

        and:
        serviceBrokerClientExtended.deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceGuid, SERVICE_GUID, PLAN_GUID, true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        then:
        def lifecycleTimeServiceTotalServiceQuery = new Query("select value from lifecycleTime_service_total_${SERVICE_NAME}", DB_NAME)
        def lifecycleTimeServiceTotalServiceQueryResult = influxDB.query(lifecycleTimeServiceTotalServiceQuery)
        def lifecycleTimeServiceTotalServiceResult = influxDBResultMapper.toPOJO(lifecycleTimeServiceTotalServiceQueryResult, LifecycleTimeServiceTotalServicePoint.class)
        assert (lifecycleTimeServiceTotalServiceResult.get(lifecycleTimeServiceTotalServiceResult.size() - 1).value == LIFECYCLE_TIME_IN_MILLISECONDS)

        cleanup:
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
    }

    def "update value for provision requests and provisioned instances including values for services and plans upon provisioning a service instance"() {
        given:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        and:
        def provisionRequestTotalTotalQuery = new Query("select value from provisionRequest_total_total", DB_NAME)
        def provisionRequestTotalTotalQueryResult = influxDB.query(provisionRequestTotalTotalQuery)
        def provisionRequestTotalTotalResult = influxDBResultMapper.toPOJO(provisionRequestTotalTotalQueryResult, ProvisionRequestTotalTotalPoint.class)

        and:
        def provisionRequestTotalSuccessQuery = new Query("select value from provisionRequest_total_success", DB_NAME)
        def provisionRequestTotalSuccessQueryResult = influxDB.query(provisionRequestTotalSuccessQuery)
        def provisionRequestTotalSuccessResult = influxDBResultMapper.toPOJO(provisionRequestTotalSuccessQueryResult, ProvisionRequestTotalSuccessPoint.class)

        and:
        def provisionRequestTotalFailQuery = new Query("select value from provisionRequest_total_fail", DB_NAME)
        def provisionRequestTotalFailQueryResult = influxDB.query(provisionRequestTotalFailQuery)
        def provisionRequestTotalFailResult = influxDBResultMapper.toPOJO(provisionRequestTotalFailQueryResult, ProvisionRequestTotalFailPoint.class)

        and:
        def provisionRequestSuccessRatioQuery = new Query("select value from provisionRequest_success_ratio", DB_NAME)
        def provisionRequestSuccessRatioQueryResult = influxDB.query(provisionRequestSuccessRatioQuery)
        def provisionRequestSuccessRatioResult = influxDBResultMapper.toPOJO(provisionRequestSuccessRatioQueryResult, ProvisionRequestSuccessRatioPoint.class)

        and:
        def provisionRequestFailRatioQuery = new Query("select value from provisionRequest_fail_ratio", DB_NAME)
        def provisionRequestFailRatioQueryResult = influxDB.query(provisionRequestFailRatioQuery)
        def provisionRequestFailRatioResult = influxDBResultMapper.toPOJO(provisionRequestFailRatioQueryResult, ProvisionRequestFailRatioPoint.class)

        and:
        def provisionRequestServiceTotalServiceQuery = new Query("select value from provisionRequest_service_total_${SERVICE_NAME}", DB_NAME)
        def provisionRequestServiceTotalServiceQueryResult = influxDB.query(provisionRequestServiceTotalServiceQuery)
        def provisionRequestServiceTotalServiceResult = influxDBResultMapper.toPOJO(provisionRequestServiceTotalServiceQueryResult, ProvisionRequestServiceTotalServicePoint.class)

        and:
        def provisionRequestServiceSuccessServiceQuery = new Query("select value from provisionRequest_service_success_${SERVICE_NAME}", DB_NAME)
        def provisionRequestServiceSuccessServiceQueryResult = influxDB.query(provisionRequestServiceSuccessServiceQuery)
        def provisionRequestServiceSuccessServiceResult = influxDBResultMapper.toPOJO(provisionRequestServiceSuccessServiceQueryResult, ProvisionRequestServiceSuccessServicePoint.class)

        and:
        def provisionRequestServiceFailServiceQuery = new Query("select value from provisionRequest_service_fail_${SERVICE_NAME}", DB_NAME)
        def provisionRequestServiceFailServiceQueryResult = influxDB.query(provisionRequestServiceFailServiceQuery)
        def provisionRequestServiceFailServiceResult = influxDBResultMapper.toPOJO(provisionRequestServiceFailServiceQueryResult, ProvisionRequestServiceFailServicePoint.class)

        and:
        def provisionRequestPlanTotalPlanQuery = new Query("select value from provisionRequest_plan_total_${PLAN_NAME}", DB_NAME)
        def provisionRequestPlanTotalPlanQueryResult = influxDB.query(provisionRequestPlanTotalPlanQuery)
        def provisionRequestPlanTotalPlanResult = influxDBResultMapper.toPOJO(provisionRequestPlanTotalPlanQueryResult, ProvisionRequestPlanTotalPlanPoint.class)

        and:
        def provisionRequestPlanSuccessPlanQuery = new Query("select value from provisionRequest_plan_success_${PLAN_NAME}", DB_NAME)
        def provisionRequestPlanSuccessPlanQueryResult = influxDB.query(provisionRequestPlanSuccessPlanQuery)
        def provisionRequestPlanSuccessPlanResult = influxDBResultMapper.toPOJO(provisionRequestPlanSuccessPlanQueryResult, ProvisionRequestPlanSuccessPlanPoint.class)

        and:
        def provisionRequestPlanFailPlanQuery = new Query("select value from provisionRequest_plan_fail_${PLAN_NAME}", DB_NAME)
        def provisionRequestPlanFailPlanQueryResult = influxDB.query(provisionRequestPlanFailPlanQuery)
        def provisionRequestPlanFailPlanResult = influxDBResultMapper.toPOJO(provisionRequestPlanFailPlanQueryResult, ProvisionRequestPlanFailPlanPoint.class)

        and:
        def provisionedInstancesTotalTotalQuery = new Query("select value from provisionedInstances_total_total", DB_NAME)
        def provisionedInstancesTotalTotalQueryResult = influxDB.query(provisionedInstancesTotalTotalQuery)
        def provisionedInstancesTotalTotalResult = influxDBResultMapper.toPOJO(provisionedInstancesTotalTotalQueryResult, ProvisionedInstancesTotalTotalPoint.class)

        and:
        def provisionedInstancesTotalSuccessQuery = new Query("select value from provisionedInstances_total_success", DB_NAME)
        def provisionedInstancesTotalSuccessQueryResult = influxDB.query(provisionedInstancesTotalSuccessQuery)
        def provisionedInstancesTotalSuccessResult = influxDBResultMapper.toPOJO(provisionedInstancesTotalSuccessQueryResult, ProvisionedInstancesTotalSuccessPoint.class)

        and:
        def provisionedInstancesTotalFailQuery = new Query("select value from provisionedInstances_total_fail", DB_NAME)
        def provisionedInstancesTotalFailQueryResult = influxDB.query(provisionedInstancesTotalFailQuery)
        def provisionedInstancesTotalFailResult = influxDBResultMapper.toPOJO(provisionedInstancesTotalFailQueryResult, ProvisionedInstancesTotalFailPoint.class)

        and:
        def provisionedInstancesSuccessRatioQuery = new Query("select value from provisionedInstances_success_ratio", DB_NAME)
        def provisionedInstancesSuccessRatioQueryResult = influxDB.query(provisionedInstancesSuccessRatioQuery)
        def provisionedInstancesSuccessRatioResult = influxDBResultMapper.toPOJO(provisionedInstancesSuccessRatioQueryResult, ProvisionedInstancesSuccessRatioPoint.class)

        and:
        def provisionedInstancesFailRatioQuery = new Query("select value from provisionedInstances_fail_ratio", DB_NAME)
        def provisionedInstancesFailRatioQueryResult = influxDB.query(provisionedInstancesFailRatioQuery)
        def provisionedInstancesFailRatioResult = influxDBResultMapper.toPOJO(provisionedInstancesFailRatioQueryResult, ProvisionedInstancesFailRatioPoint.class)

        and:
        def provisionedInstancesServiceTotalServiceQuery = new Query("select value from provisionedInstances_service_total_${SERVICE_NAME}", DB_NAME)
        def provisionedInstancesServiceTotalServiceQueryResult = influxDB.query(provisionedInstancesServiceTotalServiceQuery)
        def provisionedInstancesServiceTotalServiceResult = influxDBResultMapper.toPOJO(provisionedInstancesServiceTotalServiceQueryResult, ProvisionedInstancesServiceTotalServicePoint.class)

        and:
        def provisionedInstancesServiceSuccessServiceQuery = new Query("select value from provisionedInstances_service_success_${SERVICE_NAME}", DB_NAME)
        def provisionedInstancesServiceSuccessServiceQueryResult = influxDB.query(provisionedInstancesServiceSuccessServiceQuery)
        def provisionedInstancesServiceSuccessServiceResult = influxDBResultMapper.toPOJO(provisionedInstancesServiceSuccessServiceQueryResult, ProvisionedInstancesServiceSuccessServicePoint.class)

        and:
        def provisionedInstancesServiceFailServiceQuery = new Query("select value from provisionedInstances_service_fail_${SERVICE_NAME}", DB_NAME)
        def provisionedInstancesServiceFailServiceQueryResult = influxDB.query(provisionedInstancesServiceFailServiceQuery)
        def provisionedInstancesServiceFailServiceResult = influxDBResultMapper.toPOJO(provisionedInstancesServiceFailServiceQueryResult, ProvisionedInstancesServiceFailServicePoint.class)

        and:
        def provisionedInstancesPlanTotalPlanQuery = new Query("select value from provisionedInstances_plan_total_${PLAN_NAME}", DB_NAME)
        def provisionedInstancesPlanTotalPlanQueryResult = influxDB.query(provisionedInstancesPlanTotalPlanQuery)
        def provisionedInstancesPlanTotalPlanResult = influxDBResultMapper.toPOJO(provisionedInstancesPlanTotalPlanQueryResult, ProvisionedInstancesPlanTotalPlanPoint.class)

        and:
        def provisionedInstancesPlanSuccessPlanQuery = new Query("select value from provisionedInstances_plan_success_${PLAN_NAME}", DB_NAME)
        def provisionedInstancesPlanSuccessPlanQueryResult = influxDB.query(provisionedInstancesPlanSuccessPlanQuery)
        def provisionedInstancesPlanSuccessPlanResult = influxDBResultMapper.toPOJO(provisionedInstancesPlanSuccessPlanQueryResult, ProvisionedInstancesPlanSuccessPlanPoint.class)

        and:
        def provisionedInstancesPlanFailPlanQuery = new Query("select value from provisionedInstances_plan_fail_${PLAN_NAME}", DB_NAME)
        def provisionedInstancesPlanFailPlanQueryResult = influxDB.query(provisionedInstancesPlanFailPlanQuery)
        def provisionedInstancesPlanFailPlanResult = influxDBResultMapper.toPOJO(provisionedInstancesPlanFailPlanQueryResult, ProvisionedInstancesPlanFailPlanPoint.class)

        then:
        assert (provisionRequestTotalTotalResult.get(provisionRequestTotalTotalResult.size() - 1).value == 1.0)
        assert (provisionRequestTotalSuccessResult.get(provisionRequestTotalSuccessResult.size() - 1).value == 1.0)
        assert (provisionRequestTotalFailResult.get(provisionRequestTotalFailResult.size() - 1).value == 0.0)
        assert (provisionRequestSuccessRatioResult.get(provisionRequestSuccessRatioResult.size() - 1).value == 100.0)
        assert (provisionRequestFailRatioResult.get(provisionRequestFailRatioResult.size() - 1).value == 0.0)
        assert (provisionRequestServiceTotalServiceResult.get(provisionRequestServiceTotalServiceResult.size() - 1).value == 1.0)
        assert (provisionRequestServiceSuccessServiceResult.get(provisionRequestServiceSuccessServiceResult.size() - 1).value == 1.0)
        assert (provisionRequestServiceFailServiceResult.get(provisionRequestServiceFailServiceResult.size() - 1).value == 0.0)
        assert (provisionRequestPlanTotalPlanResult.get(provisionRequestPlanTotalPlanResult.size() - 1).value == 1.0)
        assert (provisionRequestPlanSuccessPlanResult.get(provisionRequestPlanSuccessPlanResult.size() - 1).value == 1.0)
        assert (provisionRequestPlanFailPlanResult.get(provisionRequestPlanFailPlanResult.size() - 1).value == 0.0)

        and:
        assert (provisionedInstancesTotalTotalResult.get(provisionedInstancesTotalTotalResult.size() - 1).value == 1.0)
        assert (provisionedInstancesTotalSuccessResult.get(provisionedInstancesTotalSuccessResult.size() - 1).value == 1.0)
        assert (provisionedInstancesTotalFailResult.get(provisionedInstancesTotalFailResult.size() - 1).value == 0.0)
        assert (provisionedInstancesSuccessRatioResult.get(provisionedInstancesSuccessRatioResult.size() - 1).value == 100.0)
        assert (provisionedInstancesFailRatioResult.get(provisionedInstancesFailRatioResult.size() - 1).value == 0.0)
        assert (provisionedInstancesServiceTotalServiceResult.get(provisionedInstancesServiceTotalServiceResult.size() - 1).value == 1.0)
        assert (provisionedInstancesServiceSuccessServiceResult.get(provisionedInstancesServiceSuccessServiceResult.size() - 1).value == 1.0)
        assert (provisionedInstancesServiceFailServiceResult.get(provisionedInstancesServiceFailServiceResult.size() - 1).value == 0.0)
        assert (provisionedInstancesPlanTotalPlanResult.get(provisionedInstancesPlanTotalPlanResult.size() - 1).value == 1.0)
        assert (provisionedInstancesPlanSuccessPlanResult.get(provisionedInstancesPlanSuccessPlanResult.size() - 1).value == 1.0)
        assert (provisionedInstancesPlanFailPlanResult.get(provisionedInstancesPlanFailPlanResult.size() - 1).value == 0.0)

        cleanup:
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
    }

    def "ignore deleted service instances in metrics regarding provisioned instances but include in provision requests"() {
        given:
        serviceBrokerClientExtended.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        Thread.sleep(WAIT_FOR_SERVICE_DEFINITION_TO_BE_DONE_IN_MILLISECONDS)

        when:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceBrokerClientExtended.createServiceInstance(new CreateServiceInstanceRequest(SERVICE_GUID, PLAN_GUID, null, null, null).withServiceInstanceId(serviceInstanceGuid).withAsyncAccepted(true))

        and:
        serviceBrokerClientExtended.deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceGuid, SERVICE_GUID, PLAN_GUID, true))
        Thread.sleep(TIME_TO_WAIT_FOR_WRITING_TO_INFLUXDB_TO_OCCUR_IN_MILLISECONDS)

        and:
        def provisionRequestTotalTotalQuery = new Query("select value from provisionRequest_total_total", DB_NAME)
        def provisionRequestTotalTotalQueryResult = influxDB.query(provisionRequestTotalTotalQuery)
        def provisionRequestTotalTotalResult = influxDBResultMapper.toPOJO(provisionRequestTotalTotalQueryResult, ProvisionRequestTotalTotalPoint.class)

        and:
        def provisionRequestTotalSuccessQuery = new Query("select value from provisionRequest_total_success", DB_NAME)
        def provisionRequestTotalSuccessQueryResult = influxDB.query(provisionRequestTotalSuccessQuery)
        def provisionRequestTotalSuccessResult = influxDBResultMapper.toPOJO(provisionRequestTotalSuccessQueryResult, ProvisionRequestTotalSuccessPoint.class)

        and:
        def provisionRequestTotalFailQuery = new Query("select value from provisionRequest_total_fail", DB_NAME)
        def provisionRequestTotalFailQueryResult = influxDB.query(provisionRequestTotalFailQuery)
        def provisionRequestTotalFailResult = influxDBResultMapper.toPOJO(provisionRequestTotalFailQueryResult, ProvisionRequestTotalFailPoint.class)

        and:
        def provisionRequestSuccessRatioQuery = new Query("select value from provisionRequest_success_ratio", DB_NAME)
        def provisionRequestSuccessRatioQueryResult = influxDB.query(provisionRequestSuccessRatioQuery)
        def provisionRequestSuccessRatioResult = influxDBResultMapper.toPOJO(provisionRequestSuccessRatioQueryResult, ProvisionRequestSuccessRatioPoint.class)

        and:
        def provisionRequestFailRatioQuery = new Query("select value from provisionRequest_fail_ratio", DB_NAME)
        def provisionRequestFailRatioQueryResult = influxDB.query(provisionRequestFailRatioQuery)
        def provisionRequestFailRatioResult = influxDBResultMapper.toPOJO(provisionRequestFailRatioQueryResult, ProvisionRequestFailRatioPoint.class)

        and:
        def provisionRequestServiceTotalServiceQuery = new Query("select value from provisionRequest_service_total_${SERVICE_NAME}", DB_NAME)
        def provisionRequestServiceTotalServiceQueryResult = influxDB.query(provisionRequestServiceTotalServiceQuery)
        def provisionRequestServiceTotalServiceResult = influxDBResultMapper.toPOJO(provisionRequestServiceTotalServiceQueryResult, ProvisionRequestServiceTotalServicePoint.class)

        and:
        def provisionRequestServiceSuccessServiceQuery = new Query("select value from provisionRequest_service_success_${SERVICE_NAME}", DB_NAME)
        def provisionRequestServiceSuccessServiceQueryResult = influxDB.query(provisionRequestServiceSuccessServiceQuery)
        def provisionRequestServiceSuccessServiceResult = influxDBResultMapper.toPOJO(provisionRequestServiceSuccessServiceQueryResult, ProvisionRequestServiceSuccessServicePoint.class)

        and:
        def provisionRequestServiceFailServiceQuery = new Query("select value from provisionRequest_service_fail_${SERVICE_NAME}", DB_NAME)
        def provisionRequestServiceFailServiceQueryResult = influxDB.query(provisionRequestServiceFailServiceQuery)
        def provisionRequestServiceFailServiceResult = influxDBResultMapper.toPOJO(provisionRequestServiceFailServiceQueryResult, ProvisionRequestServiceFailServicePoint.class)

        and:
        def provisionRequestPlanTotalPlanQuery = new Query("select value from provisionRequest_plan_total_${PLAN_NAME}", DB_NAME)
        def provisionRequestPlanTotalPlanQueryResult = influxDB.query(provisionRequestPlanTotalPlanQuery)
        def provisionRequestPlanTotalPlanResult = influxDBResultMapper.toPOJO(provisionRequestPlanTotalPlanQueryResult, ProvisionRequestPlanTotalPlanPoint.class)

        and:
        def provisionRequestPlanSuccessPlanQuery = new Query("select value from provisionRequest_plan_success_${PLAN_NAME}", DB_NAME)
        def provisionRequestPlanSuccessPlanQueryResult = influxDB.query(provisionRequestPlanSuccessPlanQuery)
        def provisionRequestPlanSuccessPlanResult = influxDBResultMapper.toPOJO(provisionRequestPlanSuccessPlanQueryResult, ProvisionRequestPlanSuccessPlanPoint.class)

        and:
        def provisionRequestPlanFailPlanQuery = new Query("select value from provisionRequest_plan_fail_${PLAN_NAME}", DB_NAME)
        def provisionRequestPlanFailPlanQueryResult = influxDB.query(provisionRequestPlanFailPlanQuery)
        def provisionRequestPlanFailPlanResult = influxDBResultMapper.toPOJO(provisionRequestPlanFailPlanQueryResult, ProvisionRequestPlanFailPlanPoint.class)

        and:
        def provisionedInstancesTotalTotalQuery = new Query("select value from provisionedInstances_total_total", DB_NAME)
        def provisionedInstancesTotalTotalQueryResult = influxDB.query(provisionedInstancesTotalTotalQuery)
        def provisionedInstancesTotalTotalResult = influxDBResultMapper.toPOJO(provisionedInstancesTotalTotalQueryResult, ProvisionedInstancesTotalTotalPoint.class)

        and:
        def provisionedInstancesTotalSuccessQuery = new Query("select value from provisionedInstances_total_success", DB_NAME)
        def provisionedInstancesTotalSuccessQueryResult = influxDB.query(provisionedInstancesTotalSuccessQuery)
        def provisionedInstancesTotalSuccessResult = influxDBResultMapper.toPOJO(provisionedInstancesTotalSuccessQueryResult, ProvisionedInstancesTotalSuccessPoint.class)

        and:
        def provisionedInstancesTotalFailQuery = new Query("select value from provisionedInstances_total_fail", DB_NAME)
        def provisionedInstancesTotalFailQueryResult = influxDB.query(provisionedInstancesTotalFailQuery)
        def provisionedInstancesTotalFailResult = influxDBResultMapper.toPOJO(provisionedInstancesTotalFailQueryResult, ProvisionedInstancesTotalFailPoint.class)

        and:
        def provisionedInstancesSuccessRatioQuery = new Query("select value from provisionedInstances_success_ratio", DB_NAME)
        def provisionedInstancesSuccessRatioQueryResult = influxDB.query(provisionedInstancesSuccessRatioQuery)
        def provisionedInstancesSuccessRatioResult = influxDBResultMapper.toPOJO(provisionedInstancesSuccessRatioQueryResult, ProvisionedInstancesSuccessRatioPoint.class)

        and:
        def provisionedInstancesFailRatioQuery = new Query("select value from provisionedInstances_fail_ratio", DB_NAME)
        def provisionedInstancesFailRatioQueryResult = influxDB.query(provisionedInstancesFailRatioQuery)
        def provisionedInstancesFailRatioResult = influxDBResultMapper.toPOJO(provisionedInstancesFailRatioQueryResult, ProvisionedInstancesFailRatioPoint.class)

        and:
        def provisionedInstancesServiceTotalServiceQuery = new Query("select value from provisionedInstances_service_total_${SERVICE_NAME}", DB_NAME)
        def provisionedInstancesServiceTotalServiceQueryResult = influxDB.query(provisionedInstancesServiceTotalServiceQuery)
        def provisionedInstancesServiceTotalServiceResult = influxDBResultMapper.toPOJO(provisionedInstancesServiceTotalServiceQueryResult, ProvisionedInstancesServiceTotalServicePoint.class)

        and:
        def provisionedInstancesServiceSuccessServiceQuery = new Query("select value from provisionedInstances_service_success_${SERVICE_NAME}", DB_NAME)
        def provisionedInstancesServiceSuccessServiceQueryResult = influxDB.query(provisionedInstancesServiceSuccessServiceQuery)
        def provisionedInstancesServiceSuccessServiceResult = influxDBResultMapper.toPOJO(provisionedInstancesServiceSuccessServiceQueryResult, ProvisionedInstancesServiceSuccessServicePoint.class)

        and:
        def provisionedInstancesServiceFailServiceQuery = new Query("select value from provisionedInstances_service_fail_${SERVICE_NAME}", DB_NAME)
        def provisionedInstancesServiceFailServiceQueryResult = influxDB.query(provisionedInstancesServiceFailServiceQuery)
        def provisionedInstancesServiceFailServiceResult = influxDBResultMapper.toPOJO(provisionedInstancesServiceFailServiceQueryResult, ProvisionedInstancesServiceFailServicePoint.class)

        and:
        def provisionedInstancesPlanTotalPlanQuery = new Query("select value from provisionedInstances_plan_total_${PLAN_NAME}", DB_NAME)
        def provisionedInstancesPlanTotalPlanQueryResult = influxDB.query(provisionedInstancesPlanTotalPlanQuery)
        def provisionedInstancesPlanTotalPlanResult = influxDBResultMapper.toPOJO(provisionedInstancesPlanTotalPlanQueryResult, ProvisionedInstancesPlanTotalPlanPoint.class)

        and:
        def provisionedInstancesPlanSuccessPlanQuery = new Query("select value from provisionedInstances_plan_success_${PLAN_NAME}", DB_NAME)
        def provisionedInstancesPlanSuccessPlanQueryResult = influxDB.query(provisionedInstancesPlanSuccessPlanQuery)
        def provisionedInstancesPlanSuccessPlanResult = influxDBResultMapper.toPOJO(provisionedInstancesPlanSuccessPlanQueryResult, ProvisionedInstancesPlanSuccessPlanPoint.class)

        and:
        def provisionedInstancesPlanFailPlanQuery = new Query("select value from provisionedInstances_plan_fail_${PLAN_NAME}", DB_NAME)
        def provisionedInstancesPlanFailPlanQueryResult = influxDB.query(provisionedInstancesPlanFailPlanQuery)
        def provisionedInstancesPlanFailPlanResult = influxDBResultMapper.toPOJO(provisionedInstancesPlanFailPlanQueryResult, ProvisionedInstancesPlanFailPlanPoint.class)

        then:
        assert (provisionRequestTotalTotalResult.get(provisionRequestTotalTotalResult.size() - 1).value == 1.0)
        assert (provisionRequestTotalSuccessResult.get(provisionRequestTotalSuccessResult.size() - 1).value == 1.0)
        assert (provisionRequestTotalFailResult.get(provisionRequestTotalFailResult.size() - 1).value == 0.0)
        assert (provisionRequestSuccessRatioResult.get(provisionRequestSuccessRatioResult.size() - 1).value == 100.0)
        assert (provisionRequestFailRatioResult.get(provisionRequestFailRatioResult.size() - 1).value == 0.0)
        assert (provisionRequestServiceTotalServiceResult.get(provisionRequestServiceTotalServiceResult.size() - 1).value == 1.0)
        assert (provisionRequestServiceSuccessServiceResult.get(provisionRequestServiceSuccessServiceResult.size() - 1).value == 1.0)
        assert (provisionRequestServiceFailServiceResult.get(provisionRequestServiceFailServiceResult.size() - 1).value == 0.0)
        assert (provisionRequestPlanTotalPlanResult.get(provisionRequestPlanTotalPlanResult.size() - 1).value == 1.0)
        assert (provisionRequestPlanSuccessPlanResult.get(provisionRequestPlanSuccessPlanResult.size() - 1).value == 1.0)
        assert (provisionRequestPlanFailPlanResult.get(provisionRequestPlanFailPlanResult.size() - 1).value == 0.0)

        and:
        assert (provisionedInstancesTotalTotalResult.get(provisionedInstancesTotalTotalResult.size() - 1).value == 0.0)
        assert (provisionedInstancesTotalSuccessResult.get(provisionedInstancesTotalSuccessResult.size() - 1).value == 0.0)
        assert (provisionedInstancesTotalFailResult.get(provisionedInstancesTotalFailResult.size() - 1).value == 0.0)
        assert (provisionedInstancesSuccessRatioResult.get(provisionedInstancesSuccessRatioResult.size() - 1).value == 0.0)
        assert (provisionedInstancesFailRatioResult.get(provisionedInstancesFailRatioResult.size() - 1).value == 0.0)
        assert (provisionedInstancesServiceTotalServiceResult.get(provisionedInstancesServiceTotalServiceResult.size() - 1).value == 0.0)
        assert (provisionedInstancesServiceSuccessServiceResult.get(provisionedInstancesServiceSuccessServiceResult.size() - 1).value == 0.0)
        assert (provisionedInstancesServiceFailServiceResult.get(provisionedInstancesServiceFailServiceResult.size() - 1).value == 0.0)
        assert (provisionedInstancesPlanTotalPlanResult.get(provisionedInstancesPlanTotalPlanResult.size() - 1).value == 0.0)
        assert (provisionedInstancesPlanSuccessPlanResult.get(provisionedInstancesPlanSuccessPlanResult.size() - 1).value == 0.0)
        assert (provisionedInstancesPlanFailPlanResult.get(provisionedInstancesPlanFailPlanResult.size() - 1).value == 0.0)

        cleanup:
        serviceInstanceRepository.delete(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
        cfServiceRepository.delete(cfServiceRepository.findByGuid(SERVICE_GUID))
        planRepository.delete(planRepository.findByGuid(PLAN_GUID))
    }
}
