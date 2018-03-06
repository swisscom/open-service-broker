package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.controller.BaseController
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@Api(value = "Dummy", description = "Dummy endpoints")
@RestController
@CompileStatic
class DummyController extends BaseController {

    @Autowired
    DummyExtension dummyExtension

    @ApiOperation(value = "Lock user")
    @RequestMapping(value = '/custom/service_instances/{instanceId}/lock', method = RequestMethod.PUT)
    ResponseEntity<String> lockUser(@PathVariable("instanceId") String serviceInstanceGuid) {
        return new ResponseEntity<String>(dummyExtension.lockUser(serviceInstanceGuid), HttpStatus.OK)
    }
}
