package com.swisscom.cloud.sb.broker.cfextensions.extensions

import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.async.job.JobStatus
import com.swisscom.cloud.sb.broker.backup.shield.dto.TaskDto
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
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

    abstract Collection<Extension> buildExtensions()

    String getApi(){
        RestTemplate restTemplate = new RestTemplate()

        String swaggerJson = restTemplate.getForEntity("http://localhost:8080/v2/api-docs", String.class).body

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)

        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>()
        map.add("source", swaggerJson)

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers)
        ResponseEntity<String> response = restTemplate.postForEntity( "https://mermade.org.uk/openapi-converter/api/v1/convert", request , String.class )

        return response.body - "<html><body><pre>"
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