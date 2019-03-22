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

package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.cfapi.dto.ErrorDto
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import spock.lang.Specification


class ErrorDtoConverterSpec extends Specification {

    def 'Can convert ServiceBrokerException correctly'() {
        given:
        def testee = new ErrorDtoConverter()

        when:
        ErrorDto result
        try {
            ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.throwNew("CUSTOM ERROR MESSAGE")
        } catch (ServiceBrokerException ex) {
            result = testee.convert(ex)
        }

        then:
        result.description == "Serviceprovider for the selected Plan encountered an Error " + "CUSTOM ERROR MESSAGE"
        result.code == ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.code
        result.error_code == ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.errorCode
    }
}