package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.backup.shield.ShieldClient
import com.swisscom.cloud.sb.broker.backup.shield.ShieldConfig
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.service.KubernetesRedisServiceProvider
import com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey
import com.swisscom.cloud.sb.model.backup.BackupStatus
import com.swisscom.cloud.sb.model.backup.RestoreStatus
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class KubernetesRedisFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    ShieldClient shieldClient

    @Autowired
    ShieldConfig shieldConfig

    @Autowired
    ServiceInstanceRepository serviceInstanceRepository

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('redis-kubernetes-functional-test', findInternalName(KubernetesRedisServiceProvider),
                'redis', null, null, 10)

        serviceLifeCycler.createParameter('PLAN_ID', 'redis.small', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_DISK_QUOTA', '128Mi', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('REDIS_INIT_CPU_REQUEST', '8m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_INIT_MEM_REQUEST', '82Mi', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_INIT_CPU_LIMIT', '82m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_INIT_MEM_LIMIT', '82Mi', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('REDIS_REDIS_CPU_REQUEST', '8m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_REDIS_MEM_REQUEST', '82Mi', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_REDIS_CPU_LIMIT', '82m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_REDIS_MEM_LIMIT', '82Mi', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('REDIS_SHIELD_CPU_REQUEST', '10m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_SHIELD_MEM_REQUEST', '40Mi', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_SHIELD_CPU_LIMIT', '100m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_SHIELD_MEM_LIMIT', '40Mi', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('SENTINEL_INIT_CPU_REQUEST', '10m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('SENTINEL_INIT_MEM_REQUEST', '24Mi', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('SENTINEL_INIT_CPU_LIMIT', '16m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('SENTINEL_INIT_MEM_LIMIT', '24Mi', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('SENTINEL_SENTINEL_CPU_REQUEST', '10m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('SENTINEL_SENTINEL_MEM_REQUEST', '24Mi', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('SENTINEL_SENTINEL_CPU_LIMIT', '16m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('SENTINEL_SENTINEL_MEM_LIMIT', '24Mi', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('OPERATOR_OPERATOR_CPU_REQUEST', '10m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('OPERATOR_OPERATOR_MEM_REQUEST', '24Mi', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('OPERATOR_OPERATOR_CPU_LIMIT', '50m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('OPERATOR_OPERATOR_MEM_LIMIT', '24Mi', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('OPERATOR_TELEGRAF_CPU_REQUEST', '10m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('OPERATOR_TELEGRAF_MEM_REQUEST', '64Mi', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('OPERATOR_TELEGRAF_CPU_LIMIT', '50m', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('OPERATOR_TELEGRAF_MEM_LIMIT', '64Mi', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('MAX_CONNECTIONS', '1000', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('MAX_DATABASES', '4', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_SERVER_MAX_MEMORY', '64', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('QUORUM', '2', serviceLifeCycler.plan)

        serviceLifeCycler.createParameter('BACKUP_SCHEDULE_NAME', 'daily', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('BACKUP_POLICY_NAME', 'month', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('BACKUP_STORAGE_NAME', 'default', serviceLifeCycler.plan)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Create redis instance, check backup and remove it"() {
        when:
        try {
            when:
            serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(300, true, true)
            def serviceInstance = serviceInstanceRepository.findByGuid(serviceLifeCycler.serviceInstanceId)
            def jobUuid = serviceInstance.details.find { it.key.equals(ShieldServiceDetailKey.SHIELD_JOB_UUID.key) }?.value
            def jobName = shieldClient.getJobName(jobUuid)
            then:
            jobName.equals("${shieldConfig.jobPrefix}redis-${serviceInstance.guid}")

            def createBU = serviceBrokerClient.createBackup(serviceInstance.guid).getBody()
            pollBackupStatus(0, serviceInstance.guid, createBU.id, BackupStatus.CREATE_IN_PROGRESS)

            def restoreBU = serviceBrokerClient.restoreBackup(serviceInstance.guid, createBU.id).getBody()
            def restoreStatus = serviceBrokerClient.getRestoreStatus(serviceInstance.guid, createBU.id, restoreBU.id).getBody()

            def count = 0
            while(restoreStatus.status == RestoreStatus.IN_PROGRESS){
                println("Attempt number ${count + 1}. Restore status = ${restoreStatus.status}.")
                count += 1
                serviceLifeCycler.pauseExecution(10)
                restoreStatus = serviceBrokerClient.getRestoreStatus(serviceInstance.guid, createBU.id, restoreBU.id).getBody()

                if(count == 11) {
                    ErrorCode.RESTORE_NOT_FOUND.throwNew("Restore timed out.")
                }
            }

            serviceBrokerClient.deleteBackup(serviceInstance.guid, createBU.id).getBody()
            pollBackupStatus(0, serviceInstance.guid, createBU.id, BackupStatus.DELETE_IN_PROGRESS)
        }
        finally {
            serviceLifeCycler.deleteServiceBindingAndAssert()
            serviceLifeCycler.deleteServiceInstanceAndAssert(true)
            serviceLifeCycler.pauseExecution(50)
        }
        then:
        noExceptionThrown()
    }

    def pollBackupStatus(int count, String serviceInstanceId, String backupId, BackupStatus status) {
        def getBU = serviceBrokerClient.getBackup(serviceInstanceId, backupId).getBody()

        while(getBU.status == status){
            println("Attempt number ${count + 1}. Backup status = ${getBU.status}.")
            count += 1
            serviceLifeCycler.pauseExecution(10)
            getBU = serviceBrokerClient.getBackup(serviceInstanceId, backupId).getBody()

            if(count == 11) {
                ErrorCode.BACKUP_NOT_FOUND.throwNew("Backup creation timed out.")
            }
        }
    }

}