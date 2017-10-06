# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

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