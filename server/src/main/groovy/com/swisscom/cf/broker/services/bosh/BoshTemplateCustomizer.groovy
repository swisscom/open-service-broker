package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceDetail


interface BoshTemplateCustomizer {
    Collection<ServiceDetail> customizeBoshTemplate(BoshTemplate template, ProvisionRequest provisionRequest)
}