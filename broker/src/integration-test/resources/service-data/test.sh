#!/bin/bash#
# A simple script to generate OSB metrics for testing

RANDOMNUMBER=$(env LC_CTYPE=C tr -dc '0-9' < /dev/urandom | head -c 2)
RANDOMNUMBER=1
echo $RANDOMNUMBER
for (( i = 0; i < $RANDOMNUMBER; i++ )); do
	RANDOMSTRING=$(env LC_CTYPE=C tr -dc "a-zA-Z0-9-_\$\?" < /dev/urandom | head -c 10)
curl -X POST \
  http://localhost:8080/custom/admin/service-definition \
  -H 'Authorization: Basic Y2NfZXh0OmNoYW5nZV9tZQ==' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 3a8dbfa3-3793-fc9d-1eee-2a9fb1dbd36e' \
  -d '{
  "guid": "dummyServiceProviderGuid'"$RANDOMSTRING"'",
  "name": "dummyServiceProvider'"$RANDOMSTRING"'",
  "description": "This service only allows provision/deprovision and takes around  a minute to reach the success state after action is triggered",
  "bindable": true,
  "internalName": "dummy",
  "displayIndex": 1,
  "tags": [],
  "metadata": {
    "version": "0.0.1",
    "displayName": "sync"
  },
  "plans": [
    {
      "guid": "dummyServiceProviderPlanGuid'"$RANDOMSTRING"'",
      "asyncRequired": false,
      "name": "dummyServiceProviderPlan'"$RANDOMSTRING"'",
      "description": "some description",
      "templateId": "",
      "free": true,
      "displayIndex": 0,
      "parameters": [],
      "metadata": {
        "displayName": "A plan"
      }
    }
  ]
}'

curl -X PUT \
  http://localhost:8080/v2/service_instances/testService$i \
  -H 'Authorization: Basic Y2NfYWRtaW46Y2hhbmdlX21l' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 3631129b-439c-530b-2a56-ca3ddf9d5e27' \
  -H 'X-Broker-API-Version: 2.13' \
  -d '{
  "service_id": "dummyServiceProviderGuid'"$RANDOMSTRING"'",
  "plan_id": "dummyServiceProviderPlanGuid'"$RANDOMSTRING"'",
  "context": {},
  "organization_guid": "string",
  "space_guid": "string",
  "parameters": [{
    "name":"success",
    "value":"false"
    }]
}'
done