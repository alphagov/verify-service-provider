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
import uk.gov.ida.saml.security.EidasValidatorFactory;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.MetadataBackedEncryptionCredentialResolver;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SecretKeyDecryptorFactory;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.verifyserviceprovider.configuration.EuropeanIdentityConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.resources.VersionNumberResource;
import uk.gov.ida.verifyserviceprovider.services.AssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.ClassifyingAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasUnsignedAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;
import uk.gov.ida.verifyserviceprovider.services.UnsignedAssertionsResponseHandler;
import uk.gov.ida.verifyserviceprovider.services.VerifyAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;

import javax.ws.rs.client.Client;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.List;
import java.util.Optional;
import java.util.Timer;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory.createStringToResponseTransformer;
import static uk.gov.ida.verifyserviceprovider.validators.EidasEncryptionAlgorithmValidatorHelper.anEidasEncryptionAlgorithmValidator;

public class VerifyServiceProviderFactory {

    private final VerifyServiceProviderConfiguration configuration;
    private final ResponseFactory responseFactory;

    private final DateTimeComparator dateTimeComparator;
    private final EntityIdService entityIdService;
    private final MetadataResolverBundle<VerifyServiceProviderConfiguration> verifyMetadataBundler;
    private final MetadataResolverBundle<VerifyServiceProviderConfiguration> msaMetadataBundle;
    private final ManifestReader manifestReader;
    private final Client client;
    private final IdaKeyStore keyStore;
    private final SamlAssertionsSignatureValidator hubSignatureValidator;

    public VerifyServiceProviderFactory(
            VerifyServiceProviderConfiguration configuration,
            MetadataResolverBundle<VerifyServiceProviderConfiguration> verifyMetadataBundler,
            MetadataResolverBundle<VerifyServiceProviderConfiguration> msaMetadataBundle,
            Client client) throws KeyException {
        this.configuration = configuration;
        List<KeyPair> decryptionKeyPairs = getDecryptionKeyPairs(
                configuration.getSamlPrimaryEncryptionKey(),
                configuration.getSamlSecondaryEncryptionKey()
        );
        this.keyStore = new IdaKeyStore(null, decryptionKeyPairs);
        this.responseFactory = new ResponseFactory(decryptionKeyPairs);
        this.dateTimeComparator = new DateTimeComparator(configuration.getClockSkew());
        this.entityIdService = new EntityIdService(configuration.getServiceEntityIds());
        this.verifyMetadataBundler = verifyMetadataBundler;
        this.msaMetadataBundle = msaMetadataBundle;
        this.manifestReader = new ManifestReader();
        this.client = client;
        this.hubSignatureValidator = new SignatureValidatorFactory().getSignatureValidator(getHubSignatureTrustEngine());
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
        if (configuration.getMsaMetadata().isPresent()) {
            return getTranslateMatchingSamlResponseResource();
        } else if (isEidasEnabled()){
            return getEidasEnabledTranslateNonMatchingSamlResponseResource();
        } else {
            return getTranslateNonMatchingSamlResponseResource();
        }
    }

    private TranslateSamlResponseResource getTranslateMatchingSamlResponseResource() {
        ResponseService matchingResponseService = responseFactory.createMatchingResponseService(
                getHubSignatureTrustEngine(),
                responseFactory.createMsaAssertionTranslator(getMsaSignatureTrustEngine(), new SignatureValidatorFactory(), dateTimeComparator),
                dateTimeComparator
        );
        return new TranslateSamlResponseResource(matchingResponseService, entityIdService);
    }

    private TranslateSamlResponseResource getEidasEnabledTranslateNonMatchingSamlResponseResource() {
        VerifyAssertionTranslator verifyAssertionTranslator = responseFactory.createVerifyIdpAssertionTranslator(
                hubSignatureValidator,
                dateTimeComparator,
                configuration.getHashingEntityId()
        );

        EidasMetadataResolverRepository eidasMetadataResolverRepository = getEidasMetadataResolverRepository();
        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        UnsignedAssertionsResponseHandler unsignedAssertionsResponseHandler = new UnsignedAssertionsResponseHandler(
                new EidasValidatorFactory(eidasMetadataResolverRepository),
                createStringToResponseTransformer(),
                new InstantValidator(dateTimeComparator),
                new SecretKeyDecryptorFactory(idaKeyStoreCredentialRetriever),
                anEidasEncryptionAlgorithmValidator(),
                hubSignatureValidator
        );

        EidasAssertionTranslator eidasAssertionTranslator = responseFactory.createEidasAssertionTranslator(
                dateTimeComparator,
                eidasMetadataResolverRepository,
                configuration.getEuropeanIdentity().get(),
                configuration.getHashingEntityId()
        );

        EidasUnsignedAssertionTranslator eidasUnsignedAssertionTranslator = responseFactory.createEidasUnsignedAssertionTranslator(
                dateTimeComparator,
                eidasMetadataResolverRepository,
                configuration.getEuropeanIdentity().get(),
                configuration.getHashingEntityId()
        );

        AssertionTranslator assertionTranslator = new ClassifyingAssertionTranslator(
                verifyAssertionTranslator,
                eidasAssertionTranslator,
                eidasUnsignedAssertionTranslator);

        return new TranslateSamlResponseResource(
                responseFactory.createNonMatchingResponseService(
                        getHubSignatureTrustEngine(),
                        assertionTranslator,
                        dateTimeComparator,
                        unsignedAssertionsResponseHandler
                ),
                entityIdService);
    }

    private TranslateSamlResponseResource getTranslateNonMatchingSamlResponseResource() {
        VerifyAssertionTranslator assertionTranslator = responseFactory.createVerifyIdpAssertionTranslator(
                hubSignatureValidator,
                dateTimeComparator,
                configuration.getHashingEntityId()
        );

        return new TranslateSamlResponseResource(
                responseFactory.createNonMatchingResponseService(
                        getHubSignatureTrustEngine(),
                        assertionTranslator,
                        dateTimeComparator,
                        null
                ),
                entityIdService);
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

    private EidasMetadataResolverRepository getEidasMetadataResolverRepository() {
        return new EidasMetadataResolverRepository(
                getEidasTrustAnchorResolver(),
                configuration.getEuropeanIdentity().get(),
                new DropwizardMetadataResolverFactory(),
                new Timer(),
                new MetadataSignatureTrustEngineFactory(),
                new MetadataResolverConfigBuilder(),
                client
            );
    }

    private EidasTrustAnchorResolver getEidasTrustAnchorResolver() {
        EidasMetadataConfiguration metadataConfiguration = configuration.getEuropeanIdentity().get();
        return new EidasTrustAnchorResolver(metadataConfiguration.getTrustAnchorUri(), client, metadataConfiguration.getTrustStore());
    }

    private boolean isEidasEnabled() {
        Optional<EuropeanIdentityConfiguration> eidasConfig = configuration.getEuropeanIdentity();
        return eidasConfig.map(EuropeanIdentityConfiguration::isEnabled).orElse(false);
    }
}
