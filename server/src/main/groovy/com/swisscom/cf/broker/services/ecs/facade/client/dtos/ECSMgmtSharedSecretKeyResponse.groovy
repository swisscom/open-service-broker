package com.swisscom.cf.broker.services.ecs.facade.client.dtos


class ECSMgmtSharedSecretKeyResponse implements Serializable {
    Object link
    String key_expiry_timestamp
    String key_timestamp
    String secret_key
}
