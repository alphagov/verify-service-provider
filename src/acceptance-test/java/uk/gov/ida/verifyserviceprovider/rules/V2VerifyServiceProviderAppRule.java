package uk.gov.ida.verifyserviceprovider.rules;

import certificates.values.CACertificates;
import com.nimbusds.jose.JOSEException;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.joda.time.DateTime;
import org.junit.After;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.Constants;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.eidas.trustanchor.Generator;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.IdpSsoDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.core.MediaType;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.ida.common.shared.security.Certificate.BEGIN_CERT;
import static uk.gov.ida.common.shared.security.Certificate.END_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP;
import static uk.gov.ida.saml.metadata.ResourceEncoder.entityIdAsResource;

public class V2VerifyServiceProviderAppRule extends DropwizardAppRule<VerifyServiceProviderConfiguration> {

    private static final String VERIFY_METADATA_PATH = "/verify-metadata";
    private static final String TRUST_ANCHOR_PATH = "/trust-anchor";
    private static final String METADATA_AGGREGATOR_PATH = "/metadata-aggregator";
    private static final String COUNTRY_METADATA_PATH = "/test-country";
    private static final String METADATA_SOURCE_PATH = "/metadata-source";

    private static final HttpStubRule metadataAggregatorServer = new HttpStubRule();
    private static final HttpStubRule trustAnchorServer = new HttpStubRule();
    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    private static final MockMsaServer msaServer = new MockMsaServer();
    private String countryEntityId;

    private static final KeyStoreResource countryMetadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    public V2VerifyServiceProviderAppRule() {
        super(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            ConfigOverride.config("serviceEntityIds", TEST_RP),
            ConfigOverride.config("hashingEntityId", "some-hashing-entity-id"),
            ConfigOverride.config("server.connector.port", String.valueOf(0)),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            ConfigOverride.config("verifyHubConfiguration.metadata.uri", () -> "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
            ConfigOverride.config("verifyHubConfiguration.metadata.trustStore.path", metadataTrustStore.getAbsolutePath()),
            ConfigOverride.config("verifyHubConfiguration.metadata.trustStore.password", metadataTrustStore.getPassword()),
            ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("msaMetadata.uri", msaServer::getUri),
            ConfigOverride.config("msaMetadata.expectedEntityId", MockMsaServer.MSA_ENTITY_ID),
            ConfigOverride.config("europeanIdentity.hubConnectorEntityId", HUB_CONNECTOR_ENTITY_ID),
            ConfigOverride.config("europeanIdentity.enabled", "true"),
            ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustAnchorUri", "http://localhost:" + trustAnchorServer.getPort() + TRUST_ANCHOR_PATH),
            ConfigOverride.config("europeanIdentity.aggregatedMetadata.metadataSourceUri", "http://localhost:" + metadataAggregatorServer.getPort() + METADATA_SOURCE_PATH),
            ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustStore.store", countryMetadataTrustStore.getAbsolutePath()),
            ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustStore.trustStorePassword", countryMetadataTrustStore.getPassword())
        );
    }

    @Override
    protected void before() {
        countryMetadataTrustStore.create();
        metadataTrustStore.create();

        countryEntityId = "https://localhost:12345" + METADATA_AGGREGATOR_PATH + COUNTRY_METADATA_PATH;

        try {
            InitializationService.initialize();
            String testCountryMetadata = new MetadataFactory().singleEntityMetadata(buildTestCountryEntityDescriptor());

            msaServer.start();
            msaServer.serveDefaultMetadata();

            verifyMetadataServer.reset();
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());

            trustAnchorServer.reset();
            trustAnchorServer.register(TRUST_ANCHOR_PATH, 200, MediaType.APPLICATION_OCTET_STREAM, buildTrustAnchorString());

            metadataAggregatorServer.reset();
            metadataAggregatorServer.register(METADATA_SOURCE_PATH + "/" + entityIdAsResource(countryEntityId), 200, Constants.APPLICATION_SAMLMETADATA_XML, testCountryMetadata);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.before();
    }

    private String buildTrustAnchorString() throws JOSEException, CertificateEncodingException {
        X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
        PrivateKey trustAnchorKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(METADATA_SIGNING_A_PRIVATE_KEY));
        X509Certificate trustAnchorCert = x509CertificateFactory.createCertificate(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);
        Generator generator = new Generator(trustAnchorKey, trustAnchorCert);
        HashMap<String, List<X509Certificate>> trustAnchorMap = new HashMap<>();
        X509Certificate metadataCACert = x509CertificateFactory.createCertificate(CACertificates.TEST_METADATA_CA.replace(BEGIN_CERT, "").replace(END_CERT, "").replace("\n", ""));
        trustAnchorMap.put(countryEntityId, singletonList(metadataCACert));
        return generator.generateFromMap(trustAnchorMap).serialize();
    }

    private EntityDescriptor buildTestCountryEntityDescriptor() throws Exception {
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

        return EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(countryEntityId)
                .withIdpSsoDescriptor(idpSsoDescriptor)
                .setAddDefaultSpServiceDescriptor(false)
                .withValidUntil(DateTime.now().plusWeeks(2))
                .withSignature(signature)
                .build();
    }

    public String getCountryEntityId() {
        return countryEntityId;
    }
}
