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
