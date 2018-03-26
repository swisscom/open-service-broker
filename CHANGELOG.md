# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
