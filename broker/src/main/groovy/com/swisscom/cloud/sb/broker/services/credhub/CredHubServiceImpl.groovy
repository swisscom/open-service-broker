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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.credhub.core.CredHubException
import org.springframework.credhub.core.CredHubOperations
import org.springframework.credhub.core.credential.CredHubCredentialOperations
import org.springframework.credhub.core.credential.CredHubCredentialTemplate
import org.springframework.credhub.core.permission.CredHubPermissionOperations
import org.springframework.credhub.core.permission.CredHubPermissionTemplate
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.SimpleCredentialName
import org.springframework.credhub.support.WriteMode
import org.springframework.credhub.support.certificate.CertificateCredential
import org.springframework.credhub.support.certificate.CertificateCredentialRequest
import org.springframework.credhub.support.certificate.CertificateParameters
import org.springframework.credhub.support.certificate.CertificateParametersRequest
import org.springframework.credhub.support.json.JsonCredential
import org.springframework.credhub.support.json.JsonCredentialRequest
import org.springframework.credhub.support.password.PasswordCredential
import org.springframework.credhub.support.permissions.Actor
import org.springframework.credhub.support.permissions.Operation
import org.springframework.credhub.support.permissions.Permission
import org.springframework.credhub.support.rsa.RsaCredential
import org.springframework.credhub.support.rsa.RsaParametersRequest
import org.springframework.stereotype.Service

@Service
@CompileStatic
@Slf4j
@ConditionalOnProperty(name = "spring.credhub.enable", havingValue = "true")
class CredHubServiceImpl implements CredHubService {

    @Autowired
    private CredHubOperations credHubOperations

    private CredHubPermissionOperations credHubPermissionOperations

    private CredHubCredentialOperations credHubCredentialOperations

    CredHubServiceImpl(CredHubOperations credHubOperations) {
        this.credHubOperations = credHubOperations
    }

    CredHubPermissionOperations getCredHubPermissionOperations() {
        return credHubPermissionOperations ? credHubPermissionOperations : new CredHubPermissionTemplate(credHubOperations)
    }

    CredHubCredentialOperations getCredHubCredentialOperations() {
        return credHubCredentialOperations ? credHubCredentialOperations : new CredHubCredentialTemplate(credHubOperations)
    }

    @Override
    CredentialDetails<JsonCredential> writeCredential(String name, Map<String, String> credentials) {
        log.info("Writing new CredHub Credential for name: ${name}")
        JsonCredential jsonCredential = new JsonCredential(credentials)
        JsonCredentialRequest request =
                JsonCredentialRequest.builder()
                        .name(new SimpleCredentialName('/' + name))
                        .value(jsonCredential)
                        .mode(WriteMode.CONVERGE)
                        .build()
        getCredHubCredentialOperations().write(request)
    }

    @Override
    CredentialDetails<JsonCredential> getCredential(String id) {
        log.info("Get CredHub credentials for id: ${id}")
        getCredHubCredentialOperations().getById(id, JsonCredential)
    }

    @Override
    CredentialDetails<PasswordCredential> getPasswordCredentialByName(String name) {
        log.info("Get CredHub credentials for name: ${name}")
        getCredHubCredentialOperations().getByName(new SimpleCredentialName(name), PasswordCredential)
    }

    @Override
    CredentialDetails<CertificateCredential> getCertificateCredentialByName(String name) {
        log.info("Get CredHub credentials for name: ${name}")
        getCredHubCredentialOperations().getByName(new SimpleCredentialName(name), CertificateCredential)
    }

    @Override
    void deleteCredential(String name) {
        log.info("Delete CredHub credentials for name: ${name}")
        ignore404 {
            getCredHubCredentialOperations().deleteByName(new SimpleCredentialName('/' + name))
        }
    }

    @Override
    CredentialDetails<RsaCredential> generateRSA(String name) {
        def request = RsaParametersRequest.builder()
                .name(new SimpleCredentialName('/' + name))
                .build()
        getCredHubCredentialOperations().generate(request)
    }

    @Override
    CredentialDetails<CertificateCredential> generateCertificate(String name, CertificateConfig parameters) {
        log.info("Writing new CredHub Credential for name: ${name}")
        def request = CertificateParametersRequest.builder()
                .name(new SimpleCredentialName('/' + name))
                .parameters(CertificateParameters.builder()
                .keyLength(parameters.keyLength)
                .commonName(parameters.commonName)
                .organizationUnit(parameters.organizationUnit)
                .organization(parameters.organization)
                .locality(parameters.locality)
                .state(parameters.state)
                .country(parameters.country)
                .duration(parameters.duration)
                .certificateAuthority(parameters.certificateAuthority)
                .certificateAuthorityCredential(parameters.certificateAuthorityCredential)
                .selfSign(parameters.selfSign)
                .build())
                .build()
        getCredHubCredentialOperations().generate(request)
    }

    @Override
    CredentialDetails<CertificateCredential> writeCertificate(String name, String certificate, String certificateAuthority, String privateKey) {
        CertificateCredential certificateCredential = new CertificateCredential(certificate, certificateAuthority, privateKey)
        CertificateCredentialRequest request = CertificateCredentialRequest.builder()
                .name(new SimpleCredentialName('/' + name))
                .value(certificateCredential).build()
        getCredHubCredentialOperations().write(request)
    }

    List<Permission> getPermissions(String name) {
        log.info("Retrieving permissions for CredHub Credential: ${name}")
        getCredHubPermissionOperations().getPermissions(new SimpleCredentialName("/" + name))
    }

    void addReadPermission(String name, String appGUID) {
        log.info("Adding read permission for CredHub Credential: ${name} to app: ${appGUID}")
        getCredHubPermissionOperations().addPermissions(new SimpleCredentialName("/" + name), Permission.builder().app(appGUID).operation(Operation.READ).build())
    }

    void deletePermission(String name, String appGUID) {
        log.info("Deleting permission for CredHub Credential: ${name} to app: ${appGUID}")
        ignore404 {
            getCredHubPermissionOperations().deletePermission(new SimpleCredentialName("/" + name), Actor.app(appGUID))
        }
    }

    String getVersion() {
        def version = credHubOperations.info().version().version
        log.info("Version = " + version)
        version
    }

    static void ignore404(Closure c) {
        try {
            c()
        } catch (CredHubException ex) {
            // Currently CredHubExceptions can not be differentiated, workaround with message parsing
            if (ex.getMessage() =~ /^Error calling CredHub: 404/) {
                log.info("CredHubException, ignoring 404 error.")
            } else {
                throw ex
            }
        }
    }
}
