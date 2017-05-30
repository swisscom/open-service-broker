package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BackupRestoreProviderLookup {
    private final ServiceProviderLookup serviceProviderLookup

    @Autowired
    BackupRestoreProviderLookup(ServiceProviderLookup serviceProviderLookup) {
        this.serviceProviderLookup = serviceProviderLookup
    }

    BackupRestoreProvider findBackupProvider(Plan plan) {
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(plan)
        if (!(serviceProvider instanceof BackupRestoreProvider)) {
            ErrorCode.BACKUP_NOT_ENABLED.throwNew()
        }
        return serviceProvider as BackupRestoreProvider
    }

    boolean isBackupProvider(Plan plan) {
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(plan)
        return (serviceProvider instanceof BackupRestoreProvider)
    }
}