package common.uk.gov.ida.verifyserviceprovider.servers;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.metadata.test.factories.metadata.EntityDescriptorFactory;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP_MS;
import static uk.gov.ida.saml.core.test.builders.metadata.EntitiesDescriptorBuilder.anEntitiesDescriptor;

public class  MockMsaServer extends WireMockClassRule {

    public static final String MSA_ENTITY_ID = TEST_RP_MS;

    public static String msaMetadata() {
        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().idpEntityDescriptor(MSA_ENTITY_ID);
        try {
            return new MetadataFactory().metadata(anEntitiesDescriptor()
                    .withEntityDescriptors(ImmutableList.of(entityDescriptor))
                    .withValidUntil(DateTime.now().plusWeeks(2)).build());
        } catch (MarshallingException | SignatureException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }

    }

    public MockMsaServer() {
        super(wireMockConfig().dynamicPort());
    }

    public void serveDefaultMetadata() {
        stubFor(
            get(urlEqualTo("/matching-service/metadata"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(msaMetadata())
                )
        );
    }

    public String getUri() {
        return String.format("http://localhost:%s/matching-service/metadata", this.port());
    }
}
