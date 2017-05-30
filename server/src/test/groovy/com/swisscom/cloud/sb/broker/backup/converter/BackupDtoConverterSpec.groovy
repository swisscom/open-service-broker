package com.swisscom.cloud.sb.broker.backup.converter

import com.swisscom.cloud.sb.model.backup.BackupStatus
import com.swisscom.cloud.sb.broker.backup.BackupStatusConverter
import com.swisscom.cloud.sb.broker.model.Backup
import spock.lang.Specification

class BackupDtoConverterSpec extends Specification {
    private BackupDtoConverter backupDtoConverter
    private RestoreDtoConverter restoreDtoConverter

    def setup() {
        restoreDtoConverter = Mock(RestoreDtoConverter)
        backupDtoConverter = new BackupDtoConverter(new BackupStatusConverter(), restoreDtoConverter)
    }

    def "conversion happy path"() {
        given:
        def source = new Backup()
        source.guid = "someId"
        source.serviceInstanceGuid = "serviceInstanceId"
        source.status = Backup.Status.SUCCESS
        source.operation = Backup.Operation.CREATE

        when:
        def result = backupDtoConverter.convert(source)
        then:
        source.guid == result.id
        source.serviceInstanceGuid == result.service_instance_id
        source.dateRequested == result.created_at
        source.dateUpdated == result.updated_at
        BackupStatus.CREATE_SUCCEEDED == result.status
    }
}
