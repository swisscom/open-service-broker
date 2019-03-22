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

package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.controller.BaseController
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@CompileStatic
class DummyController extends BaseController {

    @Autowired
    DummyExtension dummyExtension

    @RequestMapping(value = '/custom/service_instances/{instanceId}/lock', method = RequestMethod.PUT)
    ResponseEntity<String> lockUser(@PathVariable("instanceId") String serviceInstanceGuid) {
        return new ResponseEntity<String>(dummyExtension.lockUser(serviceInstanceGuid), HttpStatus.OK)
    }

    @RequestMapping(value = '/custom/service_instances/{instanceId}/unlock', method = RequestMethod.PUT)
    ResponseEntity<String> unlockUser(@PathVariable("instanceId") String serviceInstanceGuid) {
        return new ResponseEntity<String>(dummyExtension.unlockUser(serviceInstanceGuid), HttpStatus.ACCEPTED)
    }
}
