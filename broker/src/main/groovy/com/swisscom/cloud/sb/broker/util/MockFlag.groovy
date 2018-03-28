package com.swisscom.cloud.sb.broker.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MockFlag {

    static Map<String, Boolean> dyndb

    @Value('${mock.dyndb}')
    void setDyndb(String flags){
        dyndb = Eval.me(flags) as Map<String, Boolean>
    }
}
