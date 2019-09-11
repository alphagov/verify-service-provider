package common.uk.gov.ida.verifyserviceprovider.servers;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.IdpSsoDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.SignatureBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.metadata.ResourceEncoder.entityIdAsResource;

public class MockMetadataAggregatorServer extends WireMockClassRule {

    public static final String METADATA_SOURCE_PATH = "/metadata-source";

    public MockMetadataAggregatorServer() {
        super(wireMockConfig().dynamicPort());
    }

    public void serveAggregatedMetadata(String entityId) throws Exception {
        stubFor(
            get(urlEqualTo(METADATA_SOURCE_PATH + "/" + entityIdAsResource(entityId)))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(buildTestCountryEntityDescriptor(entityId))
                )
        );
    }

    public String getUri() {
        return "http://localhost:" + port() + METADATA_SOURCE_PATH;
    }

    private String buildTestCountryEntityDescriptor(String countryEntityId) throws Exception {
        KeyDescriptor signingKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
            .withX509ForSigning(STUB_COUNTRY_PUBLIC_PRIMARY_CERT)
            .build();

        IDPSSODescriptor idpSsoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
            .withoutDefaultSigningKey()
            .addKeyDescriptor(signingKeyDescriptor)
            .build();

        Signature signature = SignatureBuilder.aSignature()
            .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
            .withX509Data(METADATA_SIGNING_A_PUBLIC_CERT)
            .build();

        EntityDescriptor entityDescriptor = EntityDescriptorBuilder.anEntityDescriptor()
            .withEntityId(countryEntityId)
            .withIdpSsoDescriptor(idpSsoDescriptor)
            .setAddDefaultSpServiceDescriptor(false)
            .withValidUntil(DateTime.now().plusWeeks(2))
            .withSignature(signature)
            .build();

        String s = new MetadataFactory().singleEntityMetadata(entityDescriptor);
        return s;
    }
}
