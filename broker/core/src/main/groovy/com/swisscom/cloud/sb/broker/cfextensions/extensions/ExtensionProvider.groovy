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

package com.swisscom.cloud.sb.broker.cfextensions.extensions

import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.async.job.JobStatus
import groovy.util.logging.Slf4j
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.parser.OpenAPIV3Parser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment

@Slf4j
trait ExtensionProvider implements CFExtensionProvider{
    //Trying to manage both sync and async extensions in this trait. Maybe I should create an AsyncExtensionProvider
    @Autowired
    JobManager jobManager

    @Autowired
    Environment env


    String getApi(){
        getApi(null)
    }

    String getApi(List<String> tags, String url = "http://localhost:${env.getProperty("local.server.port")}/v2/api-docs") {

        OpenAPI openAPI = new OpenAPIV3Parser().read(url)
        if (tags) {
            openAPI.setPaths(filterPathsByTags(openAPI, tags))
        }
        Json.pretty(openAPI)
    }

    private Paths filterPathsByTags(OpenAPI openAPI, List<String> tags) {
        Paths pathsToReturn = new Paths()
        for (path in openAPI.getPaths()) {
            for (def operation : path.getValue().readOperations()) {
                for (String tag : tags) {
                    if (operation.getTags().contains(tag)) {
                        pathsToReturn.addPathItem(path.getKey(), path.getValue())
                    }
Ø                }
            }
        }
        pathsToReturn
    }

    //The following methods are required for asynchronous extensions.

    //This should be overriden.
    JobStatus getJobStatus(Status status){
        return JobStatus.SUCCESSFUL
    }

    def queueExtension(JobConfig jobConfig){
        jobManager.queue(jobConfig)
    }
}
