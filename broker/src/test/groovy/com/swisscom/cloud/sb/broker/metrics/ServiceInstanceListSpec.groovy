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
