package uk.gov.ida.verifyserviceprovider.configuration;

import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;

public interface VerifyServiceProviderMetadataConfiguration extends MetadataResolverConfiguration {
    boolean shouldLoadTrustStoreFromResources();
}
