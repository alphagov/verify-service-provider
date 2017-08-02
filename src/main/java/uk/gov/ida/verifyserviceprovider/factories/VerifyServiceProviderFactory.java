package uk.gov.ida.verifyserviceprovider.factories;

import io.dropwizard.setup.Environment;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.healthcheck.MetadataHealthCheck;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import static uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory.createStringToResponseTransformer;

public class VerifyServiceProviderFactory {

    private final DropwizardMetadataResolverFactory metadataResolverFactory = new DropwizardMetadataResolverFactory();
    private final Environment environment;
    private final VerifyServiceProviderConfiguration configuration;
    private final ResponseFactory responseFactory;

    public VerifyServiceProviderFactory(
        VerifyServiceProviderConfiguration configuration,
        Environment environment
    ) {
        this.environment = environment;
        this.configuration = configuration;
        this.responseFactory = new ResponseFactory(configuration.getSamlPrimaryEncryptionKey(), configuration.getSamlSecondaryEncryptionKey());
    }

    public MetadataHealthCheck getHubMetadataHealthCheck() {
        return new MetadataHealthCheck(
            metadataResolverFactory.createMetadataResolver(environment, configuration.getVerifyHubMetadata()),
            configuration.getVerifyHubMetadata().getExpectedEntityId()
        );
    }

    public MetadataHealthCheck getMsaMetadataHealthCheck() {
        MetadataResolver msaMetadataResolver = metadataResolverFactory.createMetadataResolverWithoutSignatureValidation(environment, configuration.getMsaMetadata());
        return new MetadataHealthCheck(
            msaMetadataResolver,
            configuration.getMsaMetadata().getExpectedEntityId()
        );
    }

    public TranslateSamlResponseResource getTranslateSamlResponseResource() {
        AssertionDecrypter assertionDecrypter = responseFactory.createAssertionDecrypter();
        ResponseService responseService = new ResponseService(createStringToResponseTransformer(), assertionDecrypter);
        return new TranslateSamlResponseResource(responseService);
    }
}
