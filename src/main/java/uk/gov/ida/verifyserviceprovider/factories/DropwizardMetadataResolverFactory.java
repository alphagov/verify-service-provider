package uk.gov.ida.verifyserviceprovider.factories;

import com.google.common.base.Throwables;
import io.dropwizard.servlets.assets.ResourceNotFoundException;
import io.dropwizard.setup.Environment;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import uk.gov.ida.saml.metadata.ExpiredCertificateMetadataFilter;
import uk.gov.ida.saml.metadata.KeyStoreLoader;
import uk.gov.ida.saml.metadata.PKIXSignatureValidationFilterProvider;
import uk.gov.ida.saml.metadata.exception.EmptyTrustStoreException;
import uk.gov.ida.saml.metadata.factories.MetadataClientFactory;
import uk.gov.ida.saml.metadata.factories.MetadataResolverFactory;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderMetadataConfiguration;

import javax.ws.rs.client.Client;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Copy of {@link uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory} which
 * can load trust stores from the classpath as well as the file system.
 *
 * Will load from the classpath if
 * {@link VerifyServiceProviderMetadataConfiguration#shouldLoadTrustStoreFromResources()}
 * returns true, otherwise it will load from the file system.
 */
public class DropwizardMetadataResolverFactory {

    private final MetadataResolverFactory metadataResolverFactory = new MetadataResolverFactory();
    private final ExpiredCertificateMetadataFilter expiredCertificateMetadataFilter = new ExpiredCertificateMetadataFilter();
    private final MetadataClientFactory metadataClientFactory = new MetadataClientFactory();

    public MetadataResolver createMetadataResolver(Environment environment, VerifyServiceProviderMetadataConfiguration metadataConfiguration) {
        return createMetadataResolver(environment, metadataConfiguration, true);
    }

    public MetadataResolver createMetadataResolverWithoutSignatureValidation(Environment environment, VerifyServiceProviderMetadataConfiguration metadataConfiguration) {
        return createMetadataResolver(environment, metadataConfiguration, false);
    }

    private MetadataResolver createMetadataResolver(Environment environment, VerifyServiceProviderMetadataConfiguration metadataConfiguration, boolean validateSignatures) {
        URI uri = metadataConfiguration.getUri();
        Long minRefreshDelay = metadataConfiguration.getMinRefreshDelay();
        Long maxRefreshDelay = metadataConfiguration.getMaxRefreshDelay();
        Client client = metadataClientFactory.getClient(environment, metadataConfiguration);

        return metadataResolverFactory.create(
            client,
            uri,
            getMetadataFilters(metadataConfiguration, validateSignatures),
            minRefreshDelay,
            maxRefreshDelay
        );
    }

    private List<MetadataFilter> getMetadataFilters(VerifyServiceProviderMetadataConfiguration metadataConfiguration, boolean validateSignatures) {
        if (!validateSignatures) {
            return emptyList();
        }

        KeyStore metadataTrustStore = getMetadataTrustStore(metadataConfiguration);
        PKIXSignatureValidationFilterProvider pkixSignatureValidationFilterProvider = new PKIXSignatureValidationFilterProvider(metadataTrustStore);
        return asList(pkixSignatureValidationFilterProvider.get(), expiredCertificateMetadataFilter);
    }

    private KeyStore getMetadataTrustStore(VerifyServiceProviderMetadataConfiguration metadataConfiguration) {
        KeyStore trustStore;
        String trustStorePath = metadataConfiguration.getTrustStorePath();
        if (metadataConfiguration.shouldLoadTrustStoreFromResources()) {
            InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream(trustStorePath);
            if (trustStoreStream == null) {
                throw new ResourceNotFoundException(new FileNotFoundException("Could not load resource from path " + trustStorePath));
            }
            trustStore = new KeyStoreLoader().load(
                trustStoreStream,
                metadataConfiguration.getTrustStorePassword()
            );
        } else {
            trustStore = new KeyStoreLoader().load(trustStorePath, metadataConfiguration.getTrustStorePassword());
        }
        return validateTruststore(trustStore);
    }

    private KeyStore validateTruststore(KeyStore trustStore) {
        int trustStoreSize;
        try {
            trustStoreSize = trustStore.size();
        } catch (KeyStoreException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        if (trustStoreSize == 0) {
            throw new EmptyTrustStoreException();
        }
        return trustStore;
    }
}
