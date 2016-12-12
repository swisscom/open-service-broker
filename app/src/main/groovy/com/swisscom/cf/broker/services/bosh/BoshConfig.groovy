package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.config.Config
import groovy.transform.CompileStatic

@CompileStatic
trait BoshConfig implements Config {
    String boshDirectorBaseUrl
    String boshDirectorUsername
    String boshDirectorPassword
}
