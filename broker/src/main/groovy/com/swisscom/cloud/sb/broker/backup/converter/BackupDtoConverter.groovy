package com.swisscom.cloud.sb.broker.backup.converter

import com.swisscom.cloud.sb.broker.backup.BackupStatusConverter
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.model.backup.BackupDto
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
class BackupDtoConverter extends AbstractGenericConverter<Backup, BackupDto> {

    private final BackupStatusConverter backupStatusConverter
    private final RestoreDtoConverter restoreDtoConverter

    @Autowired
    BackupDtoConverter(BackupStatusConverter backupStatusConverter, RestoreDtoConverter restoreDtoConverter) {
        this.backupStatusConverter = backupStatusConverter
        this.restoreDtoConverter = restoreDtoConverter
    }

    @Override
    public void convert(Backup source, BackupDto prototype) {
        prototype.id = source.guid
        prototype.service_instance_id = source.serviceInstanceGuid
        prototype.created_at = source.dateRequested
        prototype.updated_at = source.dateUpdated
        prototype.status = backupStatusConverter.convert(source.status, source.operation)
        prototype.restores = restoreDtoConverter.convertAll(source.restores)
    }
}
