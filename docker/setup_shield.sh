#!/bin/bash
# Configures shield for automated testing
set -e

HOST='localhost:8443'

# Login
X_SHIELD_SESSION=$(curl -i -k -H 'Accept: application/json' -H 'Content-Type: application/json' -X POST https://${HOST}/v2/auth/login -d '{"username":"admin","password":"shield"}' | grep X-Shield-Session | awk '{print $2}')

# Initialize
curl -k -H 'Accept: application/json' -H "X-Shield-Session: ${X_SHIELD_SESSION}" -H 'Content-Type: application/json' -X POST https://${HOST}/v2/init -d '{"master":"shield"}'

# Get tenant1 uuid
tenant_uuid=$(curl -k -H 'Accept: application/json' -H "X-Shield-Session: ${X_SHIELD_SESSION}" "https://${HOST}/v2/tenants?name=tenant1&exact=t" | jq -r .[0].uuid)

# Create store default
curl -k -H 'Accept: application/json' -H 'Content-Type: application/json' -H "X-Shield-Session: ${X_SHIELD_SESSION}" -X POST https://${HOST}/v2/tenants/${tenant_uuid}/stores -d '{"name":"default","summary":"default","plugin":"s3","agent":"shield-agent:5444","config":{"access_key_id":"minio-access","secret_access_key":"minio-secret","bucket":"backup","s3_host":"minio","s3_port":"9000","skip_ssl_validation":true},"threshold": 1073741824}'

# Create policy month
curl -k -H 'Accept: application/json' -H 'Content-Type: application/json' -H "X-Shield-Session: ${X_SHIELD_SESSION}" -X POST https://${HOST}/v2/tenants/${tenant_uuid}/policies -d '{"name":"month","summary":"keep for a month","expires":2592000}'