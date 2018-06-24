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

package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import spock.lang.Specification

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class NotNullClass {

    Integer numberRange
    String mayBeEmpty
    @NotNull
    String notEmpty

    NotNullClassEnum specificValue
}

enum NotNullClassEnum {
    valid('valid'),
    partiallyValid('partiallyValid')

    final String value

    NotNullClassEnum(String value) { this.value = value }

    @Override
    String toString() {
        return value
    }
}

class ValidationTest {
    @Size(min = 2, max = 5)
    Integer[] arrayTest = [1, 2, 3]

    // Range not supported because groovy doesn't handle long/int conversion correctly
    // @Range(min = 10, max = 100)
    Integer rangeTest = 50
}

class DtoValidationHelperSpec extends Specification {

    void "Validation throws a ServiceProvider exception"() {
        given:
        def json = '{"numberRange":40}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, NotNullClass.class)

        then:
        def ex = thrown(ServiceBrokerException)
        ex.message.contains("notEmpty")
    }

    void "Validation is okay if DTO is okay"() {
        given:
        def json = '{"notEmpty":"NotEmpty"}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, NotNullClass.class)

        then:
        noExceptionThrown()
    }

    void "Validation is not okay if additional Fields are present"() {
        given:
        def json = '{"notEmpty":"NotEmpty","doesNotExist":true}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, NotNullClass.class)

        then:
        def ex = thrown(ServiceBrokerException)
    }

    void "Validation is okay if a not mandatory field is omitted"() {
        given:
        def json = '{"notEmpty":"NotEmpty"}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, NotNullClass.class)

        then:
        noExceptionThrown()
    }

    void "Validation throws a ServiceProvider exception if enum has wrong value"() {
        given:
        def json = '{"notEmpty":"NotEmpty", "specificValue":"invalid"}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, NotNullClass.class)

        then:
        def ex = thrown(ServiceBrokerException)
        ex.message.contains("specificValue")
    }

    void "Validation is okay if DTO is complex and okay"() {
        given:
        def json = '{"notEmpty":"NotEmpty","specificValue":"valid","numberRange":20}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, NotNullClass.class)

        then:
        noExceptionThrown()
    }

    void "Validation for Range is okay"() {
        given:
        def json = '{"arrayTest":[1,2,3,4],"rangeTest":50}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, ValidationTest.class)

        then:
        noExceptionThrown()
    }

    void "Validation for Range is throws if the few elements are present"() {
        given:
        def json = '{"arrayTest":[1],"rangeTest":50}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, ValidationTest.class)

        then:
        thrown(ServiceBrokerException)
    }

    void "Validation for Range is doesn't throw if is null"() {
        given:
        def json = '{"arrayTest":null,"rangeTest":50}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, ValidationTest.class)

        then:
        noExceptionThrown()
    }
}
