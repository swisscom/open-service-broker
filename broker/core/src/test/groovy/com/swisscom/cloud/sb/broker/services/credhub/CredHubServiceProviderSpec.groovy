package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import groovy.json.JsonSlurper
import org.springframework.credhub.support.password.PasswordCredential
import org.springframework.credhub.support.permissions.Operation
import org.springframework.credhub.support.permissions.Permission
import org.springframework.http.HttpStatus
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.CredentialType
import org.springframework.credhub.support.SimpleCredentialName
import spock.lang.Specification

import static com.swisscom.cloud.sb.broker.services.credhub.CredHubServiceDetailKey.CREDHUB_CREDENTIAL_NAME
import static com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper.from


class CredHubServiceProviderSpec extends Specification{

    private final String SERVICE_INSTANCE = "TEST_UUID"

    private CredHubServiceProvider credHubServiceProvider
    private CredHubServiceImpl credHubServiceImpl
    private ServiceBindingRepository serviceBindingRepository

    def setup() {
        credHubServiceImpl = Mock(CredHubServiceImpl)
        serviceBindingRepository = Mock(ServiceBindingRepository)
        credHubServiceProvider = new CredHubServiceProvider(credHubServiceImpl, serviceBindingRepository)
    }

    def "get credential name non-existing"(){
        given:
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> null

        then:
        assert credHubServiceProvider.getCredential(name) == null
        noExceptionThrown()
    }

    def "get credential name existing"(){
        given:
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        def cred = new CredentialDetails("id", new SimpleCredentialName(name), CredentialType.JSON, PasswordCredential)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> cred

        then:
        assert credHubServiceProvider.getCredential(name) == cred
        noExceptionThrown()
    }

    def "provision credential"(){
        given:
        def cred = '{"password": "pass"}'
        def jsonSlurper = new JsonSlurper()
        Map object = (Map)jsonSlurper.parseText(cred)
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: SERVICE_INSTANCE, plan: new Plan(), parameters: cred)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> null
        credHubServiceImpl.writeCredential(name, object) >> new CredentialDetails("id", new SimpleCredentialName(name), CredentialType.JSON, PasswordCredential)

        then:
        assert from(credHubServiceProvider.provision(provisionRequest).details).getValue(CREDHUB_CREDENTIAL_NAME) == "swisscom-service-broker/credhub/" + SERVICE_INSTANCE + "/credentials"
        noExceptionThrown()
    }

    def "provision existing credential"(){
        given:
        def cred = '{"password": "pass"}'
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: SERVICE_INSTANCE, plan: new Plan(), parameters: cred)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> new CredentialDetails("id", new SimpleCredentialName(name), CredentialType.JSON, PasswordCredential)
        credHubServiceProvider.provision(provisionRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.CONFLICT
    }

    def "provision credential without JSON"(){
        given:
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: SERVICE_INSTANCE, plan: new Plan(), parameters: null)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> null
        credHubServiceProvider.provision(provisionRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST
    }

    def "deprovision credential"(){
        given:
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(serviceInstanceGuid: SERVICE_INSTANCE)

        when:
        credHubServiceProvider.deprovision(deprovisionRequest)

        then:
        noExceptionThrown()
    }

    def "update credential"(){
        given:
        def cred = '{"password": "pass"}'
        def jsonSlurper = new JsonSlurper()
        Map object = (Map)jsonSlurper.parseText(cred)
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        UpdateRequest updateRequest = new UpdateRequest(serviceInstanceGuid: SERVICE_INSTANCE, parameters: cred)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> new CredentialDetails("id", new SimpleCredentialName(name), CredentialType.JSON, PasswordCredential)
        credHubServiceImpl.writeCredential(name, object) >> new CredentialDetails("id", new SimpleCredentialName(name), CredentialType.JSON, PasswordCredential)

        then:
        assert from(credHubServiceProvider.update(updateRequest).details).getValue(CREDHUB_CREDENTIAL_NAME) == "swisscom-service-broker/credhub/" + SERVICE_INSTANCE + "/credentials"
        noExceptionThrown()
    }

    def "update non-existing credential"(){
        given:
        def cred = '{"password": "pass"}'
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        UpdateRequest updateRequest = new UpdateRequest(serviceInstanceGuid: SERVICE_INSTANCE, parameters: cred)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> null
        credHubServiceProvider.update(updateRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.NOT_FOUND
    }

    def "update credential no json"(){
        given:
        def cred = null
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        UpdateRequest updateRequest = new UpdateRequest(serviceInstanceGuid: SERVICE_INSTANCE, parameters: cred)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> new CredentialDetails("id", new SimpleCredentialName(name), CredentialType.JSON, PasswordCredential)
        credHubServiceProvider.update(updateRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST
    }

    def "create credential key"(){
        given:
        def name = "cred-key"

        when:
        def key = credHubServiceProvider.constructKey(name)

        then:
        assert key ==  "swisscom-service-broker/credhub/cred-key/credentials"
        noExceptionThrown()
    }

    def "bind credential"(){
        given:
        def app_guid = "app-guid"
        def service_guid = "service-instance"
        BindRequest bindRequest = new BindRequest(serviceInstance: new ServiceInstance(guid: service_guid), app_guid: app_guid)
        List<Permission> permissions = [Permission.builder().app(app_guid).operation(Operation.READ).build()]

        when:
        credHubServiceImpl.getPermissions(credHubServiceProvider.constructKey(service_guid)) >> permissions
        credHubServiceProvider.bind(bindRequest)

        then:
        noExceptionThrown()
    }

    def "unsuccessful bind credential"(){
        given:
        def app_guid = "app-guid"
        def service_guid = "service-instance"
        BindRequest bindRequest = new BindRequest(serviceInstance: new ServiceInstance(guid: service_guid), app_guid: app_guid)
        List<Permission> permissions = [Permission.builder().app("something").operation(Operation.READ).build()]

        when:
        credHubServiceImpl.getPermissions(credHubServiceProvider.constructKey(service_guid)) >> permissions
        credHubServiceProvider.bind(bindRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST
    }


}
