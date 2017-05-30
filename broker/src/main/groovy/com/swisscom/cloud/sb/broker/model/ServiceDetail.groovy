package com.swisscom.cloud.sb.broker.model

import com.swisscom.cloud.sb.broker.util.ServiceDetailKey

import javax.persistence.Column
import javax.persistence.Entity

@Entity
class ServiceDetail extends BaseModel{

    @Column(name = '_key')
    String key
    @Column(name = '_value')
    String value
    @Column(name = '_type')
    String type
    @Column(columnDefinition='tinyint(1) default 0')
    boolean uniqueKey

    static ServiceDetail from(String key, String value) { return new ServiceDetail(key: key, value: value) }

    static ServiceDetail from(ServiceDetailKey detailKey, String value) {
        return new ServiceDetail(key: detailKey.key, value: value, type: detailKey.detailType().type)
    }
}
