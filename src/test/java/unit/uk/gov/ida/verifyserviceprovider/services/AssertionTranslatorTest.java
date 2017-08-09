package unit.uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.PrivateKeyStoreFactory;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.services.AssertionTranslator;

import java.security.PrivateKey;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.extensions.IdaAuthnContext.LEVEL_2_AUTHN_CTX;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder.anEntityDescriptor;
import static uk.gov.ida.saml.core.test.builders.metadata.IdpSsoDescriptorBuilder.anIdpSsoDescriptor;
import static uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder.aKeyDescriptor;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.Scenario.SUCCESS_MATCH;

public class AssertionTranslatorTest {

    private AssertionTranslator translator;
    private Credential testRpMsaSigningCredential =
        new TestCredentialFactory(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY).getSigningCredential();

    @Before
    public void setUp() throws Exception {
        PrivateKey privateKey = new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP).getEncryptionPrivateKeys().get(0);
        ResponseFactory responseFactory = new ResponseFactory(privateKey, privateKey);

        EntityDescriptor entityDescriptor = anEntityDescriptor()
            .withIdpSsoDescriptor(anIdpSsoDescriptor()
                .addKeyDescriptor(aKeyDescriptor()
                    .withX509ForSigning(TEST_RP_MS_PUBLIC_SIGNING_CERT)
                    .build())
                .build())
            .build();

        MetadataResolver msaMetadataResolver = mock(MetadataResolver.class);
        when(msaMetadataResolver.resolve(any())).thenReturn(ImmutableList.of(entityDescriptor));

        translator = responseFactory.createAssertionTranslator(msaMetadataResolver);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldTranslateValidAssertion(){
        TranslatedResponseBody result = translator.translate(ImmutableList.of(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).buildUnencrypted()
        ));
        assertThat(result).isEqualTo(new TranslatedResponseBody(
            SUCCESS_MATCH,
            "some-pid",
            LEVEL_2,
            null
        ));
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsIsEmptyList() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one assertion is expected.");

        translator.translate(Collections.emptyList());
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsIsNull() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one assertion is expected.");

        translator.translate(null);
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsListSizeIsLargerThanOne() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one assertion is expected.");

        translator.translate(
            ImmutableList.of(
                anAssertion().buildUnencrypted(),
                anAssertion().buildUnencrypted()
            )
        );
    }

    @Test
    public void shouldThrowExceptionWhenAssertionIsNotSigned() throws Exception {
        expectedException.expect(SamlTransformationErrorException.class);
        expectedException.expectMessage("SAML Validation Specification: Message signature is not signed");

        translator.translate(Collections.singletonList(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).withoutSigning().buildUnencrypted())
        );
    }

    @Test
    public void shouldThrowExceptionWhenAssertionSignedByUnknownKey() throws Exception {
        expectedException.expect(SamlTransformationErrorException.class);
        expectedException.expectMessage("SAML Validation Specification: Signature was not valid.");

        Credential unknownSigningCredential = new TestCredentialFactory(TEST_PUBLIC_CERT, TEST_PRIVATE_KEY).getSigningCredential();
        translator.translate(Collections.singletonList(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX)
                .withSignature(aSignature().withSigningCredential(unknownSigningCredential).build())
                .buildUnencrypted())
        );
    }

    @Test
    public void shouldThrowExceptionWhenSubjectIsMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject is missing from the assertion.");

        Assertion assertion = aSignedAssertion()
            .withSubject(null)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion));
    }

    @Test
    public void shouldThrowExceptionWhenNameIdIsMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("NameID is missing from the subject of the assertion.");

        Assertion assertion = aSignedAssertion()
            .withSubject(aSubject().withNameId(null).build())
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion));
    }

    @Test
    public void shouldThrowExceptionWhenAuthnStatementsIsEmpty(){
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one authn statement is expected.");

        Assertion assertion = aSignedAssertion()
            .buildUnencrypted();
        assertion.getAuthnStatements().clear();

        translator.translate(ImmutableList.of(assertion));
    }

    @Test
    public void shouldThrowExceptionWhenMultipleAuthnStatementsPresent(){
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one authn statement is expected.");

        Assertion assertion = aSignedAssertion()
            .addAuthnStatement(anAuthnStatement().build())
            .addAuthnStatement(anAuthnStatement().build())
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion));
    }

    @Test
    public void shouldThrowExceptionWhenLevelOfAssuranceNotPresent(){
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Expected a level of assurance.");

        AuthnStatement authnStatement = anAuthnStatement().withAuthnContext(
            anAuthnContext().withAuthnContextClassRef(null).build())
            .build();
        Assertion assertion = aSignedAssertion()
            .addAuthnStatement(authnStatement
            ).buildUnencrypted();

        translator.translate(ImmutableList.of(assertion));
    }

    @Test
    public void shouldThrowExceptionWithUnknownLevelOfAssurance() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Level of assurance 'unknown' is not supported.");

        Assertion assertion = aSignedAssertion()
            .addAuthnStatement(anAuthnStatement()
                .withAuthnContext(anAuthnContext()
                    .withAuthnContextClassRef(anAuthnContextClassRef()
                        .withAuthnContextClasRefValue("unknown")
                        .build())
                    .build())
                .build())
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion));
    }

    private AssertionBuilder aSignedAssertion() {
        return anAssertion()
            .withSignature(aSignature()
                .withSigningCredential(testRpMsaSigningCredential)
                .build());
    }

    private AssertionBuilder anAssertionWith(String pid, String levelOfAssurance) {
        return aSignedAssertion()
            .withSubject(aSubject().withPersistentId(pid).build())
            .addAuthnStatement(anAuthnStatement()
                .withAuthnContext(anAuthnContext()
                    .withAuthnContextClassRef(anAuthnContextClassRef()
                        .withAuthnContextClasRefValue(levelOfAssurance).build())
                    .build())
                .build());
    }
}