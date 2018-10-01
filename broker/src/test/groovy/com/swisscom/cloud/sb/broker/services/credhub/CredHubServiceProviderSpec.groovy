package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import groovy.json.JsonSlurper
import org.springframework.credhub.support.password.PasswordCredential
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

    def setup() {
        credHubServiceImpl = Mock(CredHubServiceImpl)
        credHubServiceProvider = new CredHubServiceProvider(credHubServiceImpl)
    }

    def "get credential name non-existing"(){
        given:
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> null

        then:
        credHubServiceProvider.getCredential(name) == null
        noExceptionThrown()
    }

    def "get credential name existing"(){
        given:
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        def cred = new CredentialDetails<PasswordCredential>(name: new SimpleCredentialName(name), id: "id", credentialType: CredentialType.JSON, value: "value", versionCreatedAt: new Date())

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> cred

        then:
        credHubServiceProvider.getCredential(name) == cred
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
        credHubServiceImpl.writeCredential(name, object) >> new CredentialDetails(name: new SimpleCredentialName(name), id: "id", credentialType: CredentialType.JSON, value: "value", versionCreatedAt: new Date())

        then:
        from(credHubServiceProvider.provision(provisionRequest).details).getValue(CREDHUB_CREDENTIAL_NAME) == "swisscom-service-broker/credhub/" + SERVICE_INSTANCE + "/credentials"
        noExceptionThrown()
    }

    def "provision existing credential"(){
        given:
        def cred = '{"password": "pass"}'
        def name = credHubServiceProvider.constructKey(SERVICE_INSTANCE)
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: SERVICE_INSTANCE, plan: new Plan(), parameters: cred)

        when:
        credHubServiceImpl.getPasswordCredentialByName(name) >> new CredentialDetails<PasswordCredential>(name: new SimpleCredentialName(name), id: "id", credentialType: CredentialType.JSON, value: "value", versionCreatedAt: new Date())
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
        credHubServiceImpl.getPasswordCredentialByName(name) >> new CredentialDetails<PasswordCredential>(name: new SimpleCredentialName(name), id: "id", credentialType: CredentialType.JSON, value: "value", versionCreatedAt: new Date())
        credHubServiceImpl.writeCredential(name, object) >> new CredentialDetails(name: new SimpleCredentialName(name), id: "id", credentialType: CredentialType.JSON, value: "value", versionCreatedAt: new Date())

        then:
        from(credHubServiceProvider.update(updateRequest).details).getValue(CREDHUB_CREDENTIAL_NAME) == "swisscom-service-broker/credhub/" + SERVICE_INSTANCE + "/credentials"
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
        credHubServiceImpl.getPasswordCredentialByName(name) >> new CredentialDetails<PasswordCredential>(name: new SimpleCredentialName(name), id: "id", credentialType: CredentialType.JSON, value: "value", versionCreatedAt: new Date())
        credHubServiceProvider.update(updateRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST
    }
}
