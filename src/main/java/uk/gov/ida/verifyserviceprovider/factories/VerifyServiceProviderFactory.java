package uk.gov.ida.verifyserviceprovider.factories;

import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.saml.metadata.EidasTrustAnchorResolver;
import uk.gov.ida.saml.metadata.MetadataResolverConfigBuilder;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory;
import uk.gov.ida.saml.metadata.factories.MetadataSignatureTrustEngineFactory;
import uk.gov.ida.saml.security.MetadataBackedEncryptionCredentialResolver;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.verifyserviceprovider.configuration.EuropeanIdentityConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.resources.VersionNumberResource;
import uk.gov.ida.verifyserviceprovider.services.*;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import javax.ws.rs.client.Client;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class VerifyServiceProviderFactory {

    private final VerifyServiceProviderConfiguration configuration;
    private final ResponseFactory responseFactory;

    private final DateTimeComparator dateTimeComparator;
    private final EntityIdService entityIdService;
    private final MetadataResolverBundle<VerifyServiceProviderConfiguration> verifyMetadataBundler;
    private final MetadataResolverBundle<VerifyServiceProviderConfiguration> msaMetadataBundle;
    private final ManifestReader manifestReader;
    private final Client client;

    public VerifyServiceProviderFactory(
            VerifyServiceProviderConfiguration configuration,
            MetadataResolverBundle<VerifyServiceProviderConfiguration> verifyMetadataBundler,
            MetadataResolverBundle<VerifyServiceProviderConfiguration> msaMetadataBundle,
            Client client) throws KeyException {
        this.configuration = configuration;
        this.responseFactory = new ResponseFactory(getDecryptionKeyPairs(configuration.getSamlPrimaryEncryptionKey(), configuration.getSamlSecondaryEncryptionKey()));
        this.dateTimeComparator = new DateTimeComparator(configuration.getClockSkew());
        this.entityIdService = new EntityIdService(configuration.getServiceEntityIds());
        this.verifyMetadataBundler = verifyMetadataBundler;
        this.msaMetadataBundle = msaMetadataBundle;
        this.manifestReader = new ManifestReader();
        this.client = client;
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

    public TranslateSamlResponseResource getTranslateSamlResponseResource() {
        if(configuration.getMsaMetadata().isPresent()) {
            return getTranslateMatchingSamlResponseResource();
        }
        if (isEidasEnabled()){
            return getTranslateNonMatchingSamlResponseResource(getEidasAssertionService());
        } else {
            return getTranslateNonMatchingSamlResponseResource(getIdpAssertionService());
        }
    }

    private TranslateSamlResponseResource<TranslatedMatchingResponseBody> getTranslateMatchingSamlResponseResource() {
        ResponseService<TranslatedMatchingResponseBody> matchingResponseService = responseFactory.createMatchingResponseService(
                getHubSignatureTrustEngine(),
                responseFactory.createMsaAssertionService(getMsaSignatureTrustEngine(), new SignatureValidatorFactory(), dateTimeComparator),
                dateTimeComparator
        );
        return new TranslateSamlResponseResource<>(matchingResponseService, entityIdService);
    }

    private TranslateSamlResponseResource<TranslatedNonMatchingResponseBody> getTranslateNonMatchingSamlResponseResource(AssertionServiceV2 assertionService) {

        return new TranslateSamlResponseResource<>(
                responseFactory.createNonMatchingResponseService(
                        getHubSignatureTrustEngine(),
                        assertionService,
                        dateTimeComparator
                ),
                entityIdService);
    }

    private EidasAssertionService getEidasAssertionService() {
        return responseFactory.createEidasAssertionService(
                    isEidasEnabled(),
                    dateTimeComparator,
                    getEidasMetadataResolverRepository()
            );
    }

    private IdpAssertionService getIdpAssertionService() {
        return responseFactory.createIdpAssertionService(
                    getHubSignatureTrustEngine(),
                    new SignatureValidatorFactory(),
                    dateTimeComparator,
                    configuration.getHashingEntityId()
            );
    }

    public VersionNumberResource getVersionNumberResource() {
        return new VersionNumberResource(manifestReader);
    }

    private ExplicitKeySignatureTrustEngine getHubSignatureTrustEngine() {
        return verifyMetadataBundler.getSignatureTrustEngine();
    }

    private MetadataCredentialResolver getHubMetadataCredentialResolver() {
        return verifyMetadataBundler.getMetadataCredentialResolver();
    }

    private ExplicitKeySignatureTrustEngine getMsaSignatureTrustEngine() {
        return msaMetadataBundle.getSignatureTrustEngine();
    }

    private Optional<EidasMetadataResolverRepository> getEidasMetadataResolverRepository() {
            return Optional.of(new EidasMetadataResolverRepository(
                getEidasTrustAnchorResolver(),
                configuration.getEuropeanIdentity().get().getAggregatedMetadata(),
                new DropwizardMetadataResolverFactory(),
                new Timer(),
                new MetadataSignatureTrustEngineFactory(),
                new MetadataResolverConfigBuilder(),
                client
            ));
    }

    private EidasTrustAnchorResolver getEidasTrustAnchorResolver() {
        EidasMetadataConfiguration metadataConfiguration = configuration.getEuropeanIdentity().get().getAggregatedMetadata();
        return new EidasTrustAnchorResolver(metadataConfiguration.getTrustAnchorUri(), client, metadataConfiguration.getTrustStore());
    }

    private boolean isEidasEnabled() {
        Optional<EuropeanIdentityConfiguration> eidasConfig = configuration.getEuropeanIdentity();
        return eidasConfig.map(EuropeanIdentityConfiguration::isEnabled).orElse(false);
    }
}
