package uk.gov.ida.verifyserviceprovider.factories;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.MetadataBackedEncryptionCredentialResolver;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.healthcheck.MetadataHealthCheck;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseV2Resource;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseV1Resource;
import uk.gov.ida.verifyserviceprovider.resources.VersionNumberResource;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class VerifyServiceProviderFactory {

    private final VerifyServiceProviderConfiguration configuration;
    private final ResponseFactory responseFactory;

    private final DateTimeComparator dateTimeComparator;
    private final EntityIdService entityIdService;
    private final MetadataResolverBundle verifyMetadataBundler;
    private final MetadataResolverBundle msaMetadataBundle;
    private final ManifestReader manifestReader;

    public VerifyServiceProviderFactory(
            VerifyServiceProviderConfiguration configuration,
            MetadataResolverBundle verifyMetadataBundler,
            MetadataResolverBundle msaMetadataBundle) throws KeyException {
        this.configuration = configuration;
        this.responseFactory = new ResponseFactory(getDecryptionKeyPairs(configuration.getSamlPrimaryEncryptionKey(), configuration.getSamlSecondaryEncryptionKey()));
        this.dateTimeComparator = new DateTimeComparator(configuration.getClockSkew());
        this.entityIdService = new EntityIdService(configuration.getServiceEntityIds());
        this.verifyMetadataBundler = verifyMetadataBundler;
        this.msaMetadataBundle = msaMetadataBundle;
        this.manifestReader = new ManifestReader();
    }

    private List<KeyPair> getDecryptionKeyPairs(PrivateKey primary, PrivateKey secondary) throws KeyException {
        if (secondary == null) {
            return singletonList(createKeyPair(primary));
        } else {
            return asList(createKeyPair(primary), createKeyPair(secondary));
        }
    }

    private KeyPair createKeyPair(PrivateKey key) throws KeyException {
        return new KeyPair(KeySupport.derivePublicKey(key), key);
    }

    public MetadataHealthCheck getHubMetadataHealthCheck() {
        return new MetadataHealthCheck(
                getHubMetadataResolver(),
            configuration.getVerifyHubMetadata().getExpectedEntityId()
        );
    }

    public MetadataHealthCheck getMsaMetadataHealthCheck() {
        return new MetadataHealthCheck(
            getMsaMetadataResolver(),
            configuration.getMsaMetadata().getExpectedEntityId()
        );
    }

    public GenerateAuthnRequestResource getGenerateAuthnRequestResource() throws Exception {
        MetadataCredentialResolver metadataCredentialResolver = getHubMetadataCredentialResolver();
        MetadataBackedEncryptionCredentialResolver encryptionCredentialResolver = new MetadataBackedEncryptionCredentialResolver(metadataCredentialResolver, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        EncrypterFactory encrypterFactory = new EncrypterFactory(encryptionCredentialResolver, configuration.getVerifyHubMetadata().getExpectedEntityId());

        PrivateKey signingKey = configuration.getSamlSigningKey();

        AuthnRequestFactory authnRequestFactory = new AuthnRequestFactory(
                configuration.getHubSsoLocation(),
                createKeyPair(signingKey),
                manifestReader, encrypterFactory
        );

        return new GenerateAuthnRequestResource(
            authnRequestFactory,
            configuration.getHubSsoLocation(),
            entityIdService
        );
    }

    public TranslateSamlResponseV1Resource getTranslateMatchingSamlResponseResource() {
        return new TranslateSamlResponseV1Resource(
            responseFactory.createMatchingResponseService(
                getHubSignatureTrustEngine(),
                responseFactory.createAssertionServiceV1(getMsaSignatureTrustEngine(), dateTimeComparator),
                dateTimeComparator
            ),
            entityIdService
        );
    }

    public TranslateSamlResponseV2Resource getTranslateSamlResponseV2Resource() {
        return new TranslateSamlResponseV2Resource(
                responseFactory.createNonMatchingResponseService(
                        getHubSignatureTrustEngine(),
                        responseFactory.createAssertionServiceV2(
                                getHubSignatureTrustEngine(),
                                dateTimeComparator,
                                configuration.getHashingEntityId()),
                        dateTimeComparator
                ),
                entityIdService
        );
    }

    public VersionNumberResource getVersionNumberResource() {
        return new VersionNumberResource(manifestReader);
    }

    private MetadataResolver getHubMetadataResolver() {
        return verifyMetadataBundler.getMetadataResolver();
    }
    private ExplicitKeySignatureTrustEngine getHubSignatureTrustEngine() {
        return verifyMetadataBundler.getSignatureTrustEngine();
    }

    private MetadataCredentialResolver getHubMetadataCredentialResolver() {
        return verifyMetadataBundler.getMetadataCredentialResolver();
    }

    private MetadataResolver getMsaMetadataResolver() {
        return msaMetadataBundle.getMetadataResolver();
    }

    private ExplicitKeySignatureTrustEngine getMsaSignatureTrustEngine() {
        return msaMetadataBundle.getSignatureTrustEngine();
    }
}
