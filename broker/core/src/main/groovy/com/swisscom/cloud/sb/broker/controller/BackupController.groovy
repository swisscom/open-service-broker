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
import com.swisscom.cloud.sb.broker.backup.BackupStatusConverter
import com.swisscom.cloud.sb.broker.backup.RestoreStatusConverter
import com.swisscom.cloud.sb.broker.backup.converter.BackupDtoConverter
import com.swisscom.cloud.sb.broker.backup.converter.RestoreDtoConverter
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.model.backup.BackupDto
import com.swisscom.cloud.sb.model.backup.RestoreDto
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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

    private BackupService backupService
    private BackupDtoConverter backupDtoConverter
    private RestoreDtoConverter restoreDtoConverter
    private ControllerHelper controllerHelper

    BackupController(BackupService backupService, ControllerHelper controllerHelper) {
        this.controllerHelper = controllerHelper
        this.backupService = backupService
        RestoreStatusConverter restoreStatusConverter = new RestoreStatusConverter()
        backupDtoConverter = new BackupDtoConverter(new BackupStatusConverter(), new RestoreDtoConverter(restoreStatusConverter))
        restoreDtoConverter = new RestoreDtoConverter(restoreStatusConverter)
    }

    @ApiOperation(value = "List Backups", response = BackupDto.class,
            notes = "List all the backups for the given service instance", responseContainer = "List")
    @RequestMapping(value = "/custom/service_instances/{serviceInstanceGuid}/backups", method = GET)
    @ResponseBody
    List<BackupDto> listBackups(
            @ApiParam(value = 'serviceInstanceGuid', required = true) @PathVariable('serviceInstanceGuid') String serviceInstanceGuid) {
        def backups = backupService.listBackups(controllerHelper.getAndCheckServiceInstance(serviceInstanceGuid))
        return backupDtoConverter.convertAll(backups)
    }

    @ApiOperation(value = "Get Backup", response = BackupDto.class)
    @RequestMapping(value = "/custom/service_instances/{serviceInstanceGuid}/backups/{backup_id}", method = GET)
    @ResponseBody
    BackupDto getBackup(
            @PathVariable('serviceInstanceGuid') String serviceInstanceGuid,
            @PathVariable('backup_id') String backupId) {
        return backupDtoConverter.convert(backupService.getBackup(controllerHelper.getAndCheckServiceInstance(serviceInstanceGuid), backupId))
    }

    @ApiOperation(value = "Create Backup", response = BackupDto.class)
    @RequestMapping(value = "/custom/service_instances/{serviceInstanceGuid}/backups", method = POST)
    ResponseEntity<BackupDto> createBackup(@PathVariable('serviceInstanceGuid') String serviceInstanceGuid) {
        def backup = backupService.requestBackupCreation(controllerHelper.getAndCheckServiceInstance(serviceInstanceGuid))
        return new ResponseEntity<BackupDto>(backupDtoConverter.convert(backup), ACCEPTED)
    }

    @ApiOperation(value = "Delete Backup")
    @RequestMapping(value = "/custom/service_instances/{serviceInstanceGuid}/backups/{backup_id}", method = DELETE)
    ResponseEntity<String> deleteBackup(
            @PathVariable('serviceInstanceGuid') String serviceInstanceGuid,
            @PathVariable('backup_id') String backupId) {
        backupService.requestBackupDeletion(controllerHelper.getAndCheckServiceInstance(serviceInstanceGuid), backupId)
        return new ResponseEntity<String>("{}", ACCEPTED)
    }

    @ApiOperation(value = "Restore Backup", response = RestoreDto.class)
    @RequestMapping(value = "/custom/service_instances/{serviceInstanceGuid}/backups/{backup_id}/restores", method = POST)
    ResponseEntity<RestoreDto> restoreBackup(
            @PathVariable('serviceInstanceGuid') String serviceInstanceGuid,
            @PathVariable('backup_id') String backupId) {
        def restore = backupService.requestBackupRestoration(controllerHelper.getAndCheckServiceInstance(serviceInstanceGuid), backupId)
        return new ResponseEntity<RestoreDto>(restoreDtoConverter.convert(restore), ACCEPTED)
    }

    @ApiOperation(value = "Get restore status", response = RestoreDto.class)
    @RequestMapping(value = "/custom/service_instances/{serviceInstanceGuid}/backups/{backup_id}/restores/{restore_id}", method = GET)
    @ResponseBody
    RestoreDto getRestore(
            @PathVariable('serviceInstanceGuid') String serviceInstanceGuid,
            @PathVariable('backup_id') String backupId,
            @PathVariable('restore_id') String restoreId) {
        def restore = backupService.getRestore(controllerHelper.getAndCheckServiceInstance(serviceInstanceGuid), backupId, restoreId)
        return restoreDtoConverter.convert(restore)
    }
}