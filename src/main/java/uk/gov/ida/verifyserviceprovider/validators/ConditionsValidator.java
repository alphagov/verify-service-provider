package uk.gov.ida.verifyserviceprovider.validators;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.List;

public class ConditionsValidator {
    private final TimeRestrictionValidator timeRestrictionValidator;

    public ConditionsValidator(TimeRestrictionValidator timeRestrictionValidator) {
        this.timeRestrictionValidator = timeRestrictionValidator;
    }

    public void validate(Conditions conditionsElement, String entityId) {
        if (conditionsElement == null) {
            throw new SamlResponseValidationException("Conditions is missing from the assertion.");
        }

        timeRestrictionValidator.validateNotBefore(conditionsElement.getNotBefore());

        DateTime notOnOrAfter = conditionsElement.getNotOnOrAfter();
        if (notOnOrAfter != null) {
            timeRestrictionValidator.validateNotOnOrAfter(notOnOrAfter);
        }

        if (conditionsElement.getProxyRestriction() != null) {
            throw new SamlResponseValidationException("Conditions should not contain proxy restriction element.");
        }

        if (conditionsElement.getOneTimeUse() != null) {
            throw new SamlResponseValidationException("Conditions should not contain one time use element.");
        }

        List<AudienceRestriction> audienceRestrictions = conditionsElement.getAudienceRestrictions();
        if (audienceRestrictions == null || audienceRestrictions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one audience restriction is expected.");
        }

        List<Audience> audiences = audienceRestrictions.get(0).getAudiences();
        if (audiences == null || audiences.size() != 1) {
            throw new SamlResponseValidationException("Exactly one audience is expected.");
        }

        String audience = audiences.get(0).getAudienceURI();
        if (!entityId.equals(audience)) {
            throw new SamlResponseValidationException(String.format("Audience must match entity ID. Expected %s but was %s", entityId, audience));
        }
    }
}
