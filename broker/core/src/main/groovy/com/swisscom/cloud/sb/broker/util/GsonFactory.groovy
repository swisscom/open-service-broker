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

package com.swisscom.cloud.sb.broker.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic

@CompileStatic
class GsonFactory {
    public static final String DATE_FORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ssXXX"

    static Gson withISO8601Datetime() {
        new GsonBuilder().setDateFormat(DATE_FORMAT_ISO_8601).create()
    }
}
