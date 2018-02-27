package com.swisscom.cloud.sb.broker.updating;

import com.swisscom.cloud.sb.broker.model.ServiceInstance;
import com.swisscom.cloud.sb.broker.model.UpdateRequest;
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup;
import org.springframework.beans.factory.annotation.Autowired;

public class UpdatingService {
    @Autowired
    protected ServiceProviderLookup serviceProviderLookup

    UpdateResponse Update(ServiceInstance serviceInstance, UpdateRequest updateRequest, boolean acceptsIncomplete)
    {
        return new UpdateResponse(isAsync: acceptsIncomplete);
    }
}
