package com.swisscom.cf.broker.controller

import com.swisscom.cf.broker.cfapi.converter.ErrorDtoConverter
import com.swisscom.cf.broker.cfapi.dto.ErrorDto
import com.swisscom.cf.broker.error.ErrorCode
import com.swisscom.cf.broker.error.ServiceBrokerException
import com.swisscom.cf.broker.model.Plan
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cf.broker.services.common.ServiceProvider
import com.swisscom.cf.broker.services.common.ServiceProviderLookup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

@Controller
@CompileStatic
@Slf4j
abstract class BaseController {

    @Autowired
    protected ServiceProviderLookup serviceProviderLookup

    @Autowired
    protected ErrorDtoConverter errorDtoConverter

    @Autowired
    protected ServiceInstanceRepository serviceInstanceRepository

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
        return new ResponseEntity<ErrorDto>(errorDtoConverter.convert(exception), exception.httpStatus);
    }

    protected ServiceProvider findServiceProvider(Plan plan) {
        serviceProviderLookup.findServiceProvider(plan)
    }

    protected ServiceInstance getAndCheckServiceInstance(String serviceInstanceId) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceId)
        if (!serviceInstance) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        }
        if (serviceInstance.deleted) {
            ErrorCode.SERVICE_INSTANCE_DELETED.throwNew()
        }
        if (!serviceInstance.completed) {
            ErrorCode.SERVICE_INSTANCE_PROVISIONING_NOT_COMPLETED.throwNew()
        }
        return serviceInstance
    }

}
