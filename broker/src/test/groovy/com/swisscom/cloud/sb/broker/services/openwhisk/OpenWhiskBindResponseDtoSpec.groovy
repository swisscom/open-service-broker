package com.swisscom.cloud.sb.broker.services.openwhisk

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

class OpenWhiskBindResponseDtoSpec extends Specification{

    def "Verify json serialization works correctly"() {
        given:
        OpenWhiskBindResponseDto openWhiskBindResponseDto = new OpenWhiskBindResponseDto(openwhiskExecutionUrl: "/v1/execution/",
                                                                                        openwhiskAdminUrl: "/v1/admin/",
                                                                                        openwhiskUUID: "TEST_UUID",
                                                                                        openwhiskKey: "TEST_KEY",
                                                                                        openwhiskNamespace: "TEST_NAMESPACE",
                                                                                        openwhiskSubject: "TEST_SUBJECT")

        and:
        String expected = """{
                                "credentials": {
                                    "executionUrl": "/v1/execution/",
                                    "adminUrl": "/v1/admin/",
                                    "uuid": "TEST_UUID",
                                    "key": "TEST_KEY",
                                    "namespace": "TEST_NAMESPACE",
                                    "subject": "TEST_SUBJECT"
                                }
                             }"""

        expect:
        JSONAssert.assertEquals(expected, openWhiskBindResponseDto.toJson(), true)
    }
}
