package uk.gov.ida.verifyserviceprovider.saml;

import common.uk.gov.ida.verifyserviceprovider.utils.CertAndKeys;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthnRequestFactoryTest {

    private static final URI DESTINATION = URI.create("http://example.com");
    private static final String SERVICE_ENTITY_ID = "http://entity-id";
    private static final CertAndKeys SIGNING_KEY = CertAndKeys.generate();

    private static AuthnRequestFactory factory = new AuthnRequestFactory(DESTINATION, SERVICE_ENTITY_ID, SIGNING_KEY.privateKey);


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