package com.swisscom.cloud.sb.broker.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MockFlag {

    static Map<String, Boolean> dyndb

    @Value('${mock.dyndb}')
    void setFlags(String flag){
        dyndb = Eval.me(flag) as Map<String, Boolean>
    }
}
