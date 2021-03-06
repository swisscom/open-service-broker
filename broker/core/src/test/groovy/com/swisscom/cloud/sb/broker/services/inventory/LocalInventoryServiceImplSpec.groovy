package com.swisscom.cloud.sb.broker.services.inventory

import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import org.springframework.data.util.Pair
import spock.lang.Specification
import spock.lang.Unroll

class LocalInventoryServiceImplSpec extends Specification {

    ServiceInstanceRepository serviceInstanceRepository
    ServiceDetailRepository serviceDetailRepository
    LocalInventoryServiceImpl testee

    void setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceInstanceRepository.save(_) >> {args -> args[0]}
        serviceDetailRepository = Mock(ServiceDetailRepository)
        serviceDetailRepository.save(_) >> {args -> args[0]}

        testee = new LocalInventoryServiceImpl(serviceInstanceRepository, serviceDetailRepository)
    }

    @Unroll
    void "should return '#flag' for has '#key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid(serviceInstance.guid) >> serviceInstance

        when:
        def result = testee.has(serviceInstance.guid, key)

        then:
        noExceptionThrown()
        result == flag

        where:
        flag                                   | key
        true                                   | "key_001"
        true                                   | "key_003"
        false                                  | "key_004"
    }


    @Unroll
    void "should throw exception when trying to has keyValuePairs with invalid arguments, instance guid '#guid' and key '#key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.has(guid, key)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | key       | message
        null                                   | "key_001" | "service instance guid cannot be null or empty"
        ""                                     | "key_001" | "service instance guid cannot be null or empty"
        " "                                    | "key_001" | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | null      | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""        | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | "  "      | "key cannot be null or empty"
    }

    void "should return correct instance from mock"() {
        given:
        def instance = new ServiceInstance(guid: "00")

        when:
        def newInstance = serviceInstanceRepository.save(instance)

        then:
        instance.guid == newInstance.guid
        instance.dateCreated == newInstance.dateCreated
    }

    void "should throw exception when service instance is missing"() {
        given:
        def guid = UUID.randomUUID().toString()

        when:
        def result = testee.get(guid, "key_test")

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "service instance with id:${guid} not found"
    }

    void "should get single pair if key exists"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ServiceDetail.from(key, value)]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid, key)

        then:
        result != null
        result.first == key
        result.second == value
    }

    void "should throw exception when key is missing"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ServiceDetail.from(key, value)]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid, "key_002")

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "no details for key:key_002 found"
    }

    void "should return correct value even though default value is set"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ServiceDetail.from(key, value)]
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

    void "should return default value when key is missing"() {
        given:
        def guid = UUID.randomUUID().toString()
        def key = "key_001"
        def value = "value_001"
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ServiceDetail.from(key, value)]
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

    void "should get multiple existing values"() {
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

    void "should get multiple values with identical keys"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.getAll(guid, "key_001")

        then:
        noExceptionThrown()
        result != null
        result.size() == 3
        result.get(0).first == "key_001"
        result.get(0).second == "value_001"
        result.get(1).first == "key_001"
        result.get(1).second == "value_002"
        result.get(2).first == "key_001"
        result.get(2).second == "value_003"
    }

    void "should throw exception when trying to single get multiple identical keys"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid, "key_001")

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "multiple details for key:key_001 found"
    }

    void "should get multiple none existing values"() {
        given:
        def guid = UUID.randomUUID().toString()
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

    void "should add a value"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_002", "value_002")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        testee.set(guid, Pair.of("key_003", "value_003"))
        def result = testee.get(guid)

        then:
        result != null
        result.size() == 3
        result.get(0).first == "key_001"
        result.get(0).second == "value_001"
        result.get(1).first == "key_002"
        result.get(1).second == "value_002"
        result.get(2).first == "key_003"
        result.get(2).second == "value_003"
    }

    void "should update a value"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_002", "value_002")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        testee.set(guid, Pair.of("key_002", "value_002_updated"))
        def result = testee.get(guid)

        then:
        result != null
        result.size() == 2
        result.get(0).first == "key_001"
        result.get(0).second == "value_001"
        result.get(1).first == "key_002"
        result.get(1).second == "value_002_updated"
    }

    void "should delete a value"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001"),
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

    void "should replace multiple values"() {
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

    void "should append multiple values"() {
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

    void "should replace multiple values with identical keys correctly"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_001", "value_001",),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        int i = 0
        serviceInstance.details.each {d -> d.id = ++i}

        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        String[] data = ["value_002", "value_004"]
        testee.createOrReplaceByKey(guid, "key_001", data)
        def result = testee.get(guid)

        then:
        result != null
        result.size() == 3
        result.get(0).first == "key_001"
        result.get(0).second == "value_002"
        result.get(1).first == "key_003"
        result.get(1).second == "value_003"
        result.get(2).first == "key_001"
        result.get(2).second == "value_004"
    }

    void "should add multiple values with identical keys while using replace correctly"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        String[] data = ["value_002", "value_004"]
        testee.createOrReplaceByKey(guid, "key_001", data)
        def result = testee.get(guid)

        then:
        result != null
        result.size() == 3
        result.get(0).first == "key_003"
        result.get(0).second == "value_003"
        result.get(1).first == "key_001"
        result.get(1).second == "value_002"
        result.get(2).first == "key_001"
        result.get(2).second == "value_004"
    }

    void "should delete everything when replacing with empty list"() {
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
        result != null
        result.size() == 0
    }

    void "should not return details with null value"() {
        given:
        def guid = UUID.randomUUID().toString()
        def serviceInstance = new ServiceInstance(
                guid: guid,
                details: [ServiceDetail.from("key_001", null)]
        )
        serviceInstanceRepository.findByGuid(guid) >> serviceInstance

        when:
        def result = testee.get(guid)

        then:
        noExceptionThrown()
        result != null
        result.size() == 0
    }

    @Unroll
    void "should throw exception when trying to get keyValuePairs with invalid arguments, instance guid '#guid' and key '#key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.get(guid, key)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | key       | message
        null                                   | "key_001" | "service instance guid cannot be null or empty"
        ""                                     | "key_001" | "service instance guid cannot be null or empty"
        " "                                    | "key_001" | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | null      | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""        | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | "  "      | "key cannot be null or empty"
    }

    @Unroll
    void "should throw exception when trying to get keyValuePairs with default with invalid arguments, instance guid '#guid' and key '#key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.get(guid, key, null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | key       | message
        null                                   | "key_001" | "service instance guid cannot be null or empty"
        ""                                     | "key_001" | "service instance guid cannot be null or empty"
        " "                                    | "key_001" | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | null      | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""        | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | "  "      | "key cannot be null or empty"
    }

    @Unroll
    void "should throw exception when trying to getAll keyValuePairs with invalid arguments, instance guid '#guid' and key '#key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.getAll(guid, key)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | key       | message
        null                                   | "key_001" | "service instance guid cannot be null or empty"
        ""                                     | "key_001" | "service instance guid cannot be null or empty"
        " "                                    | "key_001" | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | null      | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""        | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | "  "      | "key cannot be null or empty"
    }

    @Unroll
    void "should throw exception when trying to get All keyValuePairs with invalid arguments, instance guid '#guid'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.get(guid)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | message
        null                                   | "service instance guid cannot be null or empty"
        ""                                     | "service instance guid cannot be null or empty"
        " "                                    | "service instance guid cannot be null or empty"
    }

    @Unroll
    void "should throw exception when trying to set keyValuePair with invalid arguments, instance guid '#guid' and data '#data_key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.set(guid, Pair.of(data_key, "value_001"))

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | data_key                            | message
        null                                   | "key_001"                           | "service instance guid cannot be null or empty"
        ""                                     | "key_001"                           | "service instance guid cannot be null or empty"
        " "                                    | "key_001"                           | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""                                  | "detail key may not be blank"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | " "                                 | "detail key may not be blank"
    }

    @Unroll
    void "should throw exception when trying to replace keyValuePair with invalid arguments, instance guid '#guid' and data '#data_key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.replace(guid, [ Pair.of(data_key, "value_001") ])

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | data_key                            | message
        null                                   | "key_001"                           | "service instance guid cannot be null or empty"
        ""                                     | "key_001"                           | "service instance guid cannot be null or empty"
        " "                                    | "key_001"                           | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""                                  | "detail key may not be blank"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | " "                                 | "detail key may not be blank"
    }

    @Unroll
    void "should throw exception when trying to append keyValuePair with invalid arguments, instance guid '#guid' and data '#data_key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.replace(guid, [ Pair.of(data_key, "value_001") ])

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | data_key                            | message
        null                                   | "key_001"                           | "service instance guid cannot be null or empty"
        ""                                     | "key_001"                           | "service instance guid cannot be null or empty"
        " "                                    | "key_001"                           | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""                                  | "detail key may not be blank"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | " "                                 | "detail key may not be blank"
    }

    @Unroll
    void "should throw exception when trying to delete keyValuePairs with invalid arguments, instance guid '#guid' and key '#key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance

        when:
        testee.delete(guid, key)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | key       | message
        null                                   | "key_001" | "service instance guid cannot be null or empty"
        ""                                     | "key_001" | "service instance guid cannot be null or empty"
        " "                                    | "key_001" | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | null      | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""        | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | "  "      | "key cannot be null or empty"
    }

    @Unroll
    void "should throw exception when trying to replaceByKey keyValuePairs with invalid arguments, instance guid '#guid' and key '#key'"() {
        given:
        def serviceInstance = new ServiceInstance(
                guid: "d6445c23-275f-48b0-ac80-d0a04b3eae46",
                details: [
                        ServiceDetail.from("key_001", "value_001"),
                        ServiceDetail.from("key_001", "value_002"),
                        ServiceDetail.from("key_001", "value_003"),
                        ServiceDetail.from("key_003", "value_003")
                ]
        )
        serviceInstanceRepository.findByGuid("d6445c23-275f-48b0-ac80-d0a04b3eae46") >> serviceInstance
        String[] values = [ "value_1", "value_2" ]


        when:
        testee.createOrReplaceByKey(guid, key, values)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == message

        where:
        guid                                   | key       | message
        null                                   | "key_001" | "service instance guid cannot be null or empty"
        ""                                     | "key_001" | "service instance guid cannot be null or empty"
        " "                                    | "key_001" | "service instance guid cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | null      | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | ""        | "key cannot be null or empty"
        "d6445c23-275f-48b0-ac80-d0a04b3eae46" | "  "      | "key cannot be null or empty"
    }
}