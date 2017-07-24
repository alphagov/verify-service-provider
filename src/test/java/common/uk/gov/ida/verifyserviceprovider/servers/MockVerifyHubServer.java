package common.uk.gov.ida.verifyserviceprovider.servers;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class MockVerifyHubServer extends WireMockClassRule {

    public MockVerifyHubServer(){
        super(wireMockConfig().dynamicPort());
    }

    public void serveDefaultMetadata() {
        stubFor(
            get(urlEqualTo("/SAML2/metadata"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(new MetadataFactory().defaultMetadata())
                )
        );
    }
}
