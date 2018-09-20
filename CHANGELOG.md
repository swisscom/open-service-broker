# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]



## [5.0.2] - 2018-09-20
- Remove unnecessary quotes in bosh-manifest

## [5.0.1] - 2018-09-19



## [5.0.0] - 2018-09-19
- [MAJOR] Upgrade Dependencies and Refactor Configuration Objects
    - Spring Boot (1.5.14 -> 2.0.4)
    - Tomcat (7.0.88 -> 9.0.11)
    - Groovy (2.4.15 -> 2.5.2)
    - Spock (1.1-groovy-2.4 -> 1.2-RC2-groovy-2.5)
    - mysql-connector-java (.. -> 8.0.12)
    - ...

## [4.2.5] - 2018-09-13
- Mongodb Enterprise - Delete default alerts on service creation
- Adds context object to update request
- Adapts Kubernetes Provisioning 


## [4.2.4] - 2018-09-06
- Fix inheritance bug of RequestWithParameters


## [4.2.3] - 2018-09-04

## [4.2.2] - 2018-09-04
- Add log4j2 files to .gitignore
- Fixes minor bug in abstract kubernetes provisioning

## [4.2.1] - 2018-08-30

## [4.2.0] - 2018-08-30
- Replace Logging Output from Slf4j-test to Slf4j-Log4j2
- \#140 Support multiple CredHubs
- \#141 Move from Travis to CircleCI
- Introduce common interface for Provision and Update Requests

## [4.1.2] - 2018-08-03
- Do not push release branch during release process
- Set Nexus/SonaType stagingProfileId

## [4.1.1] - 2018-08-03
- \#90 Improve README service-definition API documentation

## [4.1.0] - 2018-08-03
- \#97 Add metrics export (influxdb) for service provision,binding,lastoperation and lifecycletime. default activated
    can be deactivated via .yml

## [4.0.6] - 2018-07-26
- Abstract RelationalDb Service Provider for easier reuse

## [4.0.5] - 2018-07-11
- Return operation field according to spec 

## [4.0.4] - 2018-07-06
- Return dashboard_url according to spec

## [4.0.3] - 2018-07-03
- Get a proper version for mongodb ent
- Support UAA authentication for BOSH based services
- Fix /version endpoint

## [4.0.2] - 2018-06-27
- Revert change in `V1_0_1__quartz_tables_mysql_innodb.sql` to match checksum again

## [4.0.1] - 2018-06-26
- Support Update Service for MongoDB Enterprise
- MongoDB Enterprise improvements
- Add Copyright Headers

## [4.0.0] - 2018-06-19
- Rename deleteServiceBindingAndServiceInstaceAndAssert function in the ServiceLifeCylcer to deleteServiceBindingAndServiceInstanceAndAssert
- Fix method calls in StateBasedServiceProvider (#123)
- Add MariaDB Service Provider
- [MAJOR] Change return type of value in ExtendedUsage EndPoint from string to float (#125)

## [3.0.0] - 2018-06-07
- Fix issue that StateMachineBasedServiceProvider has no access to Context information (#116)

## [2.13.1] - 2018-06-05
- Fix build script for gradle 4 to include test jar sources
- Fix exchange response object in ServiceBrokerClient (#115)

## [2.13.0] - 2018-06-04
- Fix issue #110 last operation not allowing querying by operationId (client)
- Add additional extended usage endpoint to service instance
- Add custom ServiceHealth Endpoint to service instance
- Fix LoggingInterceptor thread unsafe operation

## [2.12.1] - 2018-05-07
- Fix not found binding and instance status code

## [2.12.0] - 2018-04-13
- Fixed issue that not all flags were correctly returned in the catalog (*bindable)
- Support custom service provider service binding fetch
- Support policy based random string generation
- Support retrying backup in case of failure

## [2.11.6] - 2018-04-11
- Add dto validation helper class for serviceproviders
- Support Openshift deployment
- Improve Service Binding fetch response

## [2.11.5] - 2018-04-10
- Fix childs reference

## [2.11.4] - 2018-04-09
- Rename parameter parentReference to parent_reference

## [2.11.3] - 2018-04-09
- Support Service update
- Add child and parent references for service instance
- Support updating service bindings
- Support generic Service Provider error codes
- Increase size of last_operation description field

## [2.11.2] - 2018-04-03

## [2.11.1] - 2018-04-03
- Refactor openAPI 3.0 method
- Fix enforce service details uniqueness in DB
- Support lastOperation error description

## [2.11.0] - 2018-03-29
- Support Extensions [spec](https://github.com/openservicebrokerapi/servicebroker/pull/431)
- Support mock flags

## [2.10.2] - 2018-03-26
- Bugfix for authenticated REST calls
- Support binding in DummyServiceProvider

## [2.10.1] - 2018-03-22
- Refactor getServiceInstanceDetails to fetchServiceInstance
- Add RestTemplate LoggingInterceptor

## [2.10.0] - 2018-03-19
- Add Service Instance Relationship
- Refactor OSB Context Support
- Support plan_updateable field for povision
- Support parameters field for provision and bind
- Support get Service Instance [spec](https://github.com/openservicebrokerapi/servicebroker/pull/333)
- Add support for service parameter update
- Fix race condition for Kubernetes Redis provision

## [2.9.2] - 2018-03-12
- Fix SQL Migration for Service Context

## [2.9.1] - 2018-03-08
- Fix SQL Migration for Service Context

## [2.9.0] - 2018-02-28
- Add Context to provision and bind while deprecating org and space ID for provisioning.
- Add manifest for Cloud Foundry deployment
- Add way to handle multiple users

## [2.8.0] - 2018-02-20
- Add extensions for Redis backup and restore 

## [2.7.0] - 2018-02-02
- Add support for multiple application users 

## [2.6.7] - 2018-01-25
- Fix missing release version

## [2.6.6] - 2018-01-25
- Fix to get usage for deleted Service Instance

## [2.6.5] - 2017-11-01
- make it possible to ignore false hostnames in HTTPS requests

## [2.6.2] - 2017-10-10
- Add fix to escape template literals for k8s templates
- Refactor `AbstractKubernetesFacade` to more common abstract methods `provision` and `getServiceBinding`

## [2.6.1] - 2017-10-06
- Fix check for k8s namespace deletion
- Fix preconditions for service deprovision

## [2.6.0] - 2017-10-02
- Remove `MongoDbClient` because its not used
- Optimized abstraction for k8s provisioning

## [2.5.0] - 2017-09-25
- Add new `RestTemplateBuilder` for advanced `RestTemplate` capabilities
- Refactoring to `AbstractServiceDetailKey` which enables service specific detail keys
- Refactoring to `AbstractTemplateConstants` which enables service specific template constants

## [2.4.0] - 2017-09-18
- Add system level backups for k8s services
- Fix extract Shield agent port from k8s deployment response
- Fix escaping list in `build.gradle` to handle provisioning templates with placeholders properly

## [2.3.6] - 2017-09-12
- Add debug messages to k8s provision template binding

## [2.3.5] - 2017-09-10
- Add option to shuffle availability zones in Bosh templates
- Add mongodb version field for Ops Manager automation update
- Fix to return all Redis (k8s) ports on bind request

## [2.3.4] - 2017-08-30
- Fix spelling for `service.kubernetes.redis.v1.kubernetesRedisHost`

## [2.3.3] - 2017-08-30
- Add default value for `service.kubernetes.redis.v1.kubernetesRedisHost`

## [2.3.2] - 2017-08-28
- Add default value for `service.kubernetes.redisConfigurationDefaults.kubernetesRedisHost`

## [2.3.1] - 2017-08-24
- Add Kubernetes service config properties for ip-ranges and protocols.

## [2.3.0] - 2017-08-24

### Added
- New field `templateVersion` on plan.
- New field `serviceProviderClass` on plan and service to specify the corresponding `ServiceProvider`. The field `internalName` will be replaced soon.
