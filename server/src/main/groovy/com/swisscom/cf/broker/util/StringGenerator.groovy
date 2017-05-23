package com.swisscom.cf.broker.util

import java.security.SecureRandom

class StringGenerator {
    public static final char[] alphaNumericCharset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join().toCharArray()
    public static final char[] hexadecimalCharset = (('a'..'f') + ('0'..'9')).join().toCharArray()
    public static final char[] lowerAlphaCharset = ('a'..'z').join().toCharArray()

    public static String randomAlphaNumericOfLength16() {
        random(16, alphaNumericCharset)
    }

    public static String randomAlphaNumeric(int length) {
        random(length, alphaNumericCharset)
    }

    public static String randomLowerAlphaOfLength16() {
        random(16, lowerAlphaCharset)
    }

    private static String random(int length, char[] chars) {
        org.apache.commons.lang.RandomStringUtils.random(length, 0, chars.length, false, false, chars, new SecureRandom())
    }

    public static String randomUuid() {
        return UUID.randomUUID().toString()
    }

    static String randomHexadecimal(int length) {
        random(length, hexadecimalCharset)
    }
}
