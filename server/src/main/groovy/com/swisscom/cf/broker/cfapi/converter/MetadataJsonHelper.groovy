package com.swisscom.cf.broker.cfapi.converter

class MetadataJsonHelper {
    public static Object getValue(String type, Object value) {
        if (!type) {
            return value
        }
        switch (type.toLowerCase()) {
            case 'bool':
            case 'boolean':
                return Boolean.valueOf(value)
            case 'int':
            case 'integer':
                return Integer.valueOf(value)
            case 'long':
                return Long.valueOf(value)
            default:
                return value
        }
    }
}
