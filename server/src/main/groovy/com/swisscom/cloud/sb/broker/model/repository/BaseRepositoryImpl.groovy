package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.BaseModel
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository

import javax.persistence.EntityManager

class BaseRepositoryImpl<T extends BaseModel, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {
    private final EntityManager entityManager

    BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager)
        this.entityManager = entityManager;
    }

    @Override
    T merge(T t) {
        return entityManager.merge(t)
    }
}
