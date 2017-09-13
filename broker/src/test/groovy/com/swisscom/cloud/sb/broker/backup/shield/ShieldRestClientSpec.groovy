package com.swisscom.cloud.sb.broker.backup.shield

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.backup.shield.dto.ArchiveDto
import com.swisscom.cloud.sb.broker.backup.shield.dto.JobDto
import com.swisscom.cloud.sb.broker.backup.shield.dto.TargetDto
import com.swisscom.cloud.sb.broker.backup.shield.dto.TaskDto
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

class ShieldRestClientSpec extends Specification {
    ShieldRestClient shieldRestClient
    MockRestServiceServer mockServer

    String baseUrl = "http://baseurl"
    String apiKey = "apiKey"

    class DummyTarget implements ShieldTarget {

        @Override
        String pluginName() {
            "doesntmatter"
        }

        @Override
        String endpointJson() {
            "{}"
        }
    }

    def setup() {
        RestTemplate restTemplate = new RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        and:
        shieldRestClient = new ShieldRestClientFactory().build(restTemplate, baseUrl, apiKey)
    }

    def "get status"() {
        given:
        mockServer.expect(requestTo(shieldRestClient.statusUrl()))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('{"status":"status"}', MediaType.APPLICATION_JSON))

        when:
        def status = shieldRestClient.getStatus()
        then:
        mockServer.verify()
        status.status == "status"
    }

    def "get store by name"() {
        given:
        mockServer.expect(requestTo(shieldRestClient.storesUrl() + "?name=storeName"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('[]', MediaType.APPLICATION_JSON))

        when:
        shieldRestClient.getStoreByName("storeName")
        then:
        mockServer.verify()
    }

    def "get retention by name"() {
        given:
        mockServer.expect(requestTo(shieldRestClient.retentionsUrl() + "?name=retentionName"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('[]', MediaType.APPLICATION_JSON))

        when:
        shieldRestClient.getRetentionByName("retentionName")
        then:
        mockServer.verify()
    }

    def "get schedule by name"() {
        given:
        mockServer.expect(requestTo(shieldRestClient.schedulesUrl() + "?name=scheduleName"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('[]', MediaType.APPLICATION_JSON))

        when:
        shieldRestClient.getScheduleByName("scheduleName")
        then:
        mockServer.verify()
    }

    def "get target by name"() {
        given:
        mockServer.expect(requestTo(shieldRestClient.targetsUrl() + "?name=targetName"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('[]', MediaType.APPLICATION_JSON))

        when:
        shieldRestClient.getTargetByName("targetName")
        then:
        mockServer.verify()
    }

    def "create target"() {
        given:
        String targetName = "targetName"
        String agent = "agent-example"
        def target = new DummyTarget()
        def body = new ObjectMapper().writeValueAsString([name    : targetName,
                                                          plugin  : target.pluginName(),
                                                          endpoint: target.endpointJson(),
                                                          agent   : agent])
        mockServer.expect(requestTo(shieldRestClient.targetsUrl()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().string(body))
                .andRespond(withSuccess('{"uuid":"targetId"}', MediaType.APPLICATION_JSON))

        when:
        def targetId = shieldRestClient.createTarget(targetName, target, agent)
        then:
        mockServer.verify()
        targetId == "targetId"
    }

    def "update target"() {
        given:
        String agent = "agent-example"
        TargetDto targetDto = new TargetDto(uuid: "targetUuid", name: "targetName")
        def target = new DummyTarget()
        def body = new ObjectMapper().writeValueAsString([name    : targetDto.name,
                                                          summary : targetDto.summary,
                                                          plugin  : target.pluginName(),
                                                          endpoint: target.endpointJson(),
                                                          agent   : agent])
        mockServer.expect(requestTo(shieldRestClient.targetUrl(targetDto.uuid)))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().string(body))
                .andRespond(withSuccess('', MediaType.APPLICATION_JSON))

        when:
        def targetId = shieldRestClient.updateTarget(targetDto, target, agent)
        then:
        mockServer.verify()
        targetId == targetDto.uuid
    }

    def "delete target"() {
        given:
        String targetId = 'target-id'
        mockServer.expect(requestTo(shieldRestClient.targetUrl(targetId)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('', MediaType.APPLICATION_JSON))

        when:
        shieldRestClient.deleteTarget(targetId)
        then:
        mockServer.verify()
    }

    def "get job by name"() {
        given:
        mockServer.expect(requestTo(shieldRestClient.jobsUrl() + "?name=jobName"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('[]', MediaType.APPLICATION_JSON))

        when:
        shieldRestClient.getJobByName("jobName")
        then:
        mockServer.verify()
    }

    def "create job"() {
        given:
        def job = [ name: "jobName",
                    target: "target-id",
                    store: "store-id",
                    retention: "retention-id",
                    schedule: "schedule-id",
                    paused: false]
        mockServer.expect(requestTo(shieldRestClient.jobsUrl()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().string(new ObjectMapper().writeValueAsString(job)))
                .andRespond(withSuccess('{"uuid": "job-uuid"}', MediaType.APPLICATION_JSON))

        when:
        def jobId = shieldRestClient.createJob(job.name,
                job.target,
                job.store,
                job.retention,
                job.schedule,
                job.paused)
        then:
        mockServer.verify()
        jobId == "job-uuid"
    }

    def "update job"() {
        given:
        JobDto jobDto = new JobDto(uuid: "jobUuid", name: "jobName")
        def job = [ name: jobDto.name,
                    summary: jobDto.summary,
                    target: "target-id",
                    store: "store-id",
                    retention: "retention-id",
                    schedule: "schedule-id",
                    paused: false]
        mockServer.expect(requestTo(shieldRestClient.jobUrl(jobDto.uuid)))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().string(new ObjectMapper().writeValueAsString(job)))
                .andRespond(withSuccess('', MediaType.APPLICATION_JSON))

        when:
        def jobId = shieldRestClient.updateJob(jobDto,
                job.target,
                job.store,
                job.retention,
                job.schedule,
                job.paused)
        then:
        mockServer.verify()
        jobId == "jobUuid"
    }

    def "run job"() {
        given:
        String jobId = "job-id"
        mockServer.expect(requestTo(shieldRestClient.jobUrl(jobId) + "/run"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('{"task_uuid":"1234"}', MediaType.APPLICATION_JSON))

        when:
        def taskId = shieldRestClient.runJob(jobId)
        then:
        mockServer.verify()
        taskId == "1234"
    }

    def "delete job"() {
        given:
        String jobId = 'job-id'
        mockServer.expect(requestTo(shieldRestClient.jobUrl(jobId)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('', MediaType.APPLICATION_JSON))

        when:
        shieldRestClient.deleteJob(jobId)
        then:
        mockServer.verify()
    }


    def "get task by uuid"() {
        given:
        String taskId = "task-id"
        mockServer.expect(requestTo(shieldRestClient.taskUrl(taskId)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('{"type": "backup", "status":"pending"}', MediaType.APPLICATION_JSON))

        when:
        def task = shieldRestClient.getTaskByUuid(taskId)
        then:
        mockServer.verify()
        task.typeParsed == TaskDto.Type.BACKUP
        task.statusParsed == TaskDto.Status.PENDING
    }

    def "get archive by uuid"() {
        given:
        String archiveId = "archive-id"
        mockServer.expect(requestTo(shieldRestClient.archiveUrl(archiveId)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('{"status":"valid"}', MediaType.APPLICATION_JSON))

        when:
        def archive = shieldRestClient.getArchiveByUuid(archiveId)
        then:
        mockServer.verify()
        archive.statusParsed == ArchiveDto.Status.VALID
    }

    def "restore archive"() {
        given:
        String archiveId = "archive-id"
        mockServer.expect(requestTo(shieldRestClient.archiveUrl(archiveId) + "/restore"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('{"task_uuid":"taskId"}', MediaType.APPLICATION_JSON))

        when:
        def taskId = shieldRestClient.restoreArchive(archiveId)
        then:
        mockServer.verify()
        taskId == "taskId"
    }

    def "delete archive"() {
        given:
        String archiveId = 'archive-id'
        mockServer.expect(requestTo(shieldRestClient.archiveUrl(archiveId)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('', MediaType.APPLICATION_JSON))

        when:
        shieldRestClient.deleteArchive(archiveId)
        then:
        mockServer.verify()
    }

    def "handle 501 response when getting null task"() {
        given:
        String taskId = null
        mockServer.expect(requestTo(shieldRestClient.taskUrl(taskId)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_IMPLEMENTED))

        when:
        shieldRestClient.getTaskByUuid(taskId)
        then:
        mockServer.verify()
        thrown(ShieldResourceNotFoundException)
    }

    def "handle 501 response when getting empty task"() {
        given:
        String taskId = ""
        mockServer.expect(requestTo(shieldRestClient.taskUrl(taskId)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_IMPLEMENTED))

        when:
        shieldRestClient.getTaskByUuid(taskId)
        then:
        mockServer.verify()
        thrown(ShieldResourceNotFoundException)
    }

    def "handle 501 response when getting null archive"() {
        given:
        String archiveId = null
        mockServer.expect(requestTo(shieldRestClient.archiveUrl(archiveId)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_IMPLEMENTED))

        when:
        shieldRestClient.getArchiveByUuid(archiveId)
        then:
        mockServer.verify()
        thrown(ShieldResourceNotFoundException)
    }

    def "handle 501 response when getting empty archive"() {
        given:
        String archiveId = ""
        mockServer.expect(requestTo(shieldRestClient.archiveUrl(archiveId)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_IMPLEMENTED))

        when:
        shieldRestClient.getArchiveByUuid(archiveId)
        then:
        mockServer.verify()
        thrown(ShieldResourceNotFoundException)
    }

    def "handle 501 response when deleting null archive"() {
        given:
        String archiveId = null
        mockServer.expect(requestTo(shieldRestClient.archiveUrl(archiveId)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_IMPLEMENTED))

        when:
        shieldRestClient.deleteArchive(archiveId)
        then:
        mockServer.verify()
        thrown(ShieldResourceNotFoundException)
    }

    def "handle 501 response when deleting empty archive"() {
        given:
        String archiveId = ""
        mockServer.expect(requestTo(shieldRestClient.archiveUrl(archiveId)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(ShieldRestClient.HEADER_API_KEY, apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_IMPLEMENTED))

        when:
        shieldRestClient.deleteArchive(archiveId)
        then:
        mockServer.verify()
        thrown(ShieldResourceNotFoundException)
    }
}
