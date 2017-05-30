package com.swisscom.cloud.sb.broker.backup.converter

import com.swisscom.cloud.sb.broker.backup.RestoreStatusConverter
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.model.backup.RestoreDto
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
class RestoreDtoConverter extends AbstractGenericConverter<Restore, RestoreDto> {

    private RestoreStatusConverter restoreStatusConverter

    @Autowired
    RestoreDtoConverter(RestoreStatusConverter restoreStatusConverter) {
        this.restoreStatusConverter = restoreStatusConverter
    }

    @Override
    public void convert(Restore source, RestoreDto prototype) {
        prototype.id = source.guid
        prototype.backup_id = source.backup.guid
        prototype.created_at = source.dateRequested
        prototype.updated_at = source.dateUpdated
        prototype.status = restoreStatusConverter.convert(source.status)
    }
}
