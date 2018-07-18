package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import org.apache.commons.lang.time.DateUtils
import spock.lang.Specification

class ServiceInstanceListSpec extends Specification {

    void "Average Lifetime returns the correct value (all dates set)"() {
        given:
        def serviceInstanceList = new ServiceInstanceList()
        def expectedDifference = 0

        and:
        def date = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-16 11:00:00")
        serviceInstanceList.add(new ServiceInstance(
                dateCreated: date,
                dateDeleted: DateUtils.addDays(date, 30)
        ))
        expectedDifference += 30 * 24 * 60 * 60

        and:
        date = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-05 11:00:00")
        serviceInstanceList.add(new ServiceInstance(
                dateCreated: date,
                dateDeleted: DateUtils.addDays(date, 5)
        ))
        expectedDifference += 5 * 24 * 60 * 60

        when:
        def differenceInSeconds = serviceInstanceList.lifecycleTimeInSeconds()

        then:
        Math.abs(expectedDifference - differenceInSeconds) < 1
    }

    void "Average Lifetime returns the correct value (only undeleted)"() {
        given:
        def serviceInstanceList = new ServiceInstanceList()
        def expectedDifference = 0
        def date = new java.util.Date()

        and:
        date = new java.util.Date()
        serviceInstanceList.add(new ServiceInstance(
                dateCreated: DateUtils.addMinutes(date, -30)
        ))
        expectedDifference += 30 * 60

        when:
        def differenceInSeconds = serviceInstanceList.lifecycleTimeInSeconds()

        then:
        Math.abs(expectedDifference - differenceInSeconds) < 1
    }

    void "Average Lifetime returns the correct value"() {
        given:
        def serviceInstanceList = new ServiceInstanceList()
        def expectedDifference = 0

        and:
        def date = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-16 11:00:00")
        serviceInstanceList.add(new ServiceInstance(
                dateCreated: date,
                dateDeleted: DateUtils.addDays(date, 30)
        ))
        expectedDifference += 30 * 24 * 60 * 60

        and:
        date = Date.parse("yyyy-MM-dd hh:mm:ss", "2018-05-05 11:00:00")
        serviceInstanceList.add(new ServiceInstance(
                dateCreated: date,
                dateDeleted: DateUtils.addDays(date, 5)
        ))
        expectedDifference += 5 * 24 * 60 * 60

        and:
        date = new java.util.Date()
        serviceInstanceList.add(new ServiceInstance(
                dateCreated: DateUtils.addMinutes(date, -30)
        ))
        expectedDifference += 30 * 60

        when:
        def differenceInSeconds = serviceInstanceList.lifecycleTimeInSeconds()

        then:
        Math.abs(expectedDifference - differenceInSeconds) < 1
    }
}
