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

import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.certificate.CertificateCredential
import org.springframework.credhub.support.json.JsonCredential
import org.springframework.credhub.support.password.PasswordCredential
import org.springframework.credhub.support.permissions.Permission
import org.springframework.credhub.support.rsa.RsaCredential

interface CredHubService {
    CredentialDetails<JsonCredential> writeCredential(String name, Map<String, String> credentials)

    CredentialDetails<JsonCredential> getCredential(String id)

    CredentialDetails<PasswordCredential> getPasswordCredentialByName(String name)

    CredentialDetails<CertificateCredential> getCertificateCredentialByName(String name)

    CredentialDetails<CertificateCredential> generateCertificate(String name, CertificateConfig parameters)

    CredentialDetails<RsaCredential> generateRSA(String name)

    CredentialDetails<CertificateCredential> writeCertificate(String name, String certificate, String certificateAuthority, String privateKey)

    void deleteCredential(String name)

    List<Permission> getPermissions(String name)

    void addReadPermission(String name, String appGUID)

    void deletePermission(String name, String appGUID)
}
