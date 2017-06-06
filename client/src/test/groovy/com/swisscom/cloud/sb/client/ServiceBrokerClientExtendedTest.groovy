package com.swisscom.cloud.sb.client

import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate

@RunWith(SpringJUnit4ClassRunner.class)
class ServiceBrokerClientExtendedTest {
    private static ServiceBrokerClientExtended serviceBrokerClientExtended
    private static String serviceInstanceId = '4ca9439a-b002-11e6-80f5-76304dec7eb7'
    private RestTemplate restTemplate
    private MockRestServiceServer mockRestServiceServer
    private String baseUrl = 'baseUrl'
    @Before
    void setup(){
        restTemplate = new RestTemplate()
        mockRestServiceServer =  MockRestServiceServer.createServer(restTemplate)

        serviceBrokerClientExtended = new ServiceBrokerClientExtended(restTemplate, baseUrl,'cfUsername','cfPassword','cfExtusername','cfExtpassword')
    }

    @Test
    void getUsage(){
        //Given
        mockRestServiceServer.expect(MockRestRequestMatchers.requestTo("${baseUrl}/custom/service_instances/${serviceInstanceId}/usage"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body("""{"value":"49651712","type":"watermark","end_date":null}"""))
        //Expect
        def result = serviceBrokerClientExtended.getUsage(serviceInstanceId).body
        Assert.assertEquals("49651712",result.value)
        Assert.assertEquals(ServiceUsageType.WATERMARK,result.type)
    }
}