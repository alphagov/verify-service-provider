package unit.uk.gov.ida.verifyserviceprovider.factories.saml;

import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAttribute;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.crypto.KeySupport;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.PrivateKeyStore;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.extensions.versioning.Version;
import uk.gov.ida.saml.core.test.PrivateKeyStoreFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.factories.EncrypterFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT;

public class AuthnRequestFactoryTest {

    private static final URI DESTINATION = URI.create("http://example.com");
    private static final String SERVICE_ENTITY_ID = "http://entity-id";
    private static final ManifestReader manifestReader = mock(ManifestReader.class);
    private static final EncrypterFactory encrypterFactory = mock(EncrypterFactory.class);
    private static Encrypter encrypter;
    private static Decrypter decrypter;
    private static AuthnRequestFactory factory;

    @Before
    public void setUp() throws KeyException {
        IdaSamlBootstrap.bootstrap();
        reset(manifestReader);

        final BasicCredential basicCredential = createBasicCredential();
        encrypter = new uk.gov.ida.saml.security.EncrypterFactory().createEncrypter(basicCredential);
        decrypter = new DecrypterFactory().createDecrypter(ImmutableList.of(basicCredential));
        when(encrypterFactory.createEncrypter()).thenReturn(encrypter);
        PrivateKeyStore privateKeyStore = new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP);
        KeyPair keyPair = new KeyPair(KeySupport.derivePublicKey(privateKeyStore.getSigningPrivateKey()), privateKeyStore.getSigningPrivateKey());
        factory = new AuthnRequestFactory(
            DESTINATION,
            keyPair,
            manifestReader,
            encrypterFactory
        );
    }

    @Test
    public void containsCorrectAttributes() throws KeyException {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);

        assertThat(authnRequest.getID()).isNotEmpty();
        assertThat(authnRequest.getIssueInstant()).isNotNull();
        assertThat(authnRequest.getDestination()).isNotEmpty();
        assertThat(authnRequest.getIssuer()).isNotNull();
        assertThat(authnRequest.getSignature()).isNotNull();
    }

    @Test
    public void shouldNotForceAuthn() {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);
        assertThat(authnRequest.isForceAuthn()).isFalse();
    }

    @Test
    public void signatureIDReferencesAuthnRequestID() {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);
        assertThat(authnRequest.getSignatureReferenceID()).isEqualTo(authnRequest.getID());
    }

    @Test
    public void destinationShouldMatchConfiguredSSOLocation() {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);
        assertThat(authnRequest.getDestination()).isEqualTo(DESTINATION.toString());
    }

    @Test
    public void issuerShouldMatchConfiguredEntityID() {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);
        assertThat(authnRequest.getIssuer().getValue()).isEqualTo(SERVICE_ENTITY_ID);
    }

    @Test
    public void shouldAddApplicationVersionInExtension() throws Exception {
        when(manifestReader.getAttributeValueFor(VerifyServiceProviderApplication.class, "Version")).thenReturn("some-version");

        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);

        Extensions extensions = authnRequest.getExtensions();
        EncryptedAttribute encryptedAttribute = (EncryptedAttribute) extensions.getUnknownXMLObjects().get(0);

        Attribute attribute = decrypter.decrypt(encryptedAttribute);
        Version version = (Version) attribute.getAttributeValues().get(0);

        assertThat(attribute.getName()).isEqualTo("Versions");
        assertThat(version.getApplicationVersion().getValue()).isEqualTo("some-version");
    }

    @Test
    public void shouldGetVersionNumberFromManifestReader() throws IOException, KeyException {
        factory.build(SERVICE_ENTITY_ID);

        verify(manifestReader, times(1)).getAttributeValueFor(VerifyServiceProviderApplication.class, "Version");
    }

    private BasicCredential createBasicCredential() {
        final PublicKey publicKey = new PublicKeyFactory(new X509CertificateFactory()).createPublicKey(HUB_TEST_PUBLIC_ENCRYPTION_CERT);
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        return new BasicCredential(publicKey, privateKey);
    }
}