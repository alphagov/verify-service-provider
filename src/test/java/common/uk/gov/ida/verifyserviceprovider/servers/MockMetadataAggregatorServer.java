package common.uk.gov.ida.verifyserviceprovider.servers;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.IdpSsoDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.SPSSODescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.SignatureBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.saml.metadata.ResourceEncoder.entityIdAsResource;

public class MockMetadataAggregatorServer extends WireMockClassRule {

    public static final String METADATA_SOURCE_PATH = "/metadata-source";

    public MockMetadataAggregatorServer() {
        super(wireMockConfig().dynamicPort());
    }

    public void serveAggregatedStubCountryMetadata() throws Exception {
        stubFor(
            get(urlEqualTo(METADATA_SOURCE_PATH + "/" + entityIdAsResource(STUB_COUNTRY_ONE)))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(buildEntityDescriptor(STUB_COUNTRY_ONE, STUB_COUNTRY_PUBLIC_PRIMARY_CERT, STUB_COUNTRY_PUBLIC_PRIMARY_CERT))
                )
        );
    }

    public void serveAggregatedHubMetadata() throws Exception {
        stubFor(
            get(urlEqualTo(METADATA_SOURCE_PATH + "/" + entityIdAsResource(HUB_ENTITY_ID)))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(buildEntityDescriptor(HUB_ENTITY_ID, HUB_TEST_PUBLIC_SIGNING_CERT, STUB_IDP_PUBLIC_PRIMARY_CERT))
                )
        );
    }

    public String getUri() {
        return "http://localhost:" + port() + METADATA_SOURCE_PATH;
    }

    private String buildEntityDescriptor(String samlSigningEntityId, String samlSigningCert, String assertionSigningCert) throws Exception {

        KeyDescriptor countrySigningKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
            .withX509ForSigning(samlSigningCert)
            .build();

        KeyDescriptor idpSigningKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
            .withX509ForSigning(assertionSigningCert)
            .build();

        SPSSODescriptor spSsoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
            .withoutDefaultSigningKey()
            .addKeyDescriptor(countrySigningKeyDescriptor)
            .build();

        IDPSSODescriptor idpSsoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
            .withoutDefaultSigningKey()
            .addKeyDescriptor(idpSigningKeyDescriptor)
            .build();

        Signature signature = SignatureBuilder.aSignature()
            .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
            .withX509Data(METADATA_SIGNING_A_PUBLIC_CERT)
            .build();

        EntityDescriptor entityDescriptor = EntityDescriptorBuilder.anEntityDescriptor()
            .withEntityId(samlSigningEntityId)
            .addSpServiceDescriptor(spSsoDescriptor)
            .withIdpSsoDescriptor(idpSsoDescriptor)
            .setAddDefaultSpServiceDescriptor(false)
            .withValidUntil(DateTime.now().plusWeeks(2))
            .withSignature(signature)
            .build();

        String s = new MetadataFactory().singleEntityMetadata(entityDescriptor);
        return s;
    }
}
