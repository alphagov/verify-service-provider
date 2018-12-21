package feature.uk.gov.ida.verifyserviceprovider.configuration.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import httpstub.HttpStubRule;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.AttributeAuthorityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.EntitiesDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyInfoBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.X509CertificateBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.X509DataBuilder;
import uk.gov.ida.shared.utils.xml.XmlUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;

public class MSAStubRule extends HttpStubRule {

    private static final Logger LOG = Logger.getLogger(MSAStubRule.class);

    private final String METADATA_FULL_PATH = "/msa/metadata";
    public final String METADATA_ENTITY_ID;

    public MSAStubRule() {
        super();
        METADATA_ENTITY_ID = "http://localhost:"+getPort()+METADATA_FULL_PATH;
    }

    public void setUpRegularMetadata() throws JsonProcessingException, MarshallingException, SignatureException {
        setUpRegularMetadata(generateMsaMetadata(ImmutableList.of(TEST_RP_MS_PUBLIC_SIGNING_CERT), Optional.ofNullable(TEST_RP_MS_PUBLIC_ENCRYPTION_CERT)));
    }

    public void setUpRegularMetadata(String metadata) throws JsonProcessingException {
        LOG.info(format("using metadata: {0}", metadata));
        String uri = UriBuilder.fromPath(METADATA_FULL_PATH).build().getPath();
        register(uri, Response.Status.OK.getStatusCode(), "application/samlmetadata+xml", metadata);
    }

    public void setUpMissingMetadata() throws JsonProcessingException {
        String uri = UriBuilder.fromPath(METADATA_FULL_PATH).build().getPath();
        register(uri, Response.Status.FORBIDDEN.getStatusCode(), "");
    }

    private String generateMsaMetadata(List<String> signingCerts, Optional<String> encCert) throws MarshallingException, SignatureException {
        return generateMsaMetadata(signingCerts, encCert, TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY);
    }

    private String generateMsaMetadata(List<String> signingCerts, Optional<String> encCert, String metadataSigningCert, String metadataSigningKey) throws MarshallingException, SignatureException {
        final EntityDescriptor entityDescriptor = attributeAuthorityEntityDescriptor(METADATA_ENTITY_ID, signingCerts, encCert);
        EntitiesDescriptor entitiesDescriptor = EntitiesDescriptorBuilder.anEntitiesDescriptor()
                .withEntityDescriptors(ImmutableList.of(entityDescriptor))
                .withValidUntil(DateTime.now().plusWeeks(2))
                .withName("")
                .build();
        sign(entitiesDescriptor, metadataSigningCert, metadataSigningKey);
        return XmlUtils.writeToString(entitiesDescriptor.getDOM());
    }

    private void sign(EntitiesDescriptor entitiesDescriptor, String signingCert, String signingKey) {
        entitiesDescriptor.setSignature(SignatureBuilder.aSignature()
                .withSigningCredential(new TestCredentialFactory(signingCert, signingKey).getSigningCredential())
                .withX509Data(signingCert)
                .build());
        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(entitiesDescriptor).marshall(entitiesDescriptor);
            Signer.signObject(entitiesDescriptor.getSignature());
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public EntityDescriptor attributeAuthorityEntityDescriptor(String entityId, List<String> publicSigningCerts, Optional<String> publicEncCert) {
        List<KeyDescriptor> keyDescriptors = buildKeyDescriptors(publicSigningCerts, publicEncCert);
        AttributeAuthorityDescriptorBuilder attributeAuthorityDescriptorBuilder = AttributeAuthorityDescriptorBuilder.anAttributeAuthorityDescriptor();
        keyDescriptors.stream().forEach(keyDescriptor -> attributeAuthorityDescriptorBuilder.addKeyDescriptor(keyDescriptor));
        final AttributeAuthorityDescriptor attributeAuthorityDescriptor = attributeAuthorityDescriptorBuilder.build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(entityId)
                    .withIdpSsoDescriptor(null)
                    .withValidUntil(DateTime.now().plusWeeks(2))
                    .withSignature(null)
                    .withoutSigning()
                    .withAttributeAuthorityDescriptor(attributeAuthorityDescriptor)
                    .setAddDefaultSpServiceDescriptor(false)
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw Throwables.propagate(e);
        }
    }

    private List<KeyDescriptor> buildKeyDescriptors(List<String> signingCerts, Optional<String> encCert) {
        List<KeyDescriptor> keyDescriptors = new ArrayList<>();
        signingCerts.forEach(signingCert -> {
            X509Certificate x509SigningCert = X509CertificateBuilder.aX509Certificate().withCert(signingCert).build();
            X509Data signing = X509DataBuilder.aX509Data().withX509Certificate(x509SigningCert).build();
            KeyInfo signing_one = KeyInfoBuilder.aKeyInfo().withKeyName("signing_"+UUID.randomUUID().toString()).withX509Data(signing).build();
            keyDescriptors.add(KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(signing_one).withUse("SIGNING").build());
        });
        if(encCert.isPresent()) {
            X509Certificate x509EncCert = X509CertificateBuilder.aX509Certificate().withCert(encCert.get()).build();
            X509Data encryption = X509DataBuilder.aX509Data().withX509Certificate(x509EncCert).build();
            KeyInfo encryption_one = KeyInfoBuilder.aKeyInfo().withKeyName("encryption_one").withX509Data(encryption).build();
            keyDescriptors.add(KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(encryption_one).withUse("ENCRYPTION").build());
        }
        return keyDescriptors;
    }

}
