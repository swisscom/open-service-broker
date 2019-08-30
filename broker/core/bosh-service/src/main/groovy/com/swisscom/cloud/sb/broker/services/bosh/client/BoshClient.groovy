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

import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import com.swisscom.cloud.sb.broker.services.bosh.BoshResourceNotFoundException
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml

@CompileStatic
class BoshClient {
    private static final Logger LOG = LoggerFactory.getLogger(BoshClient.class);

    private final BoshRestClient boshRestClient

    @PackageScope
    BoshClient(BoshRestClient boshRestClient) {
        this.boshRestClient = boshRestClient
    }

    public static BoshClient of(BoshConfig boshConfig, RestTemplateBuilder restTemplateBuilder) {
        return new BoshClient(new BoshRestClient(boshConfig, restTemplateBuilder))
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
        return boshRestClient.postDeployment(yml)
    }

    /**
     * Deletes a deployment if it exists and returns the id of the task deleting the deployment
     * if the deployment does not exist, an {@link Optional#empty} is returned
     *
     * @param id deploymentId to be deleted
     * @return String indicating task id that is created.
     */
    Optional<String> deleteDeploymentIfExists(String id) {
        try {
            return Optional.of(boshRestClient.deleteDeployment(id))
        } catch (BoshResourceNotFoundException e) {
            LOG.warn("[delete]: Bosh deployment ${id} not found")
            return Optional.empty()
        }
    }

    BoshDirectorTask getTask(String id) {
        return boshRestClient.getTask(id);
    }

    BoshInfo fetchBoshInfo() {
        return boshRestClient.fetchBoshInfo();
    }

    BoshCloudConfig postConfig(BoshConfigRequest config) {
        return boshRestClient.postConfig(config.toJson())
    }

    void deleteConfig(String name, String type) {
        boshRestClient.deleteConfig(name, type)
    }
}
