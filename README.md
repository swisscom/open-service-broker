# Cloud Foundry Service Broker

##Introduction


## Development
## Prerequisite
Java 1.8 
Gradle 
Any Java / Grails IDE, IntelliJ is recommended by the author.

## Deployment

##JAVA_OPTS
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Xmx2048m -XX:MaxPermSize=1024m -XX:+UseConcMarkSweepGC"


##Configuration

# Service Definitions

## Get service definition

Via the example call below, service definition for a given service id can be retrieved.

curl -u "username:password" -X GET 'http://localhost:8080/cf-broker/service-definition/$serviceGuid'


## Add service definition

Service broker provides a way to update service definition via http calls. The service definition files are in project https://gitlab.swisscloud.io/appc-cf-services/appc-cf-service-manager-templates.

Here is an example: curl -u "username:password" -X POST -H "Content-Type: application/json" --data-binary "@path/to/definition/file" 'http://localhost:8080/cf-broker/service-definition'

The interface can be used for both adding a new service or updating an existing one. For an existing service, if a plan that is in use is tried to be removed an exception will be thrown.

## Remove service definition

A service and its plan(s), which are not used i.e. have no service instances, can be removed via a rest interface.
Here is an example to delete a service that has id 'serviceGuid':
curl -u "username:password" -X DELETE 'http://localhost:8080/cf-broker/service-definition/serviceGuid'



# Swagger

Swagger api documentation is accessible under http:/localhost:8080/swagger-ui.html