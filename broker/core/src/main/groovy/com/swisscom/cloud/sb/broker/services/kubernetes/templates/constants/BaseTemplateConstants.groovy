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

package com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants

import groovy.transform.CompileStatic

@CompileStatic
enum BaseTemplateConstants implements AbstractTemplateConstants{
    SERVICE_ID("SERVICE_ID"),
    SPACE_ID("SPACE_ID"),
    ORG_ID("ORG_ID"),
    PLAN_ID("PLAN_ID")

    private BaseTemplateConstants(String value) {
        com_swisscom_cloud_sb_broker_services_kubernetes_templates_constants_AbstractTemplateConstants__value = value
    }

}