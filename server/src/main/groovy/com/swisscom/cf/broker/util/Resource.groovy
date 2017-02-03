package com.swisscom.cf.broker.util

class Resource {
    public static String readTestFileContent(String testResource) {
        return new File(this.getClass().getResource(testResource).getFile()).text
    }
}
