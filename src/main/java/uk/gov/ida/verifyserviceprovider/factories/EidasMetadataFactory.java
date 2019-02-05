package uk.gov.ida.verifyserviceprovider.factories;

import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.saml.metadata.EidasTrustAnchorResolver;
import uk.gov.ida.saml.metadata.MetadataResolverConfigBuilder;
import uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory;
import uk.gov.ida.saml.metadata.factories.MetadataSignatureTrustEngineFactory;
import uk.gov.ida.verifyserviceprovider.configuration.EuropeanIdentityConfiguration;

import javax.ws.rs.client.Client;
import java.util.Timer;

public class EidasMetadataFactory {
    public EidasMetadataResolverRepository createEidasMetadataResolverRepository(EuropeanIdentityConfiguration europeanIdentity, Client client) {
        EidasMetadataConfiguration aggregatedMetadata = europeanIdentity.getAggregatedMetadata();
        return new EidasMetadataResolverRepository(
                getEidasTrustAnchorResolver(client, aggregatedMetadata),
                aggregatedMetadata,
                new DropwizardMetadataResolverFactory(),
                new Timer(),
                new MetadataSignatureTrustEngineFactory(),
                new MetadataResolverConfigBuilder(),
                client
        );
    }

    private EidasTrustAnchorResolver getEidasTrustAnchorResolver(Client client, EidasMetadataConfiguration aggregatedMetadata) {
        return new EidasTrustAnchorResolver(aggregatedMetadata.getTrustAnchorUri(), client, aggregatedMetadata.getTrustStore());
    }
}
