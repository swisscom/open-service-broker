# Contributing to Open Service Broker

The following is a set of guidelines for contributing to Open Service Broker,
which are hosted in the 
[Open Service Broker](https://github.com/swisscom/open-service-broker) on GitHub.
These are mostly guidelines, not rules. Use your best judgment, and feel free to
propose changes to this document in a pull request.

## Library Dependencies
All dependencies of the project are declared at `gradle/dependencies.gradle`
where can be consulted and updated by the members of the team. If you need to
add a new dependency to certain module, add the dependency in this file and
refer to it from your module.

### Example
First, at `gradle/dependencies.gradle` declare your dependency. Please, include
a small comment of why is needed or its main function.
```groovy
ext{
  versions = [
          groovy          : '2.5.2',  //Main modules are written in groovy
          ...
  ]
  libs = [
         groovy           : "org.codehaus.groovy:groovy-all:${versions.groovy}", 
         ...
```

Then in the `build.gradle` of your module:
```groovy
dependencies {
    compile libs.groovy
    ...
```

