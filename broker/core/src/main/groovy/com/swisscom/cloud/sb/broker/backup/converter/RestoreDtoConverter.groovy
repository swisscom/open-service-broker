/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.backup.converter

import com.swisscom.cloud.sb.broker.backup.RestoreStatusConverter
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.model.backup.RestoreDto
import groovy.transform.CompileStatic

@CompileStatic
class RestoreDtoConverter extends AbstractGenericConverter<Restore, RestoreDto> {

    private RestoreStatusConverter restoreStatusConverter

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
