package common.uk.gov.ida.verifyserviceprovider.servers;

import certificates.values.CACertificates;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.nimbusds.jose.JOSEException;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.eidas.trustanchor.Generator;
import uk.gov.ida.saml.core.test.TestCertificateStrings;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Collections.singletonList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;

public class MockTrustAnchorServer extends WireMockClassRule {

    public static final String TRUST_ANCHOR = "/trust-anchor";

    private String buildTrustAnchorString(String countryEntityId) throws JOSEException, CertificateEncodingException {
        X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
        PrivateKey trustAnchorKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(METADATA_SIGNING_A_PRIVATE_KEY));
        X509Certificate trustAnchorCert = x509CertificateFactory.createCertificate(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);
        Generator generator = new Generator(trustAnchorKey, trustAnchorCert);
        HashMap<String, List<X509Certificate>> trustAnchorMap = new HashMap<>();
        String encoded = CACertificates.TEST_METADATA_CA;
        X509Certificate metadataCACert = x509CertificateFactory.createCertificate(encoded);
        trustAnchorMap.put(countryEntityId, singletonList(metadataCACert));
        return generator.generateFromMap(trustAnchorMap).serialize();
    }

    public MockTrustAnchorServer() {
        super(wireMockConfig().dynamicPort());
    }

    public void serveTrustAnchor(String entityId) throws CertificateEncodingException, JOSEException {
        stubFor(
            get(urlEqualTo(TRUST_ANCHOR))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(buildTrustAnchorString(entityId))
                )
        );
    }

    public String getUri() {
        return String.format("http://localhost:%s/%s", this.port(), TRUST_ANCHOR);
    }
}
