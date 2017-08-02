package unit.uk.gov.ida.verifyserviceprovider.factories.saml;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.PrivateKeyStoreFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthnRequestFactoryTest {

    private static final URI DESTINATION = URI.create("http://example.com");
    private static final String SERVICE_ENTITY_ID = "http://entity-id";

    private static AuthnRequestFactory factory = new AuthnRequestFactory(
        DESTINATION,
        SERVICE_ENTITY_ID,
        new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP).getSigningPrivateKey()
    );


    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void containsCorrectAttributes() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2);

        assertThat(authnRequest.getID()).isNotEmpty();
        assertThat(authnRequest.getIssueInstant()).isNotNull();
        assertThat(authnRequest.getDestination()).isNotEmpty();
        assertThat(authnRequest.getIssuer()).isNotNull();
        assertThat(authnRequest.getSignature()).isNotNull();
    }

    @Test
    public void shouldNotForceAuthn() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2);
        assertThat(authnRequest.isForceAuthn()).isFalse();
    }

    @Test
    public void signatureIDReferencesAuthnRequestID() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2);
        assertThat(authnRequest.getSignatureReferenceID()).isEqualTo(authnRequest.getID());
    }

    @Test
    public void destinationShouldMatchConfiguredSSOLocation() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2);
        assertThat(authnRequest.getDestination()).isEqualTo(DESTINATION.toString());
    }

    @Test
    public void issuerShouldMatchConfiguredEntityID() {
        AuthnRequest authnRequest = factory.build(LevelOfAssurance.LEVEL_2);
        assertThat(authnRequest.getIssuer().getValue()).isEqualTo(SERVICE_ENTITY_ID);
    }
}