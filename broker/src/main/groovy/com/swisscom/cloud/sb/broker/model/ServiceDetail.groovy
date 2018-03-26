package com.swisscom.cloud.sb.broker.model

import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey

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

    static ServiceDetail from(AbstractServiceDetailKey detailKey, String value) {
        return new ServiceDetail(key: detailKey.key, value: value, type: detailKey.detailType().type)
    }

    @Override
    boolean equals(Object obj) {
        ServiceDetail otherServiceDetail = obj as ServiceDetail
        if (otherServiceDetail == null)
            return false

        return isSameServiceDetail(otherServiceDetail)
    }

    private boolean isSameServiceDetail(ServiceDetail serviceDetail) {
        return serviceDetail.id == this.id ||
                (this.uniqueKey && serviceDetail.key ==  this.key)
    }
}
