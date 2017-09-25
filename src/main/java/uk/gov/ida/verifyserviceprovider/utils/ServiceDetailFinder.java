package uk.gov.ida.verifyserviceprovider.utils;

import uk.gov.ida.verifyserviceprovider.dto.ServiceDetails;

import java.util.List;

public class ServiceDetailFinder {
    private final List<ServiceDetails> services;

    public ServiceDetailFinder(List<ServiceDetails> serviceDetails) {
        this.services = serviceDetails;
    }

    public ServiceDetails getServiceDetails(String entityId) {
        if (entityId == null) {
            if (services.size() > 1) {
                throw new RuntimeException();
            }
            return services.get(0);
        }
        return services.stream().filter(service -> service.getEntityId().equals(entityId)).findFirst().orElseThrow(RuntimeException::new);
    }
}
