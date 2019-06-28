package uk.gov.ida.verifyserviceprovider.validators;

import org.opensaml.saml.saml2.core.Assertion;

import java.util.List;

public abstract class IdentityAssertionValidator {
    public abstract void validate(List<Assertion> assertions,
                                  String requestId);
}
