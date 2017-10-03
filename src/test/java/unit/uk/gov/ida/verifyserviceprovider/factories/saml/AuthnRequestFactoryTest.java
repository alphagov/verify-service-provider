package unit.uk.gov.ida.verifyserviceprovider.factories.saml;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.PrivateKeyStoreFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.saml.extensions.Bootstrap;
import uk.gov.ida.verifyserviceprovider.saml.extensions.Version;
import uk.gov.ida.verifyserviceprovider.utils.ManifestReader;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthnRequestFactoryTest {

    private static final URI DESTINATION = URI.create("http://example.com");
    private static final String SERVICE_ENTITY_ID = "http://entity-id";
    private static final ManifestReader manifestReader = mock(ManifestReader.class);

    private static AuthnRequestFactory factory = new AuthnRequestFactory(
        DESTINATION,
        new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP).getSigningPrivateKey(),
            manifestReader);


    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
        reset(manifestReader);
    }

    @Test
    public void containsCorrectAttributes() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2, SERVICE_ENTITY_ID);

        assertThat(authnRequest.getID()).isNotEmpty();
        assertThat(authnRequest.getIssueInstant()).isNotNull();
        assertThat(authnRequest.getDestination()).isNotEmpty();
        assertThat(authnRequest.getIssuer()).isNotNull();
        assertThat(authnRequest.getSignature()).isNotNull();
    }

    @Test
    public void shouldNotForceAuthn() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2, SERVICE_ENTITY_ID);
        assertThat(authnRequest.isForceAuthn()).isFalse();
    }

    @Test
    public void signatureIDReferencesAuthnRequestID() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2, SERVICE_ENTITY_ID);
        assertThat(authnRequest.getSignatureReferenceID()).isEqualTo(authnRequest.getID());
    }

    @Test
    public void destinationShouldMatchConfiguredSSOLocation() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2, SERVICE_ENTITY_ID);
        assertThat(authnRequest.getDestination()).isEqualTo(DESTINATION.toString());
    }

    @Test
    public void issuerShouldMatchConfiguredEntityID() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2, SERVICE_ENTITY_ID);
        assertThat(authnRequest.getIssuer().getValue()).isEqualTo(SERVICE_ENTITY_ID);
    }

    @Test
    public void shouldAddExtensionToRequest() throws InitializationException {
        Bootstrap.bootstrap();

        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2, SERVICE_ENTITY_ID);

        Extensions extensions = authnRequest.getExtensions();
        assertThat(extensions).isNotNull();
        assertThat(extensions.getUnknownXMLObjects()).hasSize(1);
        Attribute attribute = (Attribute) extensions.getUnknownXMLObjects().get(0);
        assertThat(attribute.getAttributeValues().get(0)).isInstanceOf(Version.class);
    }

    @Test
    public void shouldAddApplicationVersionInExtension() throws InitializationException {
        Bootstrap.bootstrap();
        String versionNumber = "0.3.0";
        when(manifestReader.getVersion()).thenReturn(versionNumber);

        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2, SERVICE_ENTITY_ID);

        Extensions extensions = authnRequest.getExtensions();
        Attribute attribute = (Attribute) extensions.getUnknownXMLObjects().get(0);
        Version version = (Version) attribute.getAttributeValues().get(0);
        assertThat(version.getApplicationVersion().getValue()).isEqualTo(versionNumber);
    }

    @Test
    public void shouldGetVersionNumberFromManifestReader() throws InitializationException {
        Bootstrap.bootstrap();
        when(manifestReader.getVersion()).thenReturn("0.3.0");

        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2, SERVICE_ENTITY_ID);

        Extensions extensions = authnRequest.getExtensions();
        Attribute attribute = (Attribute) extensions.getUnknownXMLObjects().get(0);
        Version version = (Version) attribute.getAttributeValues().get(0);
        verify(manifestReader, times(1)).getVersion();
        assertThat(version.getApplicationVersion().getValue()).isNotEmpty();
    }
}