package com.swisscom.cf.broker.util

interface MutexFactory {
    Object getNamedMutex(String name)
}
