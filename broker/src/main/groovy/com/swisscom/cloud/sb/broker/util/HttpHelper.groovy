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

import org.apache.commons.codec.binary.Base64
import org.springframework.http.HttpHeaders

import java.nio.charset.Charset

class HttpHelper {
    public static HttpHeaders createSimpleAuthHeaders(String username, String password ){
        return new HttpHeaders(){
            {
                set(HttpHeaders.AUTHORIZATION, computeBasicAuth(username,password) )
            }
        }
    }

    public static String computeBasicAuth(String username,String password){
        String auth = username + ":" + password
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")) )
        return "Basic " + new String( encodedAuth )
    }
}
