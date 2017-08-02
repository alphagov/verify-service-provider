package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

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

    public TranslatedResponseBody convertTranslatedResponseBody(String decodedSamlResponse) throws IOException {
        Response response = stringToOpenSamlObjectTransformer.apply(decodedSamlResponse);

        List<Assertion> assertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        // TODO: test for error if assertions.size() != 1

        Subject subject = assertions.get(0).getSubject();

        // TODO: what if subject is null?

        NameID nameID = subject.getNameID();

        // TODO: what if name ID is null?

        String pid = nameID.getValue();


        return new TranslatedResponseBody(
            "MATCH",
            pid,
            LEVEL_2,
            null
        );
    }
}
