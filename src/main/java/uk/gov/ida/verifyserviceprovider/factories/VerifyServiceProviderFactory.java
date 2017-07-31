package uk.gov.ida.verifyserviceprovider.factories;

import io.dropwizard.setup.Environment;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.healthcheck.MetadataHealthCheck;

public class VerifyServiceProviderFactory {

    private final DropwizardMetadataResolverFactory metadataResolverFactory = new DropwizardMetadataResolverFactory();
    private final Environment environment;
    private final VerifyServiceProviderConfiguration configuration;

    public VerifyServiceProviderFactory(
        VerifyServiceProviderConfiguration configuration,
        Environment environment
    ) {
        this.environment = environment;
        this.configuration = configuration;
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
}
