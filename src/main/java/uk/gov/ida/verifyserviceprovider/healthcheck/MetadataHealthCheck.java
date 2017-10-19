package uk.gov.ida.verifyserviceprovider.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;

public class MetadataHealthCheck extends HealthCheck {

    private MetadataResolver metadataResolver;
    private String expectedEntityId;

    public MetadataHealthCheck(
        MetadataResolver metadataProvider,
        String expectedEntityId
    ) {
        this.metadataResolver = metadataProvider;
        this.expectedEntityId = expectedEntityId;
    }

    @Override
    protected Result check() throws Exception {
        try {
            CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(expectedEntityId));
            EntityDescriptor entityDescriptor = metadataResolver.resolveSingle(criteria);
            if (entityDescriptor != null) {
                return healthy();
            }
            return unhealthy(getMessage("No exception was thrown"));
        } catch (Exception e) {
            return unhealthy(getMessage(e.getMessage()));
        }
    }

    private String getMessage(String message) {
        return "Could not load metadata for entity " + this.expectedEntityId + ". " + message + ". See the logs for more details.";
    }
}

