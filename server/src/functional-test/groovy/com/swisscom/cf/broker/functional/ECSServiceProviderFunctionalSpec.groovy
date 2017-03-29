package com.swisscom.cf.broker.functional

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.swisscom.cf.broker.services.common.ServiceProviderLookup
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.BillingManager
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtBillingInformationResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.service.ECSServiceProvider
import org.springframework.beans.factory.annotation.Autowired

class ECSServiceProviderFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    ECSConfig ecsConfig

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('ECS', ServiceProviderLookup.findInternalName(ECSServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Add a file to a bucket with ECS"() {
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(10)
        def credentials = serviceLifeCycler.getCredentials()
        println("Credentials: ${credentials}")
        and:
        AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials(credentials.accessKey, credentials.sharedSecret))
        s3client.setEndpoint("https://ds11s3.swisscom.com")
        s3client.createBucket("bucket")
        s3client.putObject(new PutObjectRequest("bucket", "key", new File("/Users/taalyko2/Downloads/gogland-163.12024.32.dmg")))
        Thread.sleep(1000)
        cleanup:
        s3client.deleteObject("bucket", "key")
        s3client.deleteBucket("bucket")
        serviceLifeCycler.deleteServiceBindingAndServiceInstaceAndAssert()
    }

    def "Adding file updates billing information"() {
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(10)
        def credentials = serviceLifeCycler.getCredentials()
        println("Credentials: ${credentials}")
        and:
        AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials(credentials.accessKey, credentials.sharedSecret))
        s3client.setEndpoint("https://ds11s3.swisscom.com")
        s3client.createBucket("bucket")
        s3client.putObject(new PutObjectRequest("bucket", "key", new File("/tmp/kokos.txt")))
        Thread.sleep(60000)
        and:
        BillingManager billingManager = new BillingManager(ecsConfig)
        ECSMgmtBillingInformationResponse billingInfo = billingManager.getInformation(new ECSMgmtNamespacePayload(namespace: credentials.accessKey.toString().split("-")[0]))
        expect:
        (new BigDecimal(billingInfo.total_size)).toInteger() > 0
        cleanup:
        s3client.deleteObject("bucket", "key")
        s3client.deleteBucket("bucket")
        serviceLifeCycler.deleteServiceBindingAndServiceInstaceAndAssert()

    }

}