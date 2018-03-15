package com.swisscom.cloud.sb.broker.cfextensions.extensions

import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.backup.shield.dto.JobStatus
import com.swisscom.cloud.sb.broker.backup.shield.dto.TaskDto
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.Catalog
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import sun.awt.AppContext

@Slf4j
trait ExtensionProvider{

    @Autowired
    protected JobManager jobManager

    abstract Collection<Extension> buildExtensions()

    abstract TaskDto getTask(String taskUuid)

    JobStatus getJobStatus(TaskDto task) {
        if (task.statusParsed.isRunning()) {
            return JobStatus.RUNNING
        }
        if (task.statusParsed.isFailed()) {
            log.warn("Task failed: ${task}")
            return JobStatus.FAILED
        }
        if (task.statusParsed.isDone()) {
            return JobStatus.SUCCESSFUL
        }
        throw new RuntimeException("Invalid task status ${task.status} for task ${task.job_uuid}")
    }

    def queueExtension(JobConfig jobConfig){
        jobManager.queue(jobConfig)
    }

    String getApi(){
        RestTemplate restTemplate = new RestTemplate()

        String swaggerJson = restTemplate.getForEntity("http://localhost:8080/v2/api-docs", String.class).body

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)

        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>()
        map.add("source", swaggerJson)

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers)
        ResponseEntity<String> response = restTemplate.postForEntity( "https://mermade.org.uk/openapi-converter/api/v1/convert", request , String.class )

        return response.body
    }
}