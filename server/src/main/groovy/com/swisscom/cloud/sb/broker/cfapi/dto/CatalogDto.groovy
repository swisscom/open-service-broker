package com.swisscom.cloud.sb.broker.cfapi.dto

import groovy.transform.CompileStatic

@CompileStatic
class CatalogDto implements Serializable {
    List<CFServiceDto> services

}
