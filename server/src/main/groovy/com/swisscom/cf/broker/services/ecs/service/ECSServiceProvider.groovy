package com.swisscom.cf.broker.services.ecs.service

import com.google.common.base.Optional
import com.swisscom.cf.broker.binding.BindRequest
import com.swisscom.cf.broker.binding.BindResponse
import com.swisscom.cf.broker.binding.UnbindRequest
import com.swisscom.cf.broker.cfextensions.serviceusage.ServiceUsage
import com.swisscom.cf.broker.cfextensions.serviceusage.ServiceUsageProvider
import com.swisscom.cf.broker.model.DeprovisionRequest
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.DeprovisionResponse
import com.swisscom.cf.broker.provisioning.ProvisionResponse
import com.swisscom.cf.broker.services.common.ServiceProvider
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.dto.ECSBindResponse
import com.swisscom.cf.broker.services.ecs.facade.ECSManagementFacade
import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.SharedSecretKeyManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputDecorator
import com.swisscom.cf.broker.util.ServiceDetailKey
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j
class ECSServiceProvider implements ServiceProvider, ServiceUsageProvider {

    @Autowired
    ECSConfig ecsConfig

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        ECSManagementInputDecorator ecsManagementInputDecorator = new ECSManagementInputDecorator(ecsConfig: ecsConfig)
        ECSManagementClient ecsManagementClient = new ECSManagementClient(createNamespace: new NamespaceManager(ecsConfig),
                createUser: new UserManager(ecsConfig), sharedSecretKeyManager: new SharedSecretKeyManager(ecsConfig))
        return (new ECSManagementFacade(ecsManagementInputDecorator, ecsManagementClient)).provision()
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {

        return new DeprovisionResponse(isAsync: false)
    }


    @Override
    BindResponse bind(BindRequest request) {
        return new BindResponse(credentials: new ECSBindResponse(accessHost: ecsConfig.getEcsClientURL(),
                accessKey: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.ECS_NAMESPACE_USER),
                sharedSecret: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.ECS_NAMESPACE_SECRET)))
    }

    @Override
    void unbind(UnbindRequest request) {
        request.getServiceInstance()
    }

    //TODO check if /object/billing/namespace/info is in bytes the same units as atmos? and check atmos as well
    @Override
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
        //Date date = enddate.present ? enddate.get() : new Date()
        return new ServiceUsage(value: 1, type: ServiceUsage.Type.WATERMARK)
    }
}