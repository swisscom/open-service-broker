package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.model.ServiceDetail

class BindResponse {
    BindResponseDto credentials
    Collection<ServiceDetail> details

    /*
    Some services such as Atmos or Elk might have non-unique credentials.
    When this is the case, a 200 should be returned instead of 201.
    With the isUniqueCredentials flag, this behavior can be controlled.
    (see http://docs.cloudfoundry.org/services/api.html#binding for details)
    */
    boolean isUniqueCredentials = true
}
