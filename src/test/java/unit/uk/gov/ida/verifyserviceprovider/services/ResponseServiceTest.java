package unit.uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableList;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.PrivateKeyStoreFactory;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.services.AssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import java.security.PrivateKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.NameIdBuilder.aNameId;
import static uk.gov.ida.saml.core.test.builders.ResponseBuilder.aResponse;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder.anEntityDescriptor;
import static uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder.aKeyDescriptor;
import static uk.gov.ida.saml.core.test.builders.metadata.SPSSODescriptorBuilder.anSpServiceDescriptor;
import static uk.gov.ida.verifyserviceprovider.dto.Scenario.SUCCESS_MATCH;

public class ResponseServiceTest {

    private ResponseService responseService;

    private XmlObjectToBase64EncodedStringTransformer<XMLObject> responseToBase64StringTransformer = new XmlObjectToBase64EncodedStringTransformer<>();

    private MetadataResolver hubMetadataResolver;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TestCredentialFactory encryptionCredentialFactory;
    private Credential testRpSigningCredential;

    @Before
    public void setUp() throws ComponentInitializationException {
        // Note: the private key and the encrypting credential need to be from the same keypair
        PrivateKey privateKey = new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP).getEncryptionPrivateKeys().get(0);
        encryptionCredentialFactory = new TestCredentialFactory(TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_RP_PRIVATE_ENCRYPTION_KEY);
        testRpSigningCredential = new TestCredentialFactory(TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY).getSigningCredential();

        hubMetadataResolver = mock(MetadataResolver.class);

        ResponseFactory responseFactory = new ResponseFactory(privateKey, privateKey);

        responseService = responseFactory.createResponseService(
            hubMetadataResolver,
            new AssertionTranslator(mock(SamlAssertionsSignatureValidator.class))
        );
    }

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldConvertTranslatedResponseBody() throws Exception {
        EntityDescriptor entityDescriptor = createEntityDescriptorWithSigningCertificate(TEST_RP_PUBLIC_SIGNING_CERT);
        when(hubMetadataResolver.resolve(any())).thenReturn(ImmutableList.of(entityDescriptor));
        Response response = createResponseSignedBy(testRpSigningCredential);

        TranslatedResponseBody result = responseService.convertTranslatedResponseBody(
            responseToBase64StringTransformer.apply(response),
            response.getInResponseTo()
        );

        assertThat(result).isEqualTo(new TranslatedResponseBody(
            SUCCESS_MATCH,
            "some-pid",
            LevelOfAssurance.LEVEL_2,
            null
        ));
    }

    @Test(expected = SamlTransformationErrorException.class)
    public void shouldFailValidationWhenMetadataDoesNotContainCorrectCertificate() throws Exception {
        Response response = createResponseSignedBy(testRpSigningCredential);
        EntityDescriptor entityDescriptor = createEntityDescriptorWithSigningCertificate(TEST_PUBLIC_CERT);

        when(hubMetadataResolver.resolve(any())).thenReturn(ImmutableList.of(entityDescriptor));

        responseService.convertTranslatedResponseBody(
            responseToBase64StringTransformer.apply(response),
            response.getInResponseTo()
        );
    }

    @Test(expected = SamlTransformationErrorException.class)
    public void shouldFailValidationWhenResponseIsNotSigned() throws Exception {
        Response response = createResponseBuilder().withoutSigning().build();
        EntityDescriptor entityDescriptor = createEntityDescriptorWithSigningCertificate(TEST_RP_PUBLIC_SIGNING_CERT);

        when(hubMetadataResolver.resolve(any())).thenReturn(ImmutableList.of(entityDescriptor));

        responseService.convertTranslatedResponseBody(
            responseToBase64StringTransformer.apply(response),
            response.getInResponseTo()
        );
    }

    @Test(expected = SamlResponseValidationException.class)
    public void shouldFailWhenInResponseToDoesNotMatchRequestId() throws Exception {
        EntityDescriptor entityDescriptor = createEntityDescriptorWithSigningCertificate(TEST_RP_PUBLIC_SIGNING_CERT);
        when(hubMetadataResolver.resolve(any())).thenReturn(ImmutableList.of(entityDescriptor));
        Response response = createResponseSignedBy(testRpSigningCredential);

        responseService.convertTranslatedResponseBody(
            responseToBase64StringTransformer.apply(response),
            "some-incorrect-request-id"
        );
    }

    private EntityDescriptor createEntityDescriptorWithSigningCertificate(String signingCert) throws MarshallingException, SignatureException {
        return anEntityDescriptor()
            .addSpServiceDescriptor(anSpServiceDescriptor()
                .withoutDefaultSigningKey()
                .addKeyDescriptor(aKeyDescriptor().withX509ForSigning(signingCert).build())
                .build()
            )
            .build();
    }

    private Response createResponseSignedBy(Credential signingCredential) throws MarshallingException, SignatureException {
        return createResponseBuilder()
            .withSigningCredential(signingCredential).build();
    }

    private ResponseBuilder createResponseBuilder() {
        return aResponse()
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
                    .buildWithEncrypterCredential(encryptionCredentialFactory.getEncryptingCredential())
            );
    }
}