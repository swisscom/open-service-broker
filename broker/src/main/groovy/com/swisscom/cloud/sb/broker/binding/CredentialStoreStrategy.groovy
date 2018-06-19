package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.ServiceBinding

interface CredentialStoreStrategy {

    def writeCredential(ServiceBinding serviceBinding, String credentialJson)

    def deleteCredential(ServiceBinding serviceBinding)

    String getCredential(ServiceBinding serviceBinding)

}
