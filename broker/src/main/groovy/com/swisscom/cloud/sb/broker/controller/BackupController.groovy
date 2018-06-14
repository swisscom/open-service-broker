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

package com.swisscom.cloud.sb.broker.controller

import com.swisscom.cloud.sb.broker.backup.BackupService
import com.swisscom.cloud.sb.broker.backup.converter.BackupDtoConverter
import com.swisscom.cloud.sb.broker.backup.converter.RestoreDtoConverter
import com.swisscom.cloud.sb.model.backup.BackupDto
import com.swisscom.cloud.sb.model.backup.RestoreDto
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import static org.springframework.http.HttpStatus.ACCEPTED
import static org.springframework.web.bind.annotation.RequestMethod.*

@Api(value = "backup/restore", description = "Endpoint for backups/restores")
@RestController
@CompileStatic
class BackupController extends BaseController {

    @Autowired
    private BackupService backupService

    @Autowired
    private BackupDtoConverter backupDtoConverter
    @Autowired
    private RestoreDtoConverter restoreDtoConverter

    @ApiOperation(value = "List Backups", response = BackupDto.class,
            notes = "List all the backups for the given service instance", responseContainer = "List")
    @RequestMapping(value = "/custom/service_instances/{service_instance}/backups", method = GET)
    @ResponseBody
    List<BackupDto> listBackups(
            @ApiParam(value = 'serviceInstanceGuid', required = true) @PathVariable('service_instance') String serviceInstanceGuid) {
        def backups = backupService.listBackups(getAndCheckServiceInstance(serviceInstanceGuid))
        return backupDtoConverter.convertAll(backups)
    }

    @ApiOperation(value = "Get Backup", response = BackupDto.class)
    @RequestMapping(value = "/custom/service_instances/{service_instance}/backups/{backup_id}", method = GET)
    @ResponseBody
    BackupDto getBackup(
            @PathVariable('service_instance') String serviceInstanceGuid,
            @PathVariable('backup_id') String backupId) {
        return backupDtoConverter.convert(backupService.getBackup(getAndCheckServiceInstance(serviceInstanceGuid), backupId))
    }

    @ApiOperation(value = "Create Backup", response = BackupDto.class)
    @RequestMapping(value = "/custom/service_instances/{service_instance}/backups", method = POST)
    ResponseEntity<BackupDto> createBackup(@PathVariable('service_instance') String serviceInstanceGuid) {
        def backup = backupService.requestBackupCreation(getAndCheckServiceInstance(serviceInstanceGuid))
        return new ResponseEntity<BackupDto>(backupDtoConverter.convert(backup), ACCEPTED)
    }

    @ApiOperation(value = "Delete Backup")
    @RequestMapping(value = "/custom/service_instances/{service_instance}/backups/{backup_id}", method = DELETE)
    ResponseEntity<String> deleteBackup(
            @PathVariable('service_instance') String serviceInstanceGuid,
            @PathVariable('backup_id') String backupId) {
        backupService.requestBackupDeletion(getAndCheckServiceInstance(serviceInstanceGuid), backupId)
        return new ResponseEntity<String>("{}", ACCEPTED)
    }

    @ApiOperation(value = "Restore Backup", response = RestoreDto.class)
    @RequestMapping(value = "/custom/service_instances/{service_instance}/backups/{backup_id}/restores", method = POST)
    ResponseEntity<RestoreDto> restoreBackup(
            @PathVariable('service_instance') String serviceInstanceGuid,
            @PathVariable('backup_id') String backupId) {
        def restore = backupService.requestBackupRestoration(getAndCheckServiceInstance(serviceInstanceGuid), backupId)
        return new ResponseEntity<RestoreDto>(restoreDtoConverter.convert(restore), ACCEPTED)
    }

    @ApiOperation(value = "Get restore status", response = RestoreDto.class)
    @RequestMapping(value = "/custom/service_instances/{service_instance}/backups/{backup_id}/restores/{restore_id}", method = GET)
    @ResponseBody
    RestoreDto getRestore(
            @PathVariable('service_instance') String serviceInstanceGuid,
            @PathVariable('backup_id') String backupId,
            @PathVariable('restore_id') String restoreId) {
        def restore = backupService.getRestore(getAndCheckServiceInstance(serviceInstanceGuid), backupId, restoreId)
        return restoreDtoConverter.convert(restore)
    }
}