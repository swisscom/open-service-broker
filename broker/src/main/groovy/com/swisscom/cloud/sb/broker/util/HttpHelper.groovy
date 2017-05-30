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
