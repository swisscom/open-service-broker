package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.metrics.measurements.BindingServiceTotalServicePoint
import com.swisscom.cloud.sb.broker.metrics.measurements.BindingTotalTotalPoint
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.influxdb.impl.InfluxDBResultMapper
import org.springframework.beans.factory.annotation.Autowired

class BindingMetricsServiceTest extends BaseSpecification {

    final String DB_NAME = "mydb"
    final String BINDING_TOTAL_TOTAL = "binding_total_total"

    InfluxDB influxDB
    InfluxDBResultMapper influxDBResultMapper

    @Autowired
    DBTestUtil dbTestUtil
    @Autowired
    PlanRepository planRepository
    @Autowired
    CFServiceRepository cfServiceRepository
    @Autowired
    ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    ServiceBindingRepository serviceBindingRepository
    @Autowired
    BindingMetricsService bindingMetricsService

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
        bindingMetricsService.totalBindingRequestsPerService.clear()
        bindingMetricsService.totalSuccessfulBindingRequestsPerService.clear()
        bindingMetricsService.totalFailedBindingRequestsPerService.clear()

        influxDB.deleteDatabase(DB_NAME)
        influxDB.close()
    }

    def "zero value for total bindings written to influxDB when there are no bindings"() {
        given:
        Query query = new Query("select value from ${BINDING_TOTAL_TOTAL}", DB_NAME)
        QueryResult queryResult = influxDB.query(query)

        when:
        def influxDBResultMapper = new InfluxDBResultMapper()
        def result = influxDBResultMapper.toPOJO(queryResult, BindingTotalTotalPoint.class)

        then:
        assert (result.get(result.size()-1).value == 0.0)

    }

    def "update value for total bindings and bindings per service upon binding a service"() {
        given:
        def plan = dbTestUtil.createPlan('plan1', UUID.randomUUID().toString(), true, 'test', 'id')
        CFService cfService = cfServiceRepository.save(new CFService(guid: UUID.randomUUID().toString(), name: 'service'))
        cfService.plans.add(plan)
        cfServiceRepository.save(cfService)

        and:
        def serviceInstance = dbTestUtil.createServiceInstace(cfService, UUID.randomUUID().toString())
        dbTestUtil.createServiceBinding(cfService, serviceInstance, UUID.randomUUID().toString(), null)
        Thread.sleep(10000)

        when:
        def bindingTotalTotalQuery = new Query("select value from ${BINDING_TOTAL_TOTAL}", DB_NAME)
        def bindingTotalTotalQueryResult = influxDB.query(bindingTotalTotalQuery)
        def bindingTotalTotalResult = influxDBResultMapper.toPOJO(bindingTotalTotalQueryResult, BindingTotalTotalPoint.class)

        and:
        def bindingServiceTotalServiceQuery = new Query("select value from binding_service_total_service", DB_NAME)
        def bindingServiceTotalServiceQueryResult = influxDB.query(bindingServiceTotalServiceQuery)
        def bindingServiceTotalServiceResult = influxDBResultMapper.toPOJO(bindingServiceTotalServiceQueryResult, BindingServiceTotalServicePoint.class)

        then:
        assert (bindingTotalTotalResult.get(bindingTotalTotalResult.size()-1).value == 1.0)
        assert (bindingServiceTotalServiceResult.get(bindingServiceTotalServiceResult.size()-1).value == 1.0)
    }
}
