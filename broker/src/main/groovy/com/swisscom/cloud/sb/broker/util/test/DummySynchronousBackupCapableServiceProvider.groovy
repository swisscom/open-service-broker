package com.swisscom.cloud.sb.broker.util.test

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.backup.BackupRestoreProvider
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageProvider
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import sun.reflect.generics.reflectiveObjects.NotImplementedException

@Component
@Slf4j
@CompileStatic
class DummySynchronousBackupCapableServiceProvider implements ServiceProvider, BackupRestoreProvider, ServiceUsageProvider {
    @Override
    BindResponse bind(BindRequest request) {
        throw new NotImplementedException()
    }

    @Override
    void unbind(UnbindRequest request) {
        throw new NotImplementedException()
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        return new ProvisionResponse(details: [], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    String createBackup(Backup backup) {
        log.info("createBackup for ${backup}")
        return StringGenerator.randomUuid()
    }

    @Override
    void deleteBackup(Backup backup) {
        log.info("deleteBackup for ${backup}")
    }

    @Override
    Backup.Status getBackupStatus(Backup backup) {
        log.info("getBackupStatus for ${backup}")
        return isReady(backup.dateRequested) ? Backup.Status.SUCCESS : Backup.Status.IN_PROGRESS
    }

    @Override
    String restoreBackup(Restore restore) {
        log.info("restoreBackup for ${restore}")
        return StringGenerator.randomUuid()
    }

    @Override
    Backup.Status getRestoreStatus(Restore restore) {
        log.info("getRestoreStatus for ${restore}")
        return isReady(restore.dateRequested) ? Backup.Status.SUCCESS : Backup.Status.IN_PROGRESS
    }

    @Override
    void notifyServiceInstanceDeletion(ServiceInstance serviceInstance) {
        log.info("notifyServiceInstanceDeletion for ${serviceInstance}")

    }

    @Override
    UpdateResponse update(UpdateRequest request) {
        ErrorCode.SERVICE_UPDATE_NOT_ALLOWED.throwNew()
        return null
    }

    private boolean isReady(Date dateCreation) {
        new DateTime(dateCreation).plusSeconds(10).isBeforeNow()
    }

    @Override
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
        return new ServiceUsage(value: "0", type: ServiceUsageType.TRANSACTIONS)
    }

    @Override
    ShieldTarget buildShieldTarget(ServiceInstance serviceInstance) {
        throw new NotImplementedException()
    }

    @Override
    String shieldAgentUrl(ServiceInstance serviceInstance) {
        log.info("shieldAgentUrl for ${serviceInstance.guid}")
        return StringGenerator.randomUuid()
    }
}
