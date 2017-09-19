package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget

/*
 Example:
{
  "agent": "kubernetes-service-node.service.consul:'${port}'",
  "endpoint": "{}",
  "name": "redis-'${namespace}'",
  "plugin": "redis-kubernetes-shield-plugin",
  "summary": "redis-'${namespace}'"
}
 */

class KubernetesRedisShieldTarget implements ShieldTarget {
    String namespace
    int port

    @Override
    String pluginName() {
        "redis-kubernetes-shield-plugin"
    }

    @Override
    String endpointJson() {
        "{}"
    }
}
