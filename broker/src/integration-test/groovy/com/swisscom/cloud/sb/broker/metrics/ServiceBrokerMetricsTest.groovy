package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.metrics.measurements.BindingRequestServiceFailServicePoint
import com.swisscom.cloud.sb.broker.metrics.measurements.BindingRequestServiceSuccessServicePoint
import com.swisscom.cloud.sb.broker.metrics.measurements.BindingRequestServiceTotalServicePoint
import com.swisscom.cloud.sb.broker.metrics.measurements.BindingServiceTotalServicePoint
import com.swisscom.cloud.sb.broker.metrics.measurements.LifecycleTimeServiceTotalServicePoint
import com.swisscom.cloud.sb.broker.metrics.measurements.MeasurementPoint
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query
import org.influxdb.impl.InfluxDBResultMapper
import org.springframework.beans.factory.annotation.Autowired

class ServiceBrokerMetricsTest extends BaseSpecification {

    final String DB_NAME = "mydb"
    final String SERVICE_NAME = "service"
    final ArrayList<MeasurementPoint> metricsRetrievableFromDB = [new MeasurementPoint("binding_total_total"), new MeasurementPoint("fake"), new MeasurementPoint("provisionRequest_fail_ratio"), new MeasurementPoint("provisionRequest_success_ratio"), new MeasurementPoint("provisionRequest_total_fail"), new MeasurementPoint("provisionRequest_total_success"), new MeasurementPoint("provisionRequest_total_total"), new MeasurementPoint("provisionedInstances_fail_ratio"), new MeasurementPoint("provisionedInstances_success_ratio"), new MeasurementPoint("provisionedInstances_total_fail"), new MeasurementPoint("provisionedInstances_total_success"), new MeasurementPoint("provisionedInstances_total_total")]
    final ArrayList<MeasurementPoint> dynamicallyGeneratedMetrics = [new MeasurementPoint("bindingRequest_service_fail_${SERVICE_NAME}"), new MeasurementPoint("bindingRequest_service_success_${SERVICE_NAME}"), new MeasurementPoint("bindingRequest_service_total_${SERVICE_NAME}"), new MeasurementPoint("binding_service_total_${SERVICE_NAME}"), new MeasurementPoint("lifecycleTime_service_total_${SERVICE_NAME}")]

    InfluxDB influxDB
    InfluxDBResultMapper influxDBResultMapper

    @Autowired
    PlanRepository planRepository
    @Autowired
    CFServiceRepository cfServiceRepository
    @Autowired
    ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    ServiceBindingRepository serviceBindingRepository

    def setup() {
        influxDB =InfluxDBFactory.connect("http://localhost:8086", "admin", "admin")
        if(!influxDB.databaseExists(DB_NAME)){
            influxDB.createDatabase(DB_NAME)
        }
        influxDBResultMapper = new InfluxDBResultMapper()

    }

    def cleanup() {
        cfServiceRepository.deleteAll()
        serviceInstanceRepository.deleteAll()
        planRepository.deleteAll()
        serviceBindingRepository.deleteAll()

        influxDB.deleteDatabase(DB_NAME)
        influxDB.close()
    }

    def "only measurements that are retrievable from the database are recorded when db is empty and initialized with 0"() {
        given:
        Thread.sleep(10000)
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
        assert(results.size() == metricsRetrievableFromDB.size())
        assert(results.name.containsAll(metricsRetrievableFromDB.name))
        assert(!results.name.containsAll(dynamicallyGeneratedMetrics.name))

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
        cfServiceRepository.save(new CFService(guid: UUID.randomUUID().toString(), name: 'service'))
        Thread.sleep(10000)

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
        assert (bindingServiceTotalServiceResult.get(bindingServiceTotalServiceResult.size()-1).value == 0.0)
        assert (bindingRequestServiceTotalServiceResult.get(bindingServiceTotalServiceResult.size()-1).value == 0.0)
        assert (bindingRequestServiceSuccessServiceResult.get(bindingRequestServiceSuccessServiceResult.size()-1).value == 0.0)
        assert (bindingRequestServiceFailServiceResult.get(bindingRequestServiceFailServiceResult.size()-1).value == 0.0)
        assert (lifecycleTimeServiceTotalServiceResult.get(lifecycleTimeServiceTotalServiceResult.size()-1).value == 0.0)
    }
}
