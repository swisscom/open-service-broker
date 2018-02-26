package com.swisscom.cloud.sb.broker.config

class GuidUserConfig {
    String guid
    List<UserConfig> users

    @Override
    String toString() {
        return "GuidUserConfig{" +
                "guid=" + guid +
                "users=" + users +
                "}"
    }
}
