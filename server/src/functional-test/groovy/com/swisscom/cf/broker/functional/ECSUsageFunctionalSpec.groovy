package com.swisscom.cf.broker.functional

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.swisscom.cf.broker.services.common.ServiceProviderLookup
import com.swisscom.cf.broker.util.StringGenerator
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

import static com.swisscom.cf.broker.util.HttpHelper.createSimpleAuthHeaders

class ECSUsageFunctionalSpec extends BaseFunctionalSpec {

    def setup(){
        serviceLifeCycler.createServiceIfDoesNotExist('ECS', ServiceProviderLookup.findInternalName(ECSServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Get usage for a uid account"(){
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(10)
        def credentials = serviceLifeCycler.getCredentials()
        println("Credentials: ${credentials}")
        //Create a bucket and object to create usage
        AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials(credentials.accessKey, credentials.sharedSecret))
        s3client.setEndpoint(credentials.accessHost)
        def bucket = ("servicebrokerfunctionaltest" + StringGenerator.randomUuid()).toLowerCase()
        def key = 'key'
        s3client.createBucket(bucket)
        s3client.putObject(new PutObjectRequest(bucket, key, new File('test/resources/testFile.txt')));
        Thread.sleep(1000)
        when:
        def response = new RestTemplate().exchange(cfExtUsageUrl.replace('@@service_instance_id@@', serviceLifeCycler.getServiceInstanceId()), new HttpEntity(createSimpleAuthHeaders("CF_ADMIN", "SOME_PASS")), String.class)
        then:
        response.statusCode == HttpStatus.OK
        // (response.body.size as int) > 0
        // response.json.value == response.json.size
        //response.json.type == 'watermark'

        cleanup:
        s3client.deleteObject(bucket, key)
        s3client.deleteBucket(bucket)
        serviceLifeCycler.deleteServiceBindingAndServiceInstaceAndAssert()
    }

}