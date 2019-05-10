# Open service broker core

All the [Spring components](https://spring.io/projects/spring-framework) 
(entities, repositories, services, controllers, services) needed in any
 Spring boot application that provide an implementation of the 
 [Open service broker API](https://github.com/openservicebrokerapi/servicebroker/blob/v2.11/spec.md).
  
 
## Usage
In your Spring boot application `build.gradle`, include the following dependency:
```$groovy
dependencies {
    compile 'com.swisscom.cloud.sb:broker-core:6.1.1-SNAPSHOT'
```

See [broker Spring boot application](https://github.com/swisscom/open-service-broker/tree/develop/broker) for
an example of usage.

## Configuration 
In `src/main/resources/log4j*` you have an example of logging configuration. For an example of needed 
Spring configuration see [broker Spring boot application](https://github.com/swisscom/open-service-broker/tree/develop/broker).

For running the BoshFacadeTest or BoshClientTest against your bosh, the
[dummy bosh-release](https://github.com/pivotal-cf-experimental/dummy-boshrelease) needs to be uploaded.
You can set your bosh credentials in `~/.gradle/gradle.properties`:
```
boshUrl=<BASEURL OF YOUR BOSH>
uaaUrl=<BASEURL OF UAA USED FOR AUTHENTICATION>
boshUsername=<YOUR BOSH USERNAME>
boshPassword=<YOUR BOSH PASSWORD>
boshMocked=false
```

## Build
Run:
```bash
$ ./gradlew build
```

The resulting jar will be at `build/libs/broker-core-6.1.1-SNAPSHOT.jar`