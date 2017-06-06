package com.swisscom.cloud.sb.broker.util


class HttpHeader {

    public static String basicAuthorization(String username, String password) {
        return 'Basic ' + "${username}:${password}".bytes.encodeBase64().toString()
    }
}
