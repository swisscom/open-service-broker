package com.swisscom.cf.broker.services.mongodb.enterprise

import com.swisscom.cf.broker.BaseSpecification
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

@Ignore
class OpsManagerFacadeTest extends BaseSpecification {

    @Autowired
    OpsManagerFacade opsManagerFacade

    def 'create group/user and then update automation config'() {
        given:
        def serviceInstanceId = 'serviceInstanceId4'
        when:
        def result = opsManagerFacade.createGroup(serviceInstanceId)
        then:
        result
    }

    def "agents ready"() {
        expect:
        opsManagerFacade.areAgentsReady('572871a9e4b0b7951b629cd5', 3)
    }

    def "get and check initial automation version"() {
        when:
        def version = opsManagerFacade.getAndCheckInitialAutomationGoalVersion('572871a9e4b0b7951b629cd5')
        then:
        version == 0
    }

    def "deployment"() {
        when:
        def result = opsManagerFacade.deployReplicaSet('5728a464e4b0b7951b676e89', 'test3')
        then:
        result
    }

    def "check automation version"() {
        expect:
        opsManagerFacade.isAutomationUpdateComplete('572871a9e4b0b7951b629cd5', 6)
    }


    def "add new db user"() {
        when:
        def result = opsManagerFacade.createDbUser('5730e094e4b0b7951c182c06', 'REPLICA_36629632-e213-4618-8827-c3fc511d25a9')
        then:
        result
    }

    def "remove db user"() {
        when:
        opsManagerFacade.deleteDbUser('5730e094e4b0b7951c182c06', 'SicbjdkXH14UKWrP')
        then:
        noExceptionThrown()
    }

    def "enable backup"() {
        when:
        opsManagerFacade.enableBackupAndSetStorageEngine('57435568e4b0017998c80bed', 'rs_76282920-2f90-4606-81d9-8db9b420d27f')

        then:
        noExceptionThrown()
    }

    def "disable backup"() {
        when:
        opsManagerFacade.disableBackup('573f1d36e4b070b97d26df01', 'rs_6d65b38b-b665-4050-b905-53678c2dab49')
        then:
        noExceptionThrown()
    }

    def "update snapshot scheudle"() {
        when:
        opsManagerFacade.updateSnapshotSchedule('57431725e4b0eb3bf68b67d1', 'rs_6d65b38b-b665-4050-b905-53678c2dab49')
        then:
        noExceptionThrown()
    }

    def "whiteListing IPs functions correctly"() {
        when:
        opsManagerFacade.whiteListIpsForUser('admin', ['10.0.0.0/8'])
        then:
        noExceptionThrown()
    }

    def "disable&terminate backup"() {
        when:
        opsManagerFacade.disableAndTerminateBackup('57435568e4b0017998c80bed', 'rs_76282920-2f90-4606-81d9-8db9b420d27f')

        then:
        noExceptionThrown()
    }

    def "undeploy"() {
        when:
        opsManagerFacade.undeploy("57467e9ae4b0406c7f41ddb9")
        then:
        noExceptionThrown()
    }
}
