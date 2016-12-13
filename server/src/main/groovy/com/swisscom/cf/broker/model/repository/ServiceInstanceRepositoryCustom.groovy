package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.model.ServiceInstance
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface ServiceInstanceRepositoryCustom {
    //TODO rename
    @Query("SELECT si FROM ServiceInstance si WHERE si.guid in (select lo.guid from LastOperation lo where lo.operation=:operation and lo.status=:status and lo.dateCreation<:olderThan)")
    public List<ServiceInstance> queryServiceInstanceForLastOperation(
            @Param("operation") LastOperation.Operation operation,
            @Param('status') LastOperation.Status status, @Param('olderThan') Date olderThan)

    @Query("SELECT si FROM ServiceInstance si WHERE si.plan.internalName=:internalName OR si.plan.service.internalName=:internalName")
    public List<ServiceInstance> listAllForInternalName(@Param("internalName") String internalName)
}