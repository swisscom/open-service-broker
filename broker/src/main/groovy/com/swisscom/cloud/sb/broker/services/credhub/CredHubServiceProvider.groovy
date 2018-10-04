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

package com.swisscom.cloud.sb.broker.services.credhub

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageProvider
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.broker.util.SensitiveParameterProvider
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.credhub.core.CredHubException
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.permissions.CredentialPermission
import org.springframework.stereotype.Component

import static com.swisscom.cloud.sb.broker.model.ServiceDetail.from
import static com.swisscom.cloud.sb.broker.services.credhub.CredHubServiceDetailKey.*

@Component
@CompileStatic
@Slf4j
class CredHubServiceProvider implements ServiceProvider, ServiceUsageProvider, SensitiveParameterProvider{

    private final CredHubServiceImpl credHubServiceImpl
    private final ServiceBindingRepository serviceBindingRepository

    @Autowired
    CredHubServiceProvider(CredHubServiceImpl credHubServiceImpl, ServiceBindingRepository serviceBindingRepository){
        this.credHubServiceImpl = credHubServiceImpl
        this.serviceBindingRepository = serviceBindingRepository
    }

    @Override
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
        return null
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {

        def res = getCredential(constructKey(request.serviceInstanceGuid))

        if (res instanceof CredentialDetails){
            ErrorCode.SERVICE_INSTANCE_ALREADY_EXISTS.throwNew('Credential already exists. Use update method.')
        }

        if(request.parameters == null) {
            log.error("No credentials.")
            ErrorCode.INVALID_JSON.throwNew('Credential JSON is required.')
        }

        def jsonSlurper = new JsonSlurper()
        Map object = (Map)jsonSlurper.parseText(request.parameters)

        def val = credHubServiceImpl.writeCredential(constructKey(request.serviceInstanceGuid), object)

        return new ProvisionResponse(details: [from(CREDHUB_CREDENTIAL_ID, val.id),
                                               from(CREDHUB_CREDENTIAL_NAME, val.name.getName()),
                                               from(CREDHUB_CREDENTIAL_TYPE, val.credentialType.getValueType()),
                                               from(CREDHUB_CREDENTIAL_TIMESTAMP, val.versionCreatedAt.toString())], isAsync: false)
    }

    ///credhub-service-broker/credhub/ac517e09-2f5e-475a-bf87-ca4275faa536/credentials
    String constructKey(String serviceInstance) {
        "swisscom-service-broker/credhub/" + serviceInstance + "/credentials"
    }

    CredentialDetails getCredential(String name){

        try {
            return credHubServiceImpl.getPasswordCredentialByName(name)
        } catch (CredHubException ex) {
            log.info("CredHubException error exception = ${ex}")
            log.info("Credential does not exist")
            return null
        }
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        credHubServiceImpl.deleteCredential(constructKey(request.serviceInstanceGuid))
        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    BindResponse bind(BindRequest request) {
        try {
            credHubServiceImpl.addReadPermission(constructKey(request.serviceInstance.guid), request.app_guid)
        } catch(Exception ex) {
            log.error("Exception = " + ex.toString())
        }

        List<CredentialPermission> res = credHubServiceImpl.getPermissions(constructKey(request.serviceInstance.guid))

        Boolean flag = false
        for (item in res) {
            if (item.actor.primaryIdentifier == request.app_guid){
                flag = true
                break
            }
        }

        if(!flag) {
            log.error("Permission not added.")
            ErrorCode.SERVICEBROKERSERVICEPROVIDER_BINDING_BAD_REQUEST.throwNew("Permission not added. Service instance not bound.")
        }

        return new BindResponse(details: [from(CREDHUB_CREDENTIAL_NAME, "/" + constructKey(request.serviceInstance.guid)),
                                          from(request.binding_guid, request.app_guid)],
                credentials: new CredHubResponseDto(credhubName: "/" + constructKey(request.serviceInstance.guid))
        )
    }

    @Override
    void unbind(UnbindRequest request) {
        credHubServiceImpl.deletePermissions(constructKey(request.serviceInstance.guid), ServiceDetailsHelper.from(serviceBindingRepository.findByGuid(request.binding.guid).details).getValue(request.binding.guid))
    }

    @Override
    UpdateResponse update(UpdateRequest request) {
        def res = getCredential(constructKey(request.serviceInstanceGuid))

        if (res == null){
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew("Credential doesn't exists Use create method.")
        }

        if(request.parameters == null) {
            log.error("No credentials.")
            ErrorCode.INVALID_JSON.throwNew('Credential JSON is required.')
        }

        def jsonSlurper = new JsonSlurper()
        Map object = (Map)jsonSlurper.parseText(request.parameters)

        def val = credHubServiceImpl.writeCredential(constructKey(request.serviceInstanceGuid), object)

        return new UpdateResponse(details: [from(CREDHUB_CREDENTIAL_ID, val.id),
                                            from(CREDHUB_CREDENTIAL_NAME, val.name.getName()),
                                            from(CREDHUB_CREDENTIAL_TYPE, val.credentialType.getValueType()),
                                            from(CREDHUB_CREDENTIAL_TIMESTAMP, val.versionCreatedAt.toString())], isAsync: false)
    }

    @Override
    Collection<Extension> buildExtensions() {
        return null
    }
}
