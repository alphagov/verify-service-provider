package uk.gov.ida.verifyserviceprovider.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdException;

import java.util.List;

public class ServiceEntityIdHelper {
    private final List<String> configuredEntityIds;
    private final String defaultEntityId;
    private static final Logger LOG = LoggerFactory.getLogger(ServiceEntityIdHelper.class);

    public ServiceEntityIdHelper(List<String> configuredEntityIds) {
        this.configuredEntityIds = configuredEntityIds;
        this.defaultEntityId = configuredEntityIds.size() == 1 ? configuredEntityIds.get(0) : null;
    }

    public String getEntityId(RequestGenerationBody requestGenerationBody) {
        if (requestGenerationBody.getEntityId() != null) {
            String entityId = requestGenerationBody.getEntityId();
            LOG.info(String.format("Received request to generate authn request specifying entityId: %s", entityId));
            if (configuredEntityIds.contains(entityId)) {
                return entityId;
            } else {
                throw new InvalidEntityIdException(String.format("Provided entityId: %s is not listed in config", entityId));
            }
        } else {
            LOG.info(String.format("Received request to generate authn request using default entityId"));
            if (defaultEntityId != null) {
                return defaultEntityId;
            } else {
                throw new InvalidEntityIdException("No entityId was provided, and there are several in config");
            }
        }
    }

    public String getEntityId(TranslateSamlResponseBody translateSamlResponseBody) {
        if (translateSamlResponseBody.getEntityId() != null) {
            String entityId = translateSamlResponseBody.getEntityId();
            LOG.info(String.format("Received request to translate a saml response for specified entityId: %s", entityId));
            if (configuredEntityIds.contains(entityId)) {
                return entityId;
            } else {
                throw new InvalidEntityIdException(String.format("Provided entityId: %s is not listed in config", entityId));
            }
        } else {
            LOG.info("Received request to translate a saml response using default entityId");
            if (defaultEntityId != null) {
                return defaultEntityId;
            } else {
                throw new InvalidEntityIdException("No entityId was provided, and there are several in config");
            }
        }
    }
}
