package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.io.IOException;
import java.util.List;

import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class ResponseService {

    private final StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer;
    private final AssertionDecrypter assertionDecrypter;

    public ResponseService(
        StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer,
        AssertionDecrypter assertionDecrypter
    ) {
        this.stringToOpenSamlObjectTransformer = stringToOpenSamlObjectTransformer;
        this.assertionDecrypter = assertionDecrypter;
    }

    public TranslatedResponseBody convertTranslatedResponseBody(String decodedSamlResponse) throws IOException, SamlResponseValidationException {
        Response response = stringToOpenSamlObjectTransformer.apply(decodedSamlResponse);

        List<Assertion> assertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        if (assertions == null || assertions.isEmpty() || assertions.size() > 1) {
            throw new SamlResponseValidationException("Only one assertion is expected.");
        }

        Subject subject = assertions.get(0).getSubject();
        if (subject == null) {
            throw new SamlResponseValidationException("Subject is missing from the assertion.");
        }

        NameID nameID = subject.getNameID();
        if (nameID == null) {
            throw new SamlResponseValidationException("NameID is missing from the subject of the assertion.");
        }

        String pid = nameID.getValue();

        return new TranslatedResponseBody(
            "MATCH",
            pid,
            LEVEL_2,
            null
        );
    }

}
