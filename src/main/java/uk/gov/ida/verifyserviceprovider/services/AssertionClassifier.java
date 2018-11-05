package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;

public class AssertionClassifier {

    public enum AssertionType {AUTHN_ASSERTION, MDS_ASSERTION}

    public AssertionType classifyAssertion(Assertion assertion) {
        if (!assertion.getAuthnStatements().isEmpty()) {
            return AssertionType.AUTHN_ASSERTION;
        }
        return AssertionType.MDS_ASSERTION;
    }

}