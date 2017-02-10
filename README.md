# Cloud Foundry Service Broker

##Introduction
######Cloud Foundry Service Broker implements Cloud Foundry Service Broker API and enables user triggered deployment of MongoDB Enterprise Servers trough the Cloud Foundry command line interface. It enables Cloud Foundry customers to create and manage their service instances as needed.

Cloud Foundry is a popular choice for cloud application platform. 
Cloud Foundry Service Broker is a modular implementation of the Cloud Foundry Service Broker API. It enables Cloud Foundry customers to provision and manage services according to their requirements.
Cloud Foundry Service Broker is built in a modular way and one service broker can host multiple services.
Service broker implements the Service Broker API defined under https://docs.cloudfoundry.org/services/api.html and it also provides some other extra functionality regarding Billing,etc.
Services can be provisioned synchronous and/or asynchronously.


![SB](./img/SB.png)
In the image above shows the basic workflow.
Users interact trough the Cloud Foundry API with the Service Broker.
Users can request service plans (Catalog), provision and bind services to apps. Additionally, Service Broker provides an API for billing purposes.

The following flow chart shows the interactions for service provisioning and service binding for MongoDB Enterprise. Please note, that this is generalised and does not represent actual calls.

![](./img/MongoDB-Enterprise_ServiceProvisioning-Binding.png)

# Deployment

Follow the [documentation](http://docs.cloudfoundry.org/services/managing-service-brokers.html) to register the broker
to Cloud Foundry.

Before a 'cf create-service-broker' or 'update-service-broker' call is made, please make sure that service broker is configured correctly.
For configuring the catalog, see the service definition section.

##JAVA_OPTS
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Xmx2048m -XX:MaxPermSize=1024m -XX:+UseConcMarkSweepGC"


##Configuration


# Service Definitions

## Get service definition

Via the example call below, service definition for a given service id can be retrieved.

curl -u "username:password" -X GET 'http://localhost:8080/cf-broker/service-definition/$serviceGuid'


## Add service definition

Service broker provides a way to update service definition via http calls.

Here is an example: curl -u "username:password" -X POST -H "Content-Type: application/json" --data-binary "@path/to/definition/file" 'http://localhost:8080/cf-broker/service-definition'

The interface can be used for both adding a new service or updating an existing one. For an existing service, if a plan that is in use is tried to be removed an exception will be thrown.

## Remove service definition

A service and its plan(s), which are not used i.e. have no service instances, can be removed via a rest interface.
Here is an example to delete a service that has id 'serviceGuid':
curl -u "username:password" -X DELETE 'http://localhost:8080/cf-broker/service-definition/serviceGuid'

# Swagger

Swagger api documentation is accessible under http:/localhost:8080/swagger-ui.html

# Development
## Prerequisite
Java 1.8


