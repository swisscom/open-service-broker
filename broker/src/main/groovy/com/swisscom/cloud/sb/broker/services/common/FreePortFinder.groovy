package com.swisscom.cloud.sb.broker.services.common

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper

import java.lang.reflect.ParameterizedType

abstract class FreePortFinder<T extends ServiceProvider> {
    private final String portRange
    private final ServiceInstanceRepository serviceInstanceRepository

    FreePortFinder(String portRange, ServiceInstanceRepository serviceInstanceRepository) {
        this.portRange = portRange
        this.serviceInstanceRepository = serviceInstanceRepository
    }

    List<Integer> findFreePorts(int count = 1) {
        if (portRange.contains('-')) {
            int startRange = portRange.substring(0, portRange.indexOf('-')) as int
            int endRange = portRange.substring(portRange.indexOf('-') + 1) as int
            def searchSpace = (startRange..<endRange).toList()
            searchSpace = searchSpace - alreadyReservedPorts()
            Collections.shuffle(searchSpace)
            if (searchSpace.size() == 0 || count > searchSpace.size()) {
                throw new RuntimeException('There are no free ports left!')
            }
            return searchSpace.take(count)
        } else {
            throw new RuntimeException('PortRange should be in format:"n1-n2" where n2>n1 e.g. "27000-45000"')
        }
    }

    private List<Integer> alreadyReservedPorts() {
        List<ServiceInstance> serviceInstances = findServiceInstancesOfServiceProvider()

        return serviceInstances?.collect { ServiceInstance si ->
            Optional<String> optionalPort = ServiceDetailsHelper.from(si.details).findValue(ServiceDetailKey.PORT)
            return optionalPort.present ? optionalPort.get() as int : 0
        }
    }

    private List<ServiceInstance> findServiceInstancesOfServiceProvider() {
        return serviceInstanceRepository.listAllForInternalName(ServiceProviderLookup.findInternalName(findGenericType()))
    }

    private Class findGenericType() {
        return ((Class) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0])
    }
}
