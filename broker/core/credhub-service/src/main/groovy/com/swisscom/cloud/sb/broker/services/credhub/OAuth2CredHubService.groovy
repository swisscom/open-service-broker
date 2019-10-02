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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.credhub.configuration.CredHubTemplateFactory
import org.springframework.credhub.core.CredHubException
import org.springframework.credhub.core.CredHubOperations
import org.springframework.credhub.core.CredHubProperties
import org.springframework.credhub.core.credential.CredHubCredentialOperations
import org.springframework.credhub.core.credential.CredHubCredentialTemplate
import org.springframework.credhub.core.permission.CredHubPermissionOperations
import org.springframework.credhub.core.permission.CredHubPermissionTemplate
import org.springframework.credhub.support.ClientOptions
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.SimpleCredentialName
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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

import static com.google.common.base.Preconditions.checkState
import static com.google.common.base.Preconditions.checkArgument
import static org.apache.commons.lang.StringUtils.isNotBlank

@CompileStatic
class OAuth2CredHubService implements CredHubService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2CredHubService.class)

    public static final String ERROR_CLOSURE_MANDATORY = "closure may not be null"
    public static final String ERROR_NAME_MANDATORY = "name may not be null or empty"
    public static final String ERROR_APPGUID_MANDATORY = "appGUID may not be null or empty"
    public static final String ERROR_CERTIFICATE_MANDATORY = "certificate may not be null or empty"
    public static final String ERROR_CERTIFICATEAUTHORITY_MANDATORY = "certificateAuthority may not be null or empty"
    public static final String ERROR_PRIVATEKEY_MANDATORY = "privateKey may not be null or empty"
    public static final String ERROR_PARAMETERS_MANDATORY = "parameters may not be null"
    public static final String ERROR_CREDENTIALS_MANDATORY = "credentials may not be null"
    public static final String ERROR_ID_MANDATORY = "id may not be null or empty"
    public static final String ERROR_REQUEST_NULL = "failed generating valid request"

    private final CredHubOperations credHubOperations

    private CredHubPermissionOperations credHubPermissionOperations

    private CredHubCredentialOperations credHubCredentialOperations

    private OAuth2CredHubService(
            URI credHubUri,
            String oAuth2RegistrationId,
            ClientOptions clientOptions,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        this.credHubOperations = new CredHubTemplateFactory()
            .credHubTemplate(
                    new CredHubProperties(
                            url: credHubUri.toString(),
                            oauth2: new CredHubProperties.OAuth2(registrationId:  oAuth2RegistrationId)),
                    clientOptions,
                    clientRegistrationRepository,
                    authorizedClientService
            )
    }

    static OAuth2CredHubService of(
            URI credHubUri,
            String oAuth2RegistrationId,
            ClientOptions clientOptions,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        checkArgument(credHubUri != null)
        checkArgument(isNotBlank(oAuth2RegistrationId))
        checkArgument(clientOptions != null)
        checkArgument(clientRegistrationRepository != null)
        checkArgument(authorizedClientService != null)

        return new OAuth2CredHubService(credHubUri, oAuth2RegistrationId, clientOptions, clientRegistrationRepository, authorizedClientService)
    }

    CredHubPermissionOperations getCredHubPermissionOperations() {
        if (credHubPermissionOperations == null) {
            checkState(credHubOperations != null)
            credHubPermissionOperations = new CredHubPermissionTemplate(credHubOperations)
        }

        return credHubPermissionOperations
    }

    CredHubCredentialOperations getCredHubCredentialOperations() {
        if (credHubCredentialOperations == null) {
            checkState(credHubOperations != null)
            credHubCredentialOperations = new CredHubCredentialTemplate(credHubOperations)
        }

        return credHubCredentialOperations
    }

    @Override
    CredentialDetails<JsonCredential> writeCredential(String name, Map<String, String> credentials) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        checkArgument(credentials != null && credentials.size() > 0, ERROR_CREDENTIALS_MANDATORY)
        LOGGER.info("Writing new CredHub Credential for name: ${name}")

        JsonCredential jsonCredential = new JsonCredential(credentials)
        JsonCredentialRequest request =
                JsonCredentialRequest.builder()
                        .name(new SimpleCredentialName('/' + name))
                        .value(jsonCredential)
                        .build()
        checkState(request !=  null, ERROR_REQUEST_NULL)

        return getCredHubCredentialOperations().write(request)
    }

    @Override
    CredentialDetails<JsonCredential> getCredential(String id) {
        checkArgument(isNotBlank(id), ERROR_ID_MANDATORY)

        LOGGER.info("Get CredHub credentials for id: ${id}")

        return getCredHubCredentialOperations().getById(id, JsonCredential)
    }

    @Override
    CredentialDetails<PasswordCredential> getPasswordCredentialByName(String name) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        LOGGER.info("Get CredHub credentials for name: ${name}")

        return getCredHubCredentialOperations().getByName(new SimpleCredentialName(name), PasswordCredential)
    }

    @Override
    CredentialDetails<CertificateCredential> getCertificateCredentialByName(String name) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        LOGGER.info("Get CredHub credentials for name: ${name}")

        return getCredHubCredentialOperations().getByName(new SimpleCredentialName(name), CertificateCredential)
    }

    @Override
    void deleteCredential(String name) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        LOGGER.info("Delete CredHub credentials for name: ${name}")

        ignore404 {
            getCredHubCredentialOperations().deleteByName(new SimpleCredentialName('/' + name))
        }
    }
    @Override
    CredentialDetails<RsaCredential> generateRSA(String name) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)

        def request = RsaParametersRequest.builder()
                .name(new SimpleCredentialName('/' + name))
                .build()
        checkState(request !=  null, ERROR_REQUEST_NULL)

        getCredHubCredentialOperations().generate(request)
    }

    @Override
    CredentialDetails<CertificateCredential> generateCertificate(String name, CertificateConfig parameters) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        checkArgument(parameters != null, ERROR_PARAMETERS_MANDATORY)

        LOGGER.info("Writing new CredHub Credential for name: ${name}")

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
        checkState(request !=  null, ERROR_REQUEST_NULL)

        return getCredHubCredentialOperations().generate(request)
    }

    @Override
    CredentialDetails<CertificateCredential> writeCertificate(String name, String certificate, String certificateAuthority, String privateKey) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        checkArgument(isNotBlank(certificate), ERROR_CERTIFICATE_MANDATORY)
        checkArgument(isNotBlank(certificateAuthority), ERROR_CERTIFICATEAUTHORITY_MANDATORY)
        checkArgument(isNotBlank(privateKey), ERROR_PRIVATEKEY_MANDATORY)

        CertificateCredential certificateCredential = new CertificateCredential(certificate, certificateAuthority, privateKey)

        CertificateCredentialRequest request = CertificateCredentialRequest.builder()
                .name(new SimpleCredentialName('/' + name))
                .value(certificateCredential).build()
        checkState(request !=  null, ERROR_REQUEST_NULL)

        return getCredHubCredentialOperations().write(request)
    }

    List<Permission> getPermissions(String name) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        LOGGER.info("Retrieving permissions for CredHub Credential: ${name}")

        return getCredHubPermissionOperations().getPermissions(new SimpleCredentialName("/" + name))
    }

    void addReadPermission(String name, String appGUID) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        checkArgument(isNotBlank(appGUID), ERROR_APPGUID_MANDATORY)
        LOGGER.info("Adding read permission for CredHub Credential: ${name} to app: ${appGUID}")
        getCredHubPermissionOperations().addPermissions(new SimpleCredentialName("/" + name), Permission.builder().app(appGUID).operation(Operation.READ).build())
    }

    void deletePermission(String name, String appGUID) {
        checkArgument(isNotBlank(name), ERROR_NAME_MANDATORY)
        checkArgument(isNotBlank(appGUID), ERROR_APPGUID_MANDATORY)
        LOGGER.info("Deleting permission for CredHub Credential: ${name} to app: ${appGUID}")

        ignore404 {
            getCredHubPermissionOperations().deletePermission(new SimpleCredentialName("/" + name), Actor.app(appGUID))
        }
    }

    String getVersion() {
        def version = credHubOperations.info().version().version
        LOGGER.info("Version = " + version)

        return version
    }

    static void ignore404(Closure c) {
        checkArgument(c != null, ERROR_CLOSURE_MANDATORY)

        try {
            c()
        } catch (CredHubException ex) {
            // Currently CredHubExceptions can not be differentiated, workaround with message parsing
            if (ex.getMessage() =~ /404/) {
                LOGGER.info("CredHubException, ignoring 404 error.")
            } else {
                throw ex
            }
        }
    }
}
