package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import org.hibernate.validator.constraints.Range
import spock.lang.Specification

import javax.validation.constraints.NotNull


class NotNullClass {
    NotNullClass() {}

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

    void "Validation throws a ServiceProvider exception if enum has wrong value"() {
        given:
        def json = '{"notEmpty":"NotEmpty", "specificValue":"invalid"}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, NotNullClass.class)

        then:
        def ex = thrown(ServiceBrokerException)
    }

    void "Validation is okay if DTO is complex and okay"() {
        given:
        def json = '{"notEmpty":"NotEmpty","specificValue":"valid","numberRange":20}'

        when:
        def result = DtoValidationHelper.deserializeAndValidate(json, NotNullClass.class)

        then:
        noExceptionThrown()

    }
}
