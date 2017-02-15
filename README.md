# Cloud Foundry Service Broker

## Introduction

> Cloud Foundry Service Broker implements the Cloud Foundry Service Broker API and enables user triggered deployments of MongoDB Enterprise Servers trough the Cloud Foundry command line interface. It enables Cloud Foundry customers to create and manage their service instances as needed.

Cloud Foundry Service Broker is a modular implementation of the Cloud Foundry Service Broker API. It enables Cloud Foundry customers to provision and manage services according to their requirements.
Cloud Foundry Service Broker is built in a modular way and one service broker can host multiple services.
Service broker implements the Service Broker API defined under <https://docs.cloudfoundry.org/services/api.html> and it also provides some other extra functionality regarding Billing, etc.
Services can be provisioned synchronously and/or asynchronously.

![Service Broker](./img/SB.png)
The image above shows the basic workflow.
Users interact trough the Cloud Foundry API with the Service Broker.
Users can request service plans (Catalog), and provision and bind services to apps. Additionally, the Service Broker provides an API for billing purposes.

The following flow chart shows the interactions for service provisioning and service binding for MongoDB Enterprise. Please note, that this is generalized and does not represent actual calls.

![flow chart](./img/MongoDB-Enterprise_ServiceProvisioning-Binding.png)

## Development

### Preconditions

- Java 1.8
- MySQL / MariaDB Server


## Deployment

### Build

Build Service Broker using the `gradlew` script in the root directory of the repository.

```bash
$ gradlew clean build
```

Follow the [documentation](http://docs.cloudfoundry.org/services/managing-service-brokers.html) to register the broker
to Cloud Foundry.

Before a `cf create-service-broker` or `update-service-broker` call is made, please make sure that Service Broker is configured correctly.
For configuring the catalog, see the service definition section.

### JAVA_OPTS

```bash
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Xmx2048m -XX:MaxPermSize=1024m -XX:+UseConcMarkSweepGC"
```

## Configuration

The configuration file for the Service Broker is located under

```
server/src/main/resources/application.yml
```

## Service Definitions

### Get service definition

Via the example call below, service definitions for a given service id can be retrieved.

```bash
curl -u 'username:password' -X GET 'http://localhost:8080/custom/admin/service-definition/{service_id}'
```

### Add service definition

Service Broker provides a way to update service definitions via HTTP calls.

Here is an example:

```bash
curl -u 'username:password' -X POST -H 'Content-Type: application/json' --data-binary '@path/to/definition/file' 'http://localhost:8080/custom/admin/service-definition/{service_id}'
```

This interface can be used for both adding a new service or updating an existing one. For an existing service, if a plan that is in use is tried to be removed an exception will be thrown.

### Remove service definition

A service and its plan(s), which are not used i.e. which have no service instances, can be removed via a REST interface.
Here is an example for how to delete a service that has the id `service_id`:

```bash
curl -u 'username:password' -X DELETE 'http://localhost:8080/custom/admin/service-definition/{service_id}'
```

### Example Service Definition

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

## Swagger

The Swagger API documentation is accessible at <http:/localhost:8080/swagger-ui.html>
