package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.PrivateKeyStoreFactory;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.services.AssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import java.security.PrivateKey;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.NameIdBuilder.aNameId;
import static uk.gov.ida.saml.core.test.builders.ResponseBuilder.aResponse;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;

public class ResponseServiceTest {

    private ResponseService responseService;

    private StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer;
    private AssertionDecrypter assertionDecrypter;
    private XmlObjectToBase64EncodedStringTransformer<XMLObject> responseToBase64StringTransformer = new XmlObjectToBase64EncodedStringTransformer<>();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TestCredentialFactory testCredentialFactory;

    @Before
    public void setUp() {
        // Note: the private key and the encrypting credential need to be from the same keypair
        PrivateKey privateKey = new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP).getEncryptionPrivateKeys().get(0);
        testCredentialFactory = new TestCredentialFactory(TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_RP_PRIVATE_ENCRYPTION_KEY);

        ResponseFactory responseFactory = new ResponseFactory(privateKey, privateKey);

        stringToOpenSamlObjectTransformer = ResponseFactory.createStringToResponseTransformer();
        assertionDecrypter = responseFactory.createAssertionDecrypter();

        responseService = new ResponseService(
            stringToOpenSamlObjectTransformer,
            assertionDecrypter,
            new AssertionTranslator()
        );
    }

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldConvertTranslatedResponseBody() throws Exception {
        Response response = aResponse()
            .withNoDefaultAssertion()
            .addEncryptedAssertion(
                anAssertion()
                    .withSubject(aSubject()
                        .withNameId(aNameId().withValue("some-pid").build())
                        .build())
                    .addAuthnStatement(anAuthnStatement()
                        .withAuthnContext(anAuthnContext()
                            .withAuthnContextClassRef(anAuthnContextClassRef()
                                .withAuthnContextClasRefValue(IdaAuthnContext.LEVEL_2_AUTHN_CTX)
                                .build())
                            .build())
                        .build())
                    .buildWithEncrypterCredential(testCredentialFactory.getEncryptingCredential())
            )
            .build();

        TranslatedResponseBody result = responseService.convertTranslatedResponseBody(responseToBase64StringTransformer.apply(response));

        assertThat(result).isEqualTo(new TranslatedResponseBody(
            "MATCH",
            "some-pid",
            LevelOfAssurance.LEVEL_2,
            null
        ));
    }
}