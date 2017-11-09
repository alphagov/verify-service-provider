package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.KeyStore;

import static io.dropwizard.util.Duration.minutes;
import static io.dropwizard.util.Duration.seconds;
import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.MSA_JERSEY_CLIENT_NAME;

public class MsaMetadataConfiguration extends MetadataConfiguration {
    @JsonCreator
    public MsaMetadataConfiguration(
            @JsonProperty("uri") @JsonAlias({ "url" }) URI uri,
            @JsonProperty("minRefreshDelay") Long minRefreshDelay,
            @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
            @JsonProperty(value = "expectedEntityId", required = true) String expectedEntityId,
            @JsonProperty("client") JerseyClientConfiguration client,
            @JsonProperty("jerseyClientName") String jerseyClientName,
            @JsonProperty("hubFederationId") String hubFederationId
    ) {
        super(uri, minRefreshDelay, maxRefreshDelay, expectedEntityId, client, ofNullable(jerseyClientName).orElse(MSA_JERSEY_CLIENT_NAME), hubFederationId);
    }

    @Override
    public KeyStore getTrustStore() {
        throw new NotImplementedException("MSA metadata is not signed, so no trust store available");
    }
}
