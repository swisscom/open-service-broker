package com.swisscom.cloud.sb.broker.cfextensions

import groovy.util.logging.Slf4j

@Slf4j
trait ExtensionProvider{

    abstract Extension buildExtension()

}