package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.List;

import static java.util.Optional.ofNullable;

public class AssertionTranslator {

    public TranslatedResponseBody translate(List<Assertion> assertions) {
        if (assertions == null || assertions.isEmpty() || assertions.size() > 1) {
            throw new SamlResponseValidationException("Exactly one assertion is expected.");
        }

        Assertion assertion = assertions.get(0);
        Subject subject = assertion.getSubject();
        if (subject == null) {
            throw new SamlResponseValidationException("Subject is missing from the assertion.");
        }

        NameID nameID = subject.getNameID();
        if (nameID == null) {
            throw new SamlResponseValidationException("NameID is missing from the subject of the assertion.");
        }

        List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
        if (authnStatements == null || authnStatements.size() != 1) {
            throw new SamlResponseValidationException("Exactly one authn statement is expected.");
        }

        AuthnStatement authnStatement = authnStatements.get(0);
        String levelOfAssuranceString = ofNullable(authnStatement.getAuthnContext())
            .map(AuthnContext::getAuthnContextClassRef)
            .map(AuthnContextClassRef::getAuthnContextClassRef)
            .orElseThrow(() -> new SamlResponseValidationException("Expected a level of assurance."));

        LevelOfAssurance levelOfAssurance;
        try {
            levelOfAssurance = LevelOfAssurance.fromSamlValue(levelOfAssuranceString);
        } catch (Exception ex) {
            throw new SamlResponseValidationException("Level of assurance '" + levelOfAssuranceString + "' is not supported.");
        }

        return new TranslatedResponseBody(
            "MATCH",
            nameID.getValue(),
            levelOfAssurance,
            null
        );

    }
}
