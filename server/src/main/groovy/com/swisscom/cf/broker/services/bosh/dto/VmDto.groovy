package com.swisscom.cf.broker.services.bosh.dto

import groovy.transform.CompileStatic

@CompileStatic
class VmDto implements Serializable {
    String name
    CloudPropertiesDto cloud_properties
}