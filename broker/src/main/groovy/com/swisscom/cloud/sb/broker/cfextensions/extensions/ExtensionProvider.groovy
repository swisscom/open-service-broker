package com.swisscom.cloud.sb.broker.cfextensions.extensions

import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.async.job.JobStatus
import com.swisscom.cloud.sb.broker.backup.shield.dto.TaskDto
import com.swisscom.cloud.sb.broker.config.ApplicationConfiguration
import groovy.util.logging.Slf4j
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.parser.OpenAPIV3Parser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Slf4j
trait ExtensionProvider{
    //Trying to manage both sync and async extensions in this trait. Maybe I should create an AsyncExtensionProvider
    @Autowired
    protected JobManager jobManager

    @Autowired
    Environment env

    abstract Collection<Extension> buildExtensions()

    String getApi(){
        getApi(["service-definition-controller"])
    }

    String getApi(List<String> tags, String url = "http://localhost:${env.getProperty("server.port")}${env.getProperty("server.contextPath")}/v2/api-docs") {
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
                }
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