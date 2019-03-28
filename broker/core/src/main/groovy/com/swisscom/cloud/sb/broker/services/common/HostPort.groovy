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

package com.swisscom.cloud.sb.broker.services.common

import groovy.transform.CompileStatic

@CompileStatic
public class HostPort {
    String host
    int port

    static HostPort from(String uri) {
        return new HostPort(host: uri.substring(0, uri.indexOf(':')), port: uri.substring(uri.indexOf(':') + 1) as int)
    }
}