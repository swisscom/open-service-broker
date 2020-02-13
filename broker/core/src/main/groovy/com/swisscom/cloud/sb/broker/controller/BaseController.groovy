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

package com.swisscom.cloud.sb.broker.controller

import com.swisscom.cloud.sb.broker.cfapi.converter.ErrorDtoConverter
import com.swisscom.cloud.sb.broker.cfapi.dto.ErrorDto
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.Errors
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

@Controller
@CompileStatic
@Slf4j
abstract class BaseController {

    protected ErrorDtoConverter errorDtoConverter

    BaseController() {
        this.errorDtoConverter = new ErrorDtoConverter()
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    ErrorDto handleException(MethodArgumentNotValidException e) {
        new ErrorDto(description: createValidationErrorMessage(e.getBindingResult()))
    }

    @ExceptionHandler
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ResponseBody
    ErrorDto exception(Exception e) {
        log.error("Uncaught error", e)
        return new ErrorDto(description: "Internal Server Error")
    }

    @ExceptionHandler
    ResponseEntity<ErrorDto> serviceBrokerException(ServiceBrokerException exception) {
        log.error("Service Broker exception", exception)
        return new ResponseEntity<ErrorDto>(errorDtoConverter.convert(exception),
                                            exception.httpStatus ?: HttpStatus.INTERNAL_SERVER_ERROR)
    }

    protected static ServiceInstance verifyServiceInstanceIsReady(ServiceInstance serviceInstance) {
        if (!serviceInstance.completed) {
            ErrorCode.SERVICE_INSTANCE_PROVISIONING_NOT_COMPLETED.throwNew("ID = " + serviceInstance.guid)
        }
        return serviceInstance
    }

    private static String createValidationErrorMessage(Errors errors) {
        "Validation failed. " + errors.getErrorCount() + " error(s): " + errors.getAllErrors().collect({
            it.getDefaultMessage()
        }).join(", ")
    }
}
