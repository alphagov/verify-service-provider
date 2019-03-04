package uk.gov.ida.verifyserviceprovider.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class EntityIdService {
    private final List<String> configuredEntityIds;
    private final String defaultEntityId;
    private static final Logger LOG = LoggerFactory.getLogger(EntityIdService.class);

    public EntityIdService(List<String> configuredEntityIds) {
        this.configuredEntityIds = configuredEntityIds;
        this.defaultEntityId = configuredEntityIds.size() == 1 ? configuredEntityIds.get(0) : null;
    }

    public String getEntityId(@Nullable RequestGenerationBody requestGenerationBody) {
        String entityId = Optional.ofNullable(requestGenerationBody).map(RequestGenerationBody::getEntityId).orElse(null);
        LOG.info(String.format("Received request to generate authn request with entityId %s", entityId != null ? entityId : "from config"));
        return getEntityId(entityId);
    }

    public String getEntityId(TranslateSamlResponseBody translateSamlResponseBody) {
        String entityId = translateSamlResponseBody.getEntityId();
        LOG.info(String.format("Received request to translate a saml response with entityId %s", entityId != null ? entityId : "from config"));
        return getEntityId(entityId);
    }

    private String getEntityId(String providedEntityId) {
        if (providedEntityId == null) {
            if (defaultEntityId != null) {
                return defaultEntityId;
            } else {
                throw new InvalidEntityIdException("No entityId was provided, and there are several in config");
            }
        } else {
            if (configuredEntityIds.contains(providedEntityId)) {
                return providedEntityId;
            } else {
                throw new InvalidEntityIdException(String.format("Provided entityId: %s is not listed in config", providedEntityId));
            }
        }
    }
}
