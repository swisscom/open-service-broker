package com.swisscom.cloud.sb.broker.util

interface MutexFactory {
    Object getNamedMutex(String name)
}
