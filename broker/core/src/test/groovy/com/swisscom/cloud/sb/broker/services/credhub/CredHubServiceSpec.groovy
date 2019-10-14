package com.swisscom.cloud.sb.broker.services.credhub

import org.springframework.credhub.core.CredHubException
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException
import spock.lang.Specification

class CredHubServiceSpec extends Specification {
    def "CredHubException is ignored with HttpStatusCodeException 404"() {
        when:
        byte[] body = "{\"error\":\"The request could not be completed because the credential does not exist or you do not have sufficient authorization.\"}".getBytes()
        HttpStatusCodeException httpStatusCodeException = new HttpStatusCodeException(HttpStatus.NOT_FOUND, null, body, null) {}

        OAuth2CredHubService.ignore404 {
            throw new CredHubException(httpStatusCodeException)
        }

        then:
        noExceptionThrown()
    }

    def "CredHubException is ignored with HttpStatus 404"() {
        when:
        OAuth2CredHubService.ignore404 {
            throw new CredHubException(HttpStatus.NOT_FOUND)
        }

        then:
        noExceptionThrown()
    }

    def "CredHubException is not ignored with HttpStatus 401"() {
        when:
        OAuth2CredHubService.ignore404 {
            throw new CredHubException(HttpStatus.UNAUTHORIZED)
        }

        then:
        thrown(CredHubException)
    }
}
