package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.BaseModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseRepository<T extends BaseModel, ID extends Serializable> extends JpaRepository<T, ID> {
    T merge(T t)
}