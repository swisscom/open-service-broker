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

package com.swisscom.cloud.sb.broker.services.bosh.client

import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshInfoDto
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshVMDto
import com.swisscom.cloud.sb.broker.services.bosh.dto.ConfigRequestDto
import com.swisscom.cloud.sb.broker.services.bosh.dto.TaskDto
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml

@Slf4j
@CompileStatic
class BoshClient {
    private final BoshConfig boshConfig
    private final BoshRestClient boshRestClient

    BoshClient(BoshRestClient boshRestClient) {
        this.boshConfig = boshRestClient.getBoshConfig()
        this.boshRestClient = boshRestClient
    }

    /**
     * Creates a new deployment
     *
     * @param yml Deployment manifest
     * @return String indicating task id that is created a result of deployment.
     */
    String postDeployment(String yml) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(yml))
        try {
            new Yaml().load(yml)
        } catch (Exception e) {
            throw new RuntimeException("Deployment needs a valid yml file: ${yml}")
        }
        log.trace("Bosh deployment on ${boshConfig.boshDirectorBaseUrl} with yml:${yml}")
        return boshRestClient.postDeployment(yml)
    }

    /**
     * Deletes a deployment
     *
     * @param id deploymentId
     * @return String indicating task id that is created.
     */
    Optional<String> deleteDeploymentIfExists(String id) {
        try {
            return Optional.of(boshRestClient.deleteDeployment(id))
        } catch (BoshResourceNotFoundException e) {
            log.warn("Bosh deployment ${id} not found")
            return Optional.absent()
        }
    }

    TaskDto getTask(String id) {
        String task = boshRestClient.getTask(id)
        log.info("Bosh task:${task}")
        return new Gson().fromJson(task, TaskDto)
    }

    List<BoshVMDto> getAllVMsInDeployment(String id) {
        String vms = boshRestClient.getAllVMsInDeployment(id)
        log.debug("Bosh VMs:${vms}")
        return new Gson().fromJson(vms, new TypeToken<List<BoshVMDto>>(){}.getType())
    }

    BoshConfig getBoshConfig() {
        return boshConfig
    }

    BoshRestClient getBoshRestClient() {
        return boshRestClient
    }

    BoshInfoDto fetchBoshInfo() {
        def result = boshRestClient.fetchBoshInfo()
        return new Gson().fromJson(result, BoshInfoDto)
    }

    void setConfig(ConfigRequestDto config) {
        boshRestClient.postConfig(config.toJson())
    }

    void deleteConfig(String name, String type) {
        boshRestClient.deleteConfig(name, type)
    }
}
