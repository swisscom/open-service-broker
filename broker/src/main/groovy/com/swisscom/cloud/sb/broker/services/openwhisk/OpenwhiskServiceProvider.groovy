package com.swisscom.cloud.sb.broker.services.openwhisk

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

/**
 * Created by zkhan on 7/10/17.
 */

@Component
@CompileStatic
@Slf4j
class OpenwhiskServiceProvider implements ServiceProvider{

    @Override
    ProvisionResponse provision(ProvisionRequest request){
        println "In provision OW"
        println "ProvisionRequest - request"
        println request.toString()
        return new ProvisionResponse(details: [], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request){
        println "In deprovision OW"
        println "DeprovisionRequest - request"
        println request.toString()
        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    BindResponse bind(BindRequest request){
        println "IN bind"
        println "BindRequest - request"
        println "app_guid " + request.app_guid
        return new BindResponse(details: [ServiceDetail.from("username", "pass")], credentials: new OpenwhiskBindResponseDto(user: "username1", pass: "password1"))
    }

    @Override
    void unbind(UnbindRequest request){
        println "In unbind"
    }
}
