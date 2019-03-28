# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]



## [6.2.0] - 2019-03-28
- [MINOR] Refactor to separate broker-core from broker
- Add `MinimalStateMachineContext` interface as a base `StateMachineContext`

## [6.1.2] - 2019-03-27
- Try patch build to resolve blocked release 6.1.1 on sonatype

## [6.1.1] - 2019-03-26
- Fix Bean classloader for inventory abstraction layer

## [6.1.0] - 2019-03-19
- [MINOR] Handle generic configs for boshbased services in statemachine

## [6.0.4] - 2019-03-16
- Add stabilization fix for inventory abstraction layer

## [6.0.3] - 2019-03-12
- Add pre action hooks to AsyncServiceProvider (beforeProvision,beforeUpdate,beforeDeprovision)
- Add inventory abstraction layer to access service instance related informations
- Clean up and update gradle

## [6.0.2] - 2019-03-11
- Make generic config management accessible through BoshFacade

## [6.0.1] - 2019-03-07
- Add trait `ParentServiceProvider` which provides the default logic for parent-child providers

## [6.0.0] - 2019-03-05
- [MAJOR] Remove unused OpenStack and cloud-config capability for bosh based services

## [5.3.6] - 2019-02-20
- Improved MongoDB Enterprise binding reliability

## [5.3.5] - 2019-02-13
- Bugfix `GetServiceDetailByKeyAndValue` for cases where the combination is not unique

## [5.3.4] - 2019-02-06
- Insert protocolVersion for new opsmanager version 4.0

## [5.3.3] - 2019-01-18
- Bugfix in `ServiceDefinitionProcessor` to handle service and plan meta strings correctly

## [5.3.2] - 2019-01-17
- Add Basic and Bearer authentication for RestTemplateBuilder

## [5.3.1] - 2019-01-16
- Fix issue with multiline release description

## [5.3.0] - 2019-01-15
- [MINOR] Add cleanup backup job, which removes all deleted or failed backups older then 14 days
- Change backup endpoint to return 410 when deleting a backup which doesn't exist anymore
- [MINOR] Add ADMIN Endpoint to terminate last operations (see readme)  

## [5.2.1] - 2019-01-10
- Bugfix for LogContextEnrichInterceptor

## [5.2.0] - 2019-01-10
- [MINOR] Increase Quartz Scheduler Thread Count to 20 and use withMisfireHandlingInstructionNowWithExistingCount policy

## [5.1.11] - 2019-01-09
- Add `dashboard_url` support for MariaDB Service Provider

## [5.1.10] - 2018-12-21
- Fix meta data LinkedHashMap to Array for compliance reasons

## [5.1.9] - 2018-12-20
- Fix Issue where arrays couldn't be used as meta data in plan or service in the servicedefinition

## [5.1.8] - 2018-12-18
- Fix Issue where logging context enricher crashes if there are no URI parameters
- Rework profiles.actives in yml to simplify overwrite of profiles

## [5.1.7] - 2018-12-12
- Refactor serviceBrokerClient to make createHttpEntity public, so that this can be used to create requests with payload

## [5.1.6] - 2018-12-10
- Fix issue where ServiceDetails are not saved from the state machine while execute an service instance update

## [5.1.5] - 2018-12-06
- Add MDC to add serviceInstanceGuid as a common metadata for all service log actions
- Cleanup duplicated/unnecessary log code
- Refactor serviceBrokerClient to reduce duplicated code and allow custom calls with exchange or extendedExchange

## [5.1.4] - 2018-11-26
- ServiceInstance.children will not contain deleted elements anymore
- Improve ServiceDefinitionInitializer (logging)
- When deleting a Plan which is still in use, Plan will be deactivated instead of delete denied
- Relational Properties have been changed to lazy loading instead of eager
- ServiceDetails now have a relational property linking to serviceInstance
- ServiceDetailRepository allows finding ServiceDetails by key and value
- Provision/Deprovision/Update/Bind/Unbind are producing audit marked log entries.

## [5.1.3] - 2018-11-22
- Add proxy support for RestTemplateBuilder

## [5.1.2] - 2018-11-20
- Add spring-cloud-starter-config dependency to fetch config from config server

## [5.1.1] - 2018-11-15
- Check every develop commit for new release
- Fix CredHub set credential bug with `mode` parameter

## [5.1.0] - 2018-11-13
- [MINOR] Add CredHubServiceProvider
- Add Audit log functionality

## [5.0.23] - 2018-11-08
- Improve automated release

## [5.0.22] - 2018-11-06
- Automate release process
- Add helper to activate services and plans for testing
- Fix issue where error messages are not correctly returned on last operations
- Fix bug where shield backups could not be deleted when shield returns HTTP status code 404.
- Fix bug with 404 during MongoDB unbind
- Fix bug with MariaDB ShieldTargets without bindir

## [5.0.21] - 2018-11-01
- Fixes bug where shield backups could not be deleted when shield returns HTTP status code 404.

## [5.0.20] - 2018-10-31
- Use springs SNAPSHOT repository for spring-credhub-starter

## [5.0.19] - 2018-10-30
- Make Service Broker compatible with CredHub 2.0
- Use `TEXT` for service_detail values to support longer values
- Update Spring Boot to 2.0.6
- Introduce application-test.yml for test configuration with docker-compose

## [5.0.18] - 2018-10-29
- Add session affinity for K8S update to ensure pods are scheduled properly


## [5.0.17] - 2018-10-15
- Fix bug that prevented isKubernetesUpdateSuccessful to return true


## [5.0.16] - 2018-10-12
- Fix Kubernetes Endpointmapper string renaming in update case


## [5.0.15] - 2018-10-12
- Fix Kubernetes Endpointmapper string renaming


## [5.0.14] - 2018-10-11
- Add proper update versioning logic dor K8S update service
- Fix update service for mongnodb-2


## [5.0.13] - 2018-10-04
- Fix casting error in K8S update


## [5.0.12] - 2018-10-03
- Fix conversion of K8S Service Response entity


## [5.0.11] - 2018-10-03
- Fix issue [#175](https://github.com/swisscom/open-service-broker/issues/175)


## [5.0.10] - 2018-10-03
- Remove unnecessary CredHub config in application.yml

## [5.0.9] - 2018-10-03
- Add resourceVersion field to K8S update
- Use boshCredHub config enable field

## [5.0.8] - 2018-10-02
- Add feature writeCertificate to allow writing exising certificate to CredHub

## [5.0.7] - 2018-10-02
- Fix K8S update-service
- Fix Error in CredHub Migration
- Implement special Template parsing in BOSH Templates (replace "{{key}}" with value instead of "value")

## [5.0.6] - 2018-10-01
- Fix endpoint lookup
- Use client-id for credhub oauth

## [5.0.5] - 2018-09-26
- Fix flyway migration 1.0.23

## [5.0.4] - 2018-09-25
- Service definitions from application.yml are source of truth, additional service definitions in DB are removed if possible or flagged as inactive if they are still in use.
- Support Shield v8 (SHield-API v2)

## [5.0.3] - 2018-09-20
- Use `spring.config.additional-location` instead of `spring.config.location`

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
