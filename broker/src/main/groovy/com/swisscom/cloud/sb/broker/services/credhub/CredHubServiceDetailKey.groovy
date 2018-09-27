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

package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import groovy.transform.CompileStatic

@CompileStatic
enum CredHubServiceDetailKey implements AbstractServiceDetailKey{

    CREDHUB_CREDENTIAL_ID("credhub_credential_id", ServiceDetailType.OTHER),
    CREDHUB_CREDENTIAL_NAME("credhub_credential_name", ServiceDetailType.OTHER),
    CREDHUB_CREDENTIAL_TYPE("credhub_credential_type", ServiceDetailType.OTHER),
    CREDHUB_CREDENTIAL_TIMESTAMP("credhub_credential_timestamp", ServiceDetailType.OTHER)

    CredHubServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__key = key
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__serviceDetailType = serviceDetailType
    }
}