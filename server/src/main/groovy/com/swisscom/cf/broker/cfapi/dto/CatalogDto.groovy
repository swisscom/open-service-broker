package com.swisscom.cf.broker.cfapi.dto

import groovy.transform.CompileStatic

@CompileStatic
class CatalogDto implements Serializable {
    List<CFServiceDto> services

}
