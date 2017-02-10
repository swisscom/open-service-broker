# Cloud Foundry Service Broker

##Introduction
######Cloud Foundry Service Broker implements Cloud Foundry Service Broker API and enables user triggered deployment of MongoDB Enterprise Servers trough the Cloud Foundry command line interface. It enables Cloud Foundry customers to create and manage their service instances as needed.

Cloud Foundry is a popular choice for enterprises to build hybrid clouds. 
Cloud Foundry Service Broker is a modular implementation of the Cloud Foundry Service Broker API. It enables Cloud Foundry customers to provision and manage services according to their requirements.
Cloud Foundry Service Broker is built in a modular way.
It provides API endpoints for Catalog, Provisioning, Binding and Billing.
Services are integrated trough SP Plugins, which either provision their service synchronous or asynchronous.

![SB](./img/SB.png)
In the image above shows the basic workflow.
Users interact trough the Cloud Foundry API with the Service Broker.
Users can request service plans (Catalog), provision and bind services to apps. Additionally, Service Broker provides an API for billing purposes.

The following flow chart shows the interactions for service provisioning and service binding for MongoDB Enterprise. Please note, that this is generalised and does not represent actual calls.

![](./img/MongoDB-Enterprise_ServiceProvisioning-Binding.png)

## Development
## Prerequisite
- Java 1.8
- MySQL / MariaDB Server
## Deployment
To get started with Cloudfoundry Servicebroker clone the Git repository.

```bash
$ git clone 
```

Build service broker using the gradlew script in the root directory of the repository.

```bash
$ gradlew clean build
```

##JAVA_OPTS
```bash
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Xmx2048m -XX:MaxPermSize=1024m -XX:+UseConcMarkSweepGC"
```


##Configuration

The configuration file for the service broker is located under ```server/src/main/resources/application.yml```

# Service Definitions

## Get service definition

Via the example call below, service definition for a given service id can be retrieved.
```bash
curl -u "username:password" -X GET 'http://localhost:8080/custom/admin/service-definition/{service_id}'
```

## Add service definition

Service broker provides a way to update service definition via http calls.

Here is an example: 
```bash
curl -u "username:password" -X POST -H "Content-Type: application/json" --data-binary "@path/to/definition/file" 'http://localhost:8080/custom/admin/service-definition/{service_id}'
```
The interface can be used for both adding a new service or updating an existing one. For an existing service, if a plan that is in use is tried to be removed an exception will be thrown.

## Remove service definition

A service and its plan(s), which are not used i.e. have no service instances, can be removed via a rest interface.
Here is an example to delete a service that has id 'serviceGuid':
```bash
curl -u "username:password" -X DELETE 'http://localhost:8080/custom/admin/service-definition/{service_id}'
```

## Example Service Definition
```json
{
  "guid": "udn9276f-hod4-5432-vw34-6c33d7359c12",
  "name": "mongodbent",
  "description": "MongoDB Enterprise HA v3.2.11",
  "bindable": true,
  "asyncRequired": true,
  "internalName": "mongoDbEnterprise",
  "displayIndex": 1,
  "tags": [],
  "metadata": {
    "version": "3.2.11",
    "displayName": "MongoDB Enterprise"
  },
  "plans": [
    {
      "guid": "jfkos87r-truz-4567-liop-dfrwscvbnmk6",
      "name": "replicaset",
      "description": "Replica Set with 3 data bearing nodes with 32 GB memory, 320 GB storage, unlimited concurrent connections",
      "templateId": "mongodbent-bosh-template",
      "free": false,
      "displayIndex": 0,
      "containerParams": [
        {
          "template": "",
          "name": "plan",
          "value": "mongoent.small"
        },
        {
          "template": "",
          "name": "vm_instance_type",
          "value": "mongoent.small"
        }
      ],
      "metadata": {
        "storageCapacity": "320GB",
        "memory": "32GB",
        "nodes": "3",
        "maximumConcurrentConnections": "unlimited",
        "dedicatedService": true,
        "highAvailability": true,
        "displayName": "Small"
      }
    }
  ]
}
```

# Swagger

Swagger api documentation is accessible under http:/localhost:8080/swagger-ui.html