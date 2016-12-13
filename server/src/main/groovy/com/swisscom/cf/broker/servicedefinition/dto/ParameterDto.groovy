package com.swisscom.cf.broker.servicedefinition.dto

import groovy.transform.CompileStatic

@CompileStatic
class ParameterDto implements Serializable {
    String template
    String name
    String value
}
