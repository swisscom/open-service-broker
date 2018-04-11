#!/bin/bash

set -xe

MYSQL_USER=sb
MYSQL_PASSWORD=i6c0MrQYDO
MYSQL_DATABASE=openservicebroker
DATABASE_SERVICE_NAME=broker-mariadb

#Create mariadb
oc import-image mariadb:10.2 --confirm
oc new-app -f https://raw.githubusercontent.com/openshift/origin/master/examples/db-templates/mariadb-persistent-template.json \
	 -p DATABASE_SERVICE_NAME=$DATABASE_SERVICE_NAME -p MYSQL_USER=$MYSQL_USER -p MYSQL_PASSWORD=$MYSQL_PASSWORD \
         -p MYSQL_DATABASE=$MYSQL_DATABASE -p NAMESPACE=$(oc project -q) --allow-missing-images

#Create a config yml for broker which includes only db config 
oc create configmap broker  --from-literal application.yml="spring:
  datasource:
    url: 'jdbc:mysql://$DATABASE_SERVICE_NAME/$MYSQL_DATABASE?autoReconnect=true'
    username: $MYSQL_USER
    password: $MYSQL_PASSWORD"

#Build & deploy service broker
oc new-app registry.hub.docker.com/mcelep/s2i-gradle~https://github.com/swisscom/open-service-broker.git \
	 --build-env BUILDER_ARGS='clean build -x test -x integrationTest -x functionalTest' \
	 --build-env ARTIFACT_DIR='broker/build/libs/' \
	 --build-env APP_SUFFIX=$(sed -n 's/^version=//p' gradle.properties) \
	  --env APP_OPTIONS='-Dspring.config.location=file:/opt/config/application.yml'
oc set volume dc/open-service-broker --add --name=config --configmap-name=broker -m /opt/config/
