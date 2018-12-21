package uk.gov.ida.verifyserviceprovider.metadata;

import com.google.common.collect.ImmutableList;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.w3c.dom.Document;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.saml.core.api.CoreTransformersFactory;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.metadata.StringBackedMetadataResolver;
import uk.gov.ida.shared.utils.xml.XmlUtils;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.exceptions.EncryptionCertDoesNotMatchPrivateKeyException;
import uk.gov.ida.verifyserviceprovider.exceptions.SigningCertDoesNotMatchPrivateKeyException;

import java.security.PrivateKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.shared.utils.string.StringEncoding.toBase64Encoded;
import static uk.gov.ida.shared.utils.xml.XmlUtils.writeToString;

@RunWith(OpenSAMLMockitoRunner.class)
public class MetadataRepositoryTest {

    @Mock
    private VerifyServiceProviderConfiguration vspConfiguration;

    private X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();

    private MetadataRepository metadataRepository;

    private String entityId;

    @Before
    public void setUp() throws Base64DecodingException {
        entityId = "http://issuer";
        when(vspConfiguration.getServiceEntityIds()).thenReturn(ImmutableList.of(entityId));
        DeserializablePublicKeyConfiguration signingKeyConfiguration = mock(DeserializablePublicKeyConfiguration.class);
        when(signingKeyConfiguration.getCert()).thenReturn(TEST_RP_PUBLIC_SIGNING_CERT);
        when(signingKeyConfiguration.getPublicKey()).thenReturn(x509CertificateFactory.createCertificate(TEST_RP_PUBLIC_SIGNING_CERT).getPublicKey());
        when(vspConfiguration.getSamlPrimarySigningCert()).thenReturn(signingKeyConfiguration);
        final PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.decode(TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY));
        when(vspConfiguration.getSamlSigningKey()).thenReturn(privateSigningKey);
        DeserializablePublicKeyConfiguration encryptionKeyConfiguration = mock(DeserializablePublicKeyConfiguration.class);
        when(encryptionKeyConfiguration.getCert()).thenReturn(TEST_RP_PUBLIC_ENCRYPTION_CERT);
        when(encryptionKeyConfiguration.getPublicKey()).thenReturn(x509CertificateFactory.createCertificate(TEST_RP_PUBLIC_ENCRYPTION_CERT).getPublicKey());
        when(vspConfiguration.getSamlPrimaryEncryptionCert()).thenReturn(encryptionKeyConfiguration);
        final PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.decode(TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY));
        when(vspConfiguration.getSamlPrimaryEncryptionKey()).thenReturn(privateEncryptionKey);

        metadataRepository = new MetadataRepository(vspConfiguration);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test(expected = SigningCertDoesNotMatchPrivateKeyException.class)
    public void shouldThrowExceptionWhenSigningCertDoesNotMatchKey() throws Base64DecodingException {
        final PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.decode(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY));
        when(vspConfiguration.getSamlSigningKey()).thenReturn(privateSigningKey);
        new MetadataRepository(vspConfiguration);
    }

    @Test(expected = EncryptionCertDoesNotMatchPrivateKeyException.class)
    public void shouldThrowExceptionWhenEncryptionCertDoesNotMatchKey() throws Base64DecodingException {
        final PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.decode(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY));
        when(vspConfiguration.getSamlPrimaryEncryptionKey()).thenReturn(privateSigningKey);
        new MetadataRepository(vspConfiguration);
    }

    @Test
    public void shouldHaveAnSPSSODescriptor() {
        Document vspMetadata = metadataRepository.getVerifyServiceProviderMetadata();
        EntityDescriptor vspEntityDescriptor = getEntityDescriptor(vspMetadata, entityId);

        assertThat(vspEntityDescriptor.getRoleDescriptors().size()).isEqualTo(1);
        SPSSODescriptor spssoDescriptor = vspEntityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        assertThat(spssoDescriptor).isNotNull();

        assertThat(spssoDescriptor.getKeyDescriptors().stream().filter(kd -> kd.getUse().equals(UsageType.SIGNING)).count()).isEqualTo(1);
        assertThat(spssoDescriptor.getKeyDescriptors().stream().filter(kd -> kd.getUse().equals(UsageType.ENCRYPTION)).count()).isEqualTo(1);
    }

    @Test
    public void shouldHaveOneSigningKeyDescriptorWhenVspIsConfiguredWithNoSecondaryPublicSigningKey() throws Exception {

        Document verifyServiceProviderMetadata = metadataRepository.getVerifyServiceProviderMetadata();
        EntityDescriptor entityDescriptor = getEntityDescriptor(verifyServiceProviderMetadata, entityId);

        assertThat(entityDescriptor.getRoleDescriptors().size()).isEqualTo(1);
        assertThat(entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().stream().filter(kd -> kd.getUse().equals(UsageType.SIGNING)).count()).isEqualTo(1);
        assertThat(entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().stream().filter(kd -> kd.getUse().equals(UsageType.ENCRYPTION)).count()).isEqualTo(1);
    }

    @Test
    public void shouldHaveTwoSigningKeyDescriptorsWhenVspIsConfiguredWithSecondaryPublicSigningKey() throws Exception {
        // FIXME: this is using the test MSA cert instead of a secondary RP cert (but it's trust is chained the same way)
        DeserializablePublicKeyConfiguration secondarySigningKeyConfiguration = mock(DeserializablePublicKeyConfiguration.class);
        when(secondarySigningKeyConfiguration.getCert()).thenReturn(TEST_RP_MS_PUBLIC_SIGNING_CERT);
        when(vspConfiguration.getSamlSecondarySigningCert()).thenReturn(secondarySigningKeyConfiguration);

        Document verifyServiceProviderMetadata = metadataRepository.getVerifyServiceProviderMetadata();
        EntityDescriptor entityDescriptor = getEntityDescriptor(verifyServiceProviderMetadata, entityId);

        assertThat(entityDescriptor.getRoleDescriptors().size()).isEqualTo(1);
        assertThat(entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().stream().filter(kd -> kd.getUse().equals(UsageType.SIGNING)).count()).isEqualTo(2);
        assertThat(entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors().stream().filter(kd -> kd.getUse().equals(UsageType.ENCRYPTION)).count()).isEqualTo(1);
    }

    @Test
    public void shouldBeAbleToLoadVspMetadataUsingMetadataResolver() throws Exception {

        Document verifyServiceProviderMetadata = metadataRepository.getVerifyServiceProviderMetadata();
        String metadata = XmlUtils.writeToString(verifyServiceProviderMetadata);

        StringBackedMetadataResolver stringBackedMetadataResolver = new StringBackedMetadataResolver(metadata);
        BasicParserPool pool = new BasicParserPool();
        pool.initialize();
        stringBackedMetadataResolver.setParserPool(pool);
        stringBackedMetadataResolver.setId("Some ID");
        stringBackedMetadataResolver.initialize();

        assertThat(stringBackedMetadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId))).getEntityID()).isEqualTo(entityId);
    }

    @Test
    public void shouldGenerateMetadataValidFor1Hour() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(DateTime.now().getMillis());

        Document verifyServiceProviderMetadata = metadataRepository.getVerifyServiceProviderMetadata();
        EntitiesDescriptor entitiesDescriptor = getEntitiesDescriptor(verifyServiceProviderMetadata);

        assertThat(entitiesDescriptor.getValidUntil()).isEqualTo(DateTime.now(DateTimeZone.UTC).plusHours(1));
    }

    @Test
    public void shouldReturnSignedMetadata() throws Exception {
        Document verifyServiceProviderMetadata = metadataRepository.getVerifyServiceProviderMetadata();
        EntitiesDescriptor descriptor = getEntitiesDescriptor(verifyServiceProviderMetadata);

        assertThat(descriptor.getSignature()).isNotNull();
    }

    private EntityDescriptor getEntityDescriptor(Document matchingServiceAdapterMetadata, String entityId) {
        EntitiesDescriptor entitiesDescriptor = getEntitiesDescriptor(matchingServiceAdapterMetadata);

        EntityDescriptor matchingEntityDescriptor = null;
        for (EntityDescriptor entityDescriptor : entitiesDescriptor.getEntityDescriptors()) {
            if (entityDescriptor.getEntityID().equals(entityId)) {
                matchingEntityDescriptor = entityDescriptor;
            }
        }
        return matchingEntityDescriptor;
    }

    private EntitiesDescriptor getEntitiesDescriptor(Document matchingServiceAdapterMetadata) {
        StringToOpenSamlObjectTransformer<XMLObject> stringtoOpenSamlObjectTransformer = new CoreTransformersFactory().getStringtoOpenSamlObjectTransformer(input -> {});

        return (EntitiesDescriptor) stringtoOpenSamlObjectTransformer.apply(toBase64Encoded(writeToString(matchingServiceAdapterMetadata)));
    }
}
