package com.swisscom.cloud.sb.broker.services.inventory

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import org.springframework.data.util.Pair
import spock.lang.Specification


class LocalInventoryServiceImplSpec extends Specification {

    ServiceInstanceRepository serviceInstanceRepository
    ServiceDetailRepository serviceDetailRepository
    LocalInventoryServiceImpl testee


    void setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceInstanceRepository.save(_) >> { args -> args[0] }
        serviceDetailRepository = Mock(ServiceDetailRepository)
        serviceDetailRepository.save(_) >> { args -> args[0] }

        testee = new LocalInventoryServiceImpl(serviceInstanceRepository, serviceDetailRepository)
    }

    void "mock verification test"() {
        given:
        def instance = new ServiceInstance(guid: "00")

        when:
        def newInstance = serviceInstanceRepository.save(instance)

        then:
        noExceptionThrown()
        instance.guid == newInstance.guid
        instance.dateCreated == newInstance.dateCreated
    }

    void "exception thrown when service instance is missing"() {
        given:
        def guid = UUID.randomUUID().toString()

        when:
        def result = testee.get(guid, "key_test")

        then:
        def ex = thrown(ServiceBrokerException)
        ex.description.contains(guid)
    }

    void "can get single existing value"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ ServiceDetail.from(key, value) ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid, key)

        then:
        noExceptionThrown()
        result != null
        result.first == key
        result.second == value
    }

    void "exception thrown when key is missing"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ ServiceDetail.from(key, value) ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid, "key_002")

        then:
        def ex = thrown(ServiceBrokerException)
        ex.description.contains("key_002")
    }

    void "returns correct value even though default value is set"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ ServiceDetail.from(key, value) ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid, "key_001", "default_value_001")

        then:
        noExceptionThrown()
        result != null
        result.first == key
        result.second == value
    }

    void "returns default value when key is missing"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ ServiceDetail.from(key, value) ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid, "key_002", "default_value_001")

        then:
        noExceptionThrown()
        result != null
        result.first == "key_002"
        result.second == "default_value_001"
    }

    void "can get multiple existing values"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from(key, value),
                        ServiceDetail.from("key_002", "value_002"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 3
        result.get(0).first == "key_001"
        result.get(0).second == "value_001"
        result.get(1).first == "key_002"
        result.get(1).second == "value_002"
        result.get(2).first == "key_003"
        result.get(2).second == "value_003"
    }

    void "can get multiple none existing values"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: []
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 0
    }

    void "can add a value"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from(key, value),
                        ServiceDetail.from("key_002", "value_002")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        testee.set(guid, Pair.of("key_003", "value_003"))
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 3
        result.get(0).first == "key_001"
        result.get(0).second == "value_001"
        result.get(1).first == "key_002"
        result.get(1).second == "value_002"
        result.get(2).first == "key_003"
        result.get(2).second == "value_003"
    }

    void "can update a value"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from(key, value),
                        ServiceDetail.from("key_002", "value_002")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        testee.set(guid, Pair.of("key_002", "value_002_updated"))
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 2
        result.get(0).first == "key_001"
        result.get(0).second == "value_001"
        result.get(1).first == "key_002"
        result.get(1).second == "value_002_updated"
    }

    void "can delete a value"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from(key, value),
                        ServiceDetail.from("key_002", "value_002")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        testee.delete(guid, "key_002")
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 1
        result.get(0).first == "key_001"
        result.get(0).second == "value_001"
    }
    
    void "can replace multiple values"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_002", "value_002"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        testee.replace(guid, [
                Pair.of("key_001", "value_001_updated"),
                Pair.of("key_003", "value_003_updated"),
                Pair.of("key_004", "value_004_added"),
                Pair.of("key_005", "value_005_added")
        ])
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 4
        result.get(0).first == "key_001"
        result.get(0).second == "value_001_updated"
        result.get(1).first == "key_003"
        result.get(1).second == "value_003_updated"
        result.get(2).first == "key_004"
        result.get(2).second == "value_004_added"
        result.get(3).first == "key_005"
        result.get(3).second == "value_005_added"
    }

    void "can append multiple values"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_002", "value_002"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        testee.append(guid, [
                Pair.of("key_004", "value_004_added"),
                Pair.of("key_005", "value_005_added")
        ])
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 5
        result.get(0).first == "key_001"
        result.get(0).second == "value_001"
        result.get(1).first == "key_002"
        result.get(1).second == "value_002"
        result.get(2).first == "key_003"
        result.get(2).second == "value_003"
        result.get(3).first == "key_004"
        result.get(3).second == "value_004_added"
        result.get(4).first == "key_005"
        result.get(4).second == "value_005_added"
    }

    void "can empty"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_002", "value_002"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        testee.replace(guid, [])
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 0
    }

    void "null values returns as not existing"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ ServiceDetail.from("key_001", null) ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 0
    }
}