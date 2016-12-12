package com.swisscom.cf.broker.util

import org.apache.commons.codec.binary.Base64
import org.springframework.http.HttpHeaders

import java.nio.charset.Charset

class HttpHelper {
    public static HttpHeaders createSimpleAuthHeaders(String username, String password ){
        return new HttpHeaders(){
            {
                String auth = username + ":" + password
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")) )
                String authHeader = "Basic " + new String( encodedAuth )
                set( "Authorization", authHeader )
            }
        }
    }
}
