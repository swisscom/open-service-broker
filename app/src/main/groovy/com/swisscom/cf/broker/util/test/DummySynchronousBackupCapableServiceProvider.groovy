package com.swisscom.cf.broker.util.test

import com.google.common.base.Optional
import com.swisscom.cf.broker.filterextensions.serviceusage.ServiceUsage
import com.swisscom.cf.broker.model.*
import com.swisscom.cf.broker.services.common.*
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import sun.reflect.generics.reflectiveObjects.NotImplementedException

@Component
@Log4j
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
        return com.swisscom.cf.broker.util.StringGenerator.randomUuid()
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
        return com.swisscom.cf.broker.util.StringGenerator.randomUuid()
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

    private boolean isReady(Date dateCreation) {
        new DateTime(dateCreation).plusSeconds(10).isBeforeNow()
    }

    @Override
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
        return new ServiceUsage(value: "0", type: ServiceUsage.Type.TRANSACTIONS)
    }
}
